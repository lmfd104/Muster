package com.lmfd.warboss.data.bsdata

import androidx.room.withTransaction
import com.lmfd.warboss.data.db.WarbossDatabase
import com.lmfd.warboss.data.db.entity.CategoryLinkEntity
import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.FactionEntity
import com.lmfd.warboss.data.db.entity.GameSystemEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity
import com.lmfd.warboss.data.db.entity.UnitEntity
import com.lmfd.warboss.data.prefs.ImportPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface BsDataRepository {
    val status: StateFlow<ImportStatus>
    fun startImport()
    fun cancelImport()
}

@Singleton
class BsDataRepositoryImpl @Inject constructor(
    private val downloader: BsDataDownloader,
    private val parser: BsDataParser,
    private val db: WarbossDatabase,
    private val prefs: ImportPrefs,
) : BsDataRepository {

    // App-scoped: lives until the process dies. SupervisorJob so one failure doesn't
    // cancel all other coroutines launched on this scope.
    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var importJob: Job? = null

    private val _status = MutableStateFlow<ImportStatus>(prefs.initialStatus())
    override val status: StateFlow<ImportStatus> = _status.asStateFlow()

    override fun startImport() {
        val current = _status.value
        if (current is ImportStatus.Downloading || current is ImportStatus.Parsing) return
        importJob = importScope.launch { runImport() }
    }

    override fun cancelImport() {
        importJob?.cancel()
        importJob = null
        prefs.markImportFailed()
        _status.value = ImportStatus.Error("Import cancelled")
    }

    private suspend fun runImport() {
        var zipFile: java.io.File? = null
        try {
            prefs.markImportStarted()

            // ── Download ─────────────────────────────────────────────────
            _status.value = ImportStatus.Downloading(0L, -1L)
            val downloaded = withContext(Dispatchers.IO) {
                downloader.download { bytesRead, total ->
                    _status.value = ImportStatus.Downloading(bytesRead, total)
                }
            }
            zipFile = downloaded

            // ── Parse ─────────────────────────────────────────────────────
            _status.value = ImportStatus.Parsing("Preparing…", 0, 0)
            val registry = GameSystemTypeRegistry()
            val archiveResult = withContext(Dispatchers.IO) {
                parser.parseArchive(downloaded, registry) { factionName, index, total ->
                    _status.value = ImportStatus.Parsing(factionName, index, total)
                }
            }

            // ── Commit ────────────────────────────────────────────────────
            val (factionCount, skippedCount) = commitArchive(archiveResult)

            prefs.markImportComplete(factionCount, skippedCount)
            downloaded.delete()
            _status.value = ImportStatus.Complete(factionCount, skippedCount)

        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            prefs.markImportFailed()
            zipFile?.delete()
            _status.value = ImportStatus.Error(e.message ?: "Unknown error", e)
        }
    }

    internal suspend fun commitArchive(result: ParseArchiveResult): Pair<Int, Int> {
        var committed = 0
        var skipped = 0
        db.withTransaction {
            // Wipe existing data (delete child tables first to avoid FK violations)
            db.profileDao().deleteAllCategoryLinks()
            db.profileDao().deleteAllKeywords()
            db.profileDao().deleteAllCharacteristics()
            db.profileDao().deleteAllProfiles()
            db.unitDao().deleteAll()
            db.factionDao().deleteAll()

            // Insert game system (REPLACE is idempotent)
            result.gameSystem?.let { gs ->
                db.gameSystemDao().insertOrReplace(
                    GameSystemEntity(gs.id, gs.name, gs.revision, gs.bsVersion)
                )
            }

            // Insert each faction and its units
            val gsId = result.gameSystem?.id ?: ""
            for (faction in result.factions) {
                val effectiveGsId = gsId.ifEmpty { faction.gameSystemId }
                if (effectiveGsId.isEmpty()) { skipped++; continue }
                try {
                    commitFaction(faction, effectiveGsId)
                    committed++
                } catch (_: Exception) {
                    skipped++
                }
            }
        }
        return committed to skipped
    }

    private suspend fun commitFaction(faction: ParsedFaction, gameSystemId: String) {
        db.factionDao().insertOrReplace(
            FactionEntity(
                id = faction.catalogueId,
                name = faction.catalogueName,
                revision = faction.revision,
                gameSystemId = gameSystemId,
                isLibrary = faction.isLibrary,
            )
        )
        for (unit in faction.units) {
            db.unitDao().insertOrReplace(
                listOf(
                    UnitEntity(
                        id = unit.id,
                        factionId = faction.catalogueId,
                        name = unit.name,
                        type = unit.type,
                        points = unit.points,
                        minQuantity = unit.minQuantity,
                        maxQuantity = unit.maxQuantity,
                        hasUnresolvableLinks = unit.hasUnresolvableLinks,
                    )
                )
            )
            // Profiles — use synthetic ID "<profileId>_<unitId>" to avoid PK collisions
            // when the same shared profile is referenced by multiple units.
            val profileEntities = unit.profiles.map { p ->
                ProfileEntity(
                    id = "${p.id}_${unit.id}",
                    entryId = unit.id,
                    factionId = faction.catalogueId,
                    name = p.name,
                    typeName = p.typeName,
                )
            }
            if (profileEntities.isNotEmpty()) db.profileDao().insertProfiles(profileEntities)

            val characteristics = unit.profiles.flatMap { p ->
                val syntheticProfileId = "${p.id}_${unit.id}"
                p.characteristics.map { (charName, charValue) ->
                    CharacteristicEntity(0L, syntheticProfileId, charName, charValue)
                }
            }
            if (characteristics.isNotEmpty()) db.profileDao().insertCharacteristics(characteristics)

            val keywords = unit.keywords.map { kw -> KeywordEntity(0L, unit.id, kw, false) } +
                unit.factionKeywords.map { kw -> KeywordEntity(0L, unit.id, kw, true) }
            if (keywords.isNotEmpty()) db.profileDao().insertKeywords(keywords)

            val categoryLinks = unit.categoryLinks.map { cl ->
                CategoryLinkEntity(0L, unit.id, cl.categoryId, cl.categoryName, cl.isPrimary)
            }
            if (categoryLinks.isNotEmpty()) db.profileDao().insertCategoryLinks(categoryLinks)
        }
    }
}
