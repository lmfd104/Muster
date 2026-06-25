package com.lmfd.warboss.data.bsdata

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

// ─── Intermediate parsed data ─────────────────────────────────────────────────

data class ParsedGameSystem(
    val id: String,
    val name: String,
    val revision: Int,
    val bsVersion: String,
)

data class ParsedFaction(
    val catalogueId: String,
    val catalogueName: String,
    val revision: Int,
    val gameSystemId: String,
    val isLibrary: Boolean,
    val units: List<ParsedUnit>,
)

data class ParsedUnit(
    val id: String,
    val name: String,
    val type: String,
    val points: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    val profiles: List<ParsedProfile>,
    val keywords: List<String>,
    val factionKeywords: List<String>,
    val categoryLinks: List<ParsedCategoryLink>,
    val hasUnresolvableLinks: Boolean,
)

data class ParsedProfile(
    val id: String,
    val name: String,
    val typeName: String,
    val characteristics: Map<String, String>,
)

data class ParsedCategoryLink(
    val categoryId: String,
    val categoryName: String,
    val isPrimary: Boolean,
)

data class ParseArchiveResult(
    val gameSystem: ParsedGameSystem?,
    val factions: List<ParsedFaction>,
)

// ─── Parser ───────────────────────────────────────────────────────────────────

@Singleton
class BsDataParser @Inject constructor() {

    /**
     * Parse the full downloaded archive in three passes.
     * Uses ZipFile (random-access) so each entry can be opened independently.
     * GitHub archives have entries prefixed with "<repo>-<branch>/".
     */
    fun parseArchive(
        zipFile: File,
        registry: GameSystemTypeRegistry,
        onParsing: (factionName: String, index: Int, total: Int) -> Unit,
    ): ParseArchiveResult {
        ZipFile(zipFile).use { zip ->
            // Pass 1: parse the single .gst file
            val gstEntry = zip.entries().asSequence()
                .firstOrNull { !it.isDirectory && it.name.endsWith(".gst", ignoreCase = true) }
                ?: throw ImportException("No game system (.gst) file found in archive")

            val gameSystem = zip.getInputStream(gstEntry).buffered().use { parseGst(it, registry) }
            if (registry.ptsTypeId.isEmpty()) {
                throw ImportException("Game system file contains no 'pts' cost type")
            }

            // Collect all .cat entries
            val catEntries = zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".cat", ignoreCase = true) }
                .toList()

            // Pass 2: populate registry from ALL .cat shared sections
            for (entry in catEntries) {
                zip.getInputStream(entry).buffered().use { parseSharedSections(it, registry) }
            }

            // Pass 3: parse unit selectionEntries from each .cat
            val factions = mutableListOf<ParsedFaction>()
            catEntries.forEachIndexed { index, entry ->
                val faction = zip.getInputStream(entry).buffered().use { parseFaction(it, registry) }
                if (faction != null) {
                    factions.add(faction)
                    onParsing(faction.catalogueName, index + 1, catEntries.size)
                }
            }

