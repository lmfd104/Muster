package com.lmfd.warboss.data.repository

import com.lmfd.warboss.data.db.WarbossDatabase
import com.lmfd.warboss.domain.model.CategoryLink
import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitProfile
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitRepositoryImpl @Inject constructor(
    private val db: WarbossDatabase,
) : UnitRepository {

    override fun getFactions(): Flow<List<Faction>> =
        db.factionDao().listAll().map { list ->
            list.map { e -> Faction(e.id, e.name, e.revision) }
        }

    override fun getUnitsForFaction(factionId: String): Flow<List<UnitSummary>> =
        db.unitDao().listByFaction(factionId).map { list ->
            list.map { e ->
                UnitSummary(
                    id = e.id,
                    factionId = e.factionId,
                    name = e.name,
                    type = e.type,
                    points = e.points,
                    minQuantity = e.minQuantity,
                    maxQuantity = e.maxQuantity,
                    hasUnresolvableLinks = e.hasUnresolvableLinks,
                )
            }
        }

    override suspend fun getUnitDetail(unitId: String): UnitDetail? {
        val entity = db.unitDao().getById(unitId).first() ?: return null

        val profileEntities = db.profileDao().listByEntryId(unitId)
        val profiles = profileEntities.map { pe ->
            val chars = db.profileDao().listCharacteristics(pe.id)
            UnitProfile(
                id = pe.id,
                name = pe.name,
                typeName = pe.typeName,
                characteristics = chars.associate { it.name to it.value },
            )
        }

        val keywordEntities = db.profileDao().listKeywords(unitId)
        val keywords = keywordEntities.filter { !it.isFactionKeyword }.map { it.keyword }
        val factionKeywords = keywordEntities.filter { it.isFactionKeyword }.map { it.keyword }

        val categoryLinkEntities = db.profileDao().listCategoryLinks(unitId)
        val categoryLinks = categoryLinkEntities.map { cl ->
            CategoryLink(cl.categoryId, cl.categoryName, cl.isPrimary)
        }

        return UnitDetail(
            summary = UnitSummary(
                id = entity.id,
                factionId = entity.factionId,
                name = entity.name,
                type = entity.type,
                points = entity.points,
                minQuantity = entity.minQuantity,
                maxQuantity = entity.maxQuantity,
                hasUnresolvableLinks = entity.hasUnresolvableLinks,
            ),
            profiles = profiles,
            keywords = keywords,
            factionKeywords = factionKeywords,
            categoryLinks = categoryLinks,
        )
    }
}
