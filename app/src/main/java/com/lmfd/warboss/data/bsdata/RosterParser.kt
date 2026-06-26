package com.lmfd.warboss.data.bsdata

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

data class ParsedRoster(
    val name: String,
    val factionName: String,
    val factionId: String,
    val units: List<RosterUnit>,
)

data class RosterUnit(
    val entryId: String,
    val name: String,
    val quantity: Int,
    val pointsPerUnit: Int,
)

object RosterParser {

    fun parse(stream: InputStream, isZipped: Boolean): ParsedRoster {
        if (isZipped) {
            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".ros", ignoreCase = true)) {
                        return parseXml(zip)
                    }
                    entry = zip.nextEntry
                }
            }
            throw IllegalArgumentException("No .ros file found inside the .rosz archive")
        } else {
            return parseXml(stream)
        }
    }

    private fun parseXml(stream: InputStream): ParsedRoster {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        var rosterName = "Imported Roster"
        var factionName = ""
        var factionId = ""
        val units = mutableListOf<RosterUnit>()

        val tagStack = mutableListOf<String>()

        // Current unit accumulator
        var unitName: String? = null
        var unitEntryId: String? = null
        var unitNumber = 1
        var unitPointsTotal = 0.0
        var unitSelectionDepth = -1
        var inUnitCosts = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name
                    tagStack.add(tag)
                    val depth = tagStack.size

                    when (tag) {
                        "roster" -> {
                            rosterName = parser.getAttributeValue(null, "name") ?: rosterName
                        }
                        "force" -> {
                            if (factionName.isEmpty()) {
                                factionName = parser.getAttributeValue(null, "catalogueName") ?: ""
                                factionId = parser.getAttributeValue(null, "catalogueId") ?: ""
                            }
                        }
                        "selection" -> {
                            val type = parser.getAttributeValue(null, "type") ?: ""
                            val parentTag = tagStack.getOrNull(depth - 2) ?: ""
                            val grandparentTag = tagStack.getOrNull(depth - 3) ?: ""
                            if (unitSelectionDepth == -1
                                && type == "unit"
                                && parentTag == "selections"
                                && grandparentTag == "force"
                            ) {
                                unitSelectionDepth = depth
                                unitName = parser.getAttributeValue(null, "name") ?: ""
                                unitEntryId = parser.getAttributeValue(null, "entryId")
                                    ?: parser.getAttributeValue(null, "id")
                                    ?: UUID.randomUUID().toString()
                                unitNumber = parser.getAttributeValue(null, "number")
                                    ?.toIntOrNull() ?: 1
                                unitPointsTotal = 0.0
                                inUnitCosts = false
                            }
                        }
                        "costs" -> {
                            if (unitSelectionDepth != -1 && depth == unitSelectionDepth + 1) {
                                inUnitCosts = true
                            }
                        }
                        "cost" -> {
                            if (inUnitCosts) {
                                val costName = (parser.getAttributeValue(null, "name") ?: "").trim()
                                if (costName == "pts") {
                                    unitPointsTotal = parser.getAttributeValue(null, "value")
                                        ?.toDoubleOrNull() ?: 0.0
                                }
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val depth = tagStack.size
                    val tag = parser.name

                    when (tag) {
                        "selection" -> {
                            if (depth == unitSelectionDepth && unitName != null) {
                                val perUnit = if (unitNumber > 0) unitPointsTotal / unitNumber else unitPointsTotal
                                units.add(
                                    RosterUnit(
                                        entryId = unitEntryId ?: UUID.randomUUID().toString(),
                                        name = unitName!!,
                                        quantity = unitNumber,
                                        pointsPerUnit = perUnit.toInt(),
                                    )
                                )
                                unitName = null
                                unitEntryId = null
                                unitNumber = 1
                                unitPointsTotal = 0.0
                                unitSelectionDepth = -1
                                inUnitCosts = false
                            }
                        }
                        "costs" -> {
                            if (inUnitCosts && depth == unitSelectionDepth + 1) {
                                inUnitCosts = false
                            }
                        }
                    }
                    if (tagStack.isNotEmpty()) tagStack.removeLast()
                }
            }
            eventType = parser.next()
        }

        return ParsedRoster(rosterName, factionName, factionId, units)
    }
}