            return ParseArchiveResult(gameSystem, factions)
        }
    }

    // ─── Pass 1: .gst ──────────────────────────────────────────────────────

    fun parseGst(input: InputStream, registry: GameSystemTypeRegistry): ParsedGameSystem? {
        val parser = newParser(input)
        // Advance to <gameSystem> element
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "gameSystem") break
        }
        if (parser.eventType != XmlPullParser.START_TAG) return null

        val id = parser.attr("id")
        val name = parser.attr("name")
        val revision = parser.attr("revision").toIntOrNull() ?: 0
        val bsVersion = parser.attr("battleScribeVersion")

        // Iterate direct children of <gameSystem>
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "costTypes" -> {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "costType") {
                            val typeId = parser.attr("id")
                            val typeName = parser.attr("name")
                            if (typeName.equals("pts", ignoreCase = true) && typeId.isNotEmpty()) {
                                registry.ptsTypeId = typeId
                            }
                        }
                        parser.skip()
                    }
                }
                "profileTypes" -> {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "profileType") {
                            val typeId = parser.attr("id")
                            val typeName = parser.attr("name")
                            if (typeId.isNotEmpty() && typeName.isNotEmpty()) {
                                registry.profileTypeNames[typeId] = typeName
                            }
                        }
                        parser.skip()
                    }
                }
                "categoryEntries" -> {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "categoryEntry") {
                            val catId = parser.attr("id")
                            val catName = parser.attr("name")
                            if (catId.isNotEmpty() && catName.isNotEmpty()) {
                                registry.categoryNames[catId] = catName
                            }
                        }
                        parser.skip()
                    }
                }
                "sharedProfiles" -> collectSharedProfilesInto(parser, registry)
                "sharedSelectionEntries" -> collectSharedEntryProfilesInto(parser, registry)
                else -> parser.skip()
            }
        }
        // parser at END_TAG "gameSystem"

        return if (id.isNotEmpty()) ParsedGameSystem(id, name, revision, bsVersion) else null
    }

    // ─── Pass 2: .cat shared sections ──────────────────────────────────────

    fun parseSharedSections(input: InputStream, registry: GameSystemTypeRegistry) {
        val parser = newParser(input)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "catalogue") break
        }
        if (parser.eventType != XmlPullParser.START_TAG) return

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "sharedProfiles", "sharedRules" -> collectSharedProfilesInto(parser, registry)
                "sharedSelectionEntries" -> collectSharedEntryProfilesInto(parser, registry)
                else -> parser.skip()
            }
        }
    }

    // ─── Pass 3: .cat unit selectionEntries ─────────────────────────────────

    fun parseFaction(input: InputStream, registry: GameSystemTypeRegistry): ParsedFaction? {
        val parser = newParser(input)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "catalogue") break
        }
        if (parser.eventType != XmlPullParser.START_TAG) return null

        val catalogueId = parser.attr("id").ifEmpty { return null }
        val catalogueName = parser.attr("name").ifEmpty { return null }
        val revision = parser.attr("revision").toIntOrNull() ?: 0
        val gameSystemId = parser.attr("gameSystemId")
        val isLibrary = parser.attr("library") == "true"

        val units = mutableListOf<ParsedUnit>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "selectionEntries" -> {
                    // Direct unit definitions inside the catalogue
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "selectionEntry") {
                            val unit = parseUnitEntry(parser, registry)
                            if (unit != null) units.add(unit)
                        } else {
                            parser.skip()
                        }
                    }
                }
                "entryLinks" -> {
                    // Catalogue-level links to shared unit entries (wh40k-10e pattern: units live
                    // in the GST sharedSelectionEntries, faction catalogues reference them here)
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "entryLink") {
                            val linkType = parser.attr("type")
                            val targetId = parser.attr("targetId")
                            val linkId = parser.attr("id")
                            val linkName = parser.attr("name")
                            if (linkType == "selectionEntry" && targetId.isNotEmpty()) {
                                registry.sharedUnits[targetId]?.let { shared ->
                                    units.add(shared.copy(
                                        id = linkId.ifEmpty { targetId },
                                        name = linkName.ifEmpty { shared.name },
                                    ))
                                }
                            }
                        }
                        parser.skip()
                    }
                }
                else -> parser.skip()
            }
        }

        return ParsedFaction(catalogueId, catalogueName, revision, gameSystemId, isLibrary, units)
    }

    // ─── Element parsers ───────────────────────────────────────────────────

    /**
     * Pre: parser at START_TAG "selectionEntry".
     * Post: parser at END_TAG "selectionEntry".
     */
    private fun parseUnitEntry(parser: XmlPullParser, registry: GameSystemTypeRegistry): ParsedUnit? {
        val type = parser.attr("type")
        val id = parser.attr("id").ifEmpty { parser.skip(); return null }
        val name = parser.attr("name").ifEmpty { parser.skip(); return null }
        if (type != "unit") { parser.skip(); return null }

        val profiles = mutableListOf<ParsedProfile>()
        val infoLinkTargetIds = mutableListOf<String>()
        val entryLinkTargetIds = mutableListOf<String>()
        val keywords = mutableListOf<String>()
        val factionKeywords = mutableListOf<String>()
        val categoryLinks = mutableListOf<ParsedCategoryLink>()
        var points = 0
        var minQuantity = 0
        var maxQuantity = 9999

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "profiles" -> profiles.addAll(parseProfilesElement(parser))
                "costs" -> points = parseCostsElement(parser, registry.ptsTypeId)
                "infoLinks" -> infoLinkTargetIds.addAll(parseInfoLinksElement(parser))
                "entryLinks" -> entryLinkTargetIds.addAll(parseEntryLinksElement(parser))
                "categoryLinks" -> categoryLinks.addAll(parseCategoryLinksElement(parser, registry))
                "constraints" -> {
                    val (mn, mx) = parseConstraintsElement(parser)
                    minQuantity = mn
                    maxQuantity = mx
                }
                else -> parser.skip()
            }
        }
        // parser at END_TAG "selectionEntry"

        // Resolve infoLinks from registry (profiles and rules)
        var hasUnresolvable = false
        for (targetId in infoLinkTargetIds) {
            val rp = registry.sharedProfiles[targetId]
            if (rp != null) {
                profiles.add(ParsedProfile(rp.id, rp.name, rp.typeName, rp.characteristics))
            } else {
                hasUnresolvable = true
            }
        }

        // Resolve entryLinks to model profiles (one level deep)
        for (targetId in entryLinkTargetIds) {
            registry.sharedEntryProfiles[targetId]?.forEach { rp ->
                profiles.add(ParsedProfile(rp.id, rp.name, rp.typeName, rp.characteristics))
            }
            // Missing model entries are normal (model in a dependency .cat); don't flag
        }

        // Derive keywords and faction keywords from category links
        for (cl in categoryLinks) {
            val catName = cl.categoryName
            if (catName.startsWith("Faction: ")) {
                factionKeywords.add(catName.removePrefix("Faction: "))
            } else if (catName.isNotEmpty()) {
                keywords.add(catName)
            }
        }

        return ParsedUnit(
            id = id,
            name = name,
            type = type,
            points = points,
            minQuantity = minQuantity,
            maxQuantity = maxQuantity,
            profiles = profiles,
            keywords = keywords,
            factionKeywords = factionKeywords,
            categoryLinks = categoryLinks,
            hasUnresolvableLinks = hasUnresolvable,
        )
    }

    /**
     * Pre: parser at START_TAG "profiles".
     * Post: parser at END_TAG "profiles".
     */
    private fun parseProfilesElement(parser: XmlPullParser): List<ParsedProfile> {
        val result = mutableListOf<ParsedProfile>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "profile") {
                val p = parseProfileElement(parser)
                if (p != null) result.add(p)
            } else {
                parser.skip()
            }
        }
        return result
    }

    /**
     * Pre: parser at START_TAG "profile".
     * Post: parser at END_TAG "profile".
     */
    fun parseProfileElement(parser: XmlPullParser): ParsedProfile? {
        val id = parser.attr("id").ifEmpty { parser.skip(); return null }
        val name = parser.attr("name")
        val typeName = parser.attr("typeName")
        val characteristics = mutableMapOf<String, String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "characteristics" -> {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) continue
                        if (parser.name == "characteristic") {
                            val charName = parser.attr("name")
                            // nextText() reads text content and leaves parser at END_TAG "characteristic"
                            val value = parser.nextText()
                            if (charName.isNotEmpty()) characteristics[charName] = value
                        } else {
                            parser.skip()
                        }
                    }
                }
                else -> parser.skip()
            }
        }
        return ParsedProfile(id, name, typeName, characteristics)
    }

    /**
     * Pre: parser at START_TAG "costs".
     * Post: parser at END_TAG "costs".
     */
    private fun parseCostsElement(parser: XmlPullParser, ptsTypeId: String): Int {
        var pts = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "cost") {
                val typeId = parser.attr("typeId")
                val value = parser.attr("value")
                if (typeId == ptsTypeId) pts = value.toDoubleOrNull()?.toInt() ?: 0
            }
            parser.skip()
        }
        return pts
    }

    /**
     * Pre: parser at START_TAG "infoLinks".
     * Post: parser at END_TAG "infoLinks".
     */
    private fun parseInfoLinksElement(parser: XmlPullParser): List<String> {
        val result = mutableListOf<String>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "infoLink") {
                val linkType = parser.attr("type")
                val targetId = parser.attr("targetId")
                if ((linkType == "profile" || linkType == "rule") && targetId.isNotEmpty()) {
                    result.add(targetId)
                }
            }
            parser.skip()
        }
        return result
    }

    /**
     * Pre: parser at START_TAG "entryLinks".
     * Post: parser at END_TAG "entryLinks".
     */
    private fun parseEntryLinksElement(parser: XmlPullParser): List<String> {
        val result = mutableListOf<String>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "entryLink") {
                val linkType = parser.attr("type")
                val targetId = parser.attr("targetId")
                if (linkType == "selectionEntry" && targetId.isNotEmpty()) result.add(targetId)
            }
            parser.skip()
        }
        return result
    }

    /**
     * Pre: parser at START_TAG "categoryLinks".
     * Post: parser at END_TAG "categoryLinks".
     */
    private fun parseCategoryLinksElement(
        parser: XmlPullParser,
        registry: GameSystemTypeRegistry,
    ): List<ParsedCategoryLink> {
        val result = mutableListOf<ParsedCategoryLink>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "categoryLink") {
                val targetId = parser.attr("targetId")
                val primary = parser.attr("primary") == "true"
                val name = registry.categoryNames[targetId] ?: parser.attr("name")
                if (targetId.isNotEmpty()) result.add(ParsedCategoryLink(targetId, name, primary))
            }
            parser.skip()
        }
        return result
    }

    /**
     * Pre: parser at START_TAG "constraints".
     * Post: parser at END_TAG "constraints".
     */
    private fun parseConstraintsElement(parser: XmlPullParser): Pair<Int, Int> {
        var min = 0
        var max = 9999
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "constraint") {
                val type = parser.attr("type")
                val value = parser.attr("value").toIntOrNull() ?: 0
                when (type) {
                    "min" -> min = value
                    "max" -> max = value
                }
            }
            parser.skip()
        }
        return min to max
    }

    // ─── Registry collection helpers ─────────────────────────────────────────

    /**
     * Collect all <profile> elements directly inside the current element into registry.
     * Pre: parser at START_TAG "sharedProfiles" or "sharedRules".
     * Post: parser at their END_TAG.
     */
    private fun collectSharedProfilesInto(parser: XmlPullParser, registry: GameSystemTypeRegistry) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "profile") {
                val p = parseProfileElement(parser)
                if (p != null) {
                    registry.addSharedProfile(
                        GameSystemTypeRegistry.RegistryProfile(p.id, p.name, p.typeName, p.characteristics)
                    )
                }
            } else {
                parser.skip()
            }
        }
    }

    /**
     * Collect profiles from <selectionEntry> elements directly inside the current element.
     * Pre: parser at START_TAG "sharedSelectionEntries".
     * Post: parser at END_TAG "sharedSelectionEntries".
     */
    private fun collectSharedEntryProfilesInto(parser: XmlPullParser, registry: GameSystemTypeRegistry) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name != "selectionEntry") { parser.skip(); continue }

            val entryId = parser.attr("id")
            val entryType = parser.attr("type")
            if (entryId.isEmpty()) { parser.skip(); continue }

            if (entryType == "unit") {
                // Full unit parse — stored for catalogue-level entryLink resolution in Pass 3
                val unit = parseUnitEntry(parser, registry)
                if (unit != null) {
                    registry.addSharedUnit(unit)
                    // Also register profiles under the entry id for nested entryLink resolution
                    unit.profiles.forEach { p ->
                        registry.addSharedEntryProfile(
                            entryId,
                            GameSystemTypeRegistry.RegistryProfile(p.id, p.name, p.typeName, p.characteristics)
                        )
                    }
                }
            } else {
                // Non-unit shared entry: collect only profiles for nested entryLink resolution
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) continue
                    if (parser.name == "profiles") {
                        parseProfilesElement(parser).forEach { p ->
                            registry.addSharedEntryProfile(
                                entryId,
                                GameSystemTypeRegistry.RegistryProfile(p.id, p.name, p.typeName, p.characteristics)
                            )
                        }
                    } else {
                        parser.skip()
                    }
                }
            }
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun newParser(input: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        return factory.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, "UTF-8")
        }
    }

    /**
     * Skip from current START_TAG through its matching END_TAG.
     * Post: parser at END_TAG of the skipped element.
     */
    private fun XmlPullParser.skip() {
        check(eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth > 0) {
            when (next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    private fun XmlPullParser.attr(name: String): String = getAttributeValue(null, name) ?: ""
}
