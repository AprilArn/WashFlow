package com.aprilarn.washflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

class AppLanguageTest {

    @Test
    fun fromTag_mapsKnownTagsCorrectly() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("EN"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("fr"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromTag("id"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromTag("ID"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromTag("in"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromTag("id-ID"))
    }

    @Test
    fun speechLocaleTags_matchExpectedLocales() {
        assertEquals("en-US", AppLanguage.ENGLISH.speechLocaleTag)
        assertEquals("id-ID", AppLanguage.INDONESIAN.speechLocaleTag)
        assertEquals("English", AppLanguage.ENGLISH.nativeLabel)
        assertEquals("Bahasa Indonesia", AppLanguage.INDONESIAN.nativeLabel)
    }

    @Test
    fun stringsXml_hasFullParityBetweenEnglishAndIndonesian() {
        val enFile = resolveResFile("values/strings.xml")
        val idFile = resolveResFile("values-in/strings.xml")

        assertTrue("English strings.xml not found at ${enFile.absolutePath}", enFile.exists())
        assertTrue("Indonesian strings.xml not found at ${idFile.absolutePath}", idFile.exists())

        val enEntries = parseStringsXml(enFile)
        val idEntries = parseStringsXml(idFile)

        val expectedTranslatableKeys = enEntries
            .filterValues { it.translatable }
            .keys

        val missingInIndonesian = expectedTranslatableKeys - idEntries.keys
        val extraInIndonesian = idEntries.keys - expectedTranslatableKeys

        assertEquals("Missing keys in values-in/strings.xml", emptySet<String>(), missingInIndonesian)
        assertEquals("Extra or non-translatable keys in values-in/strings.xml", emptySet<String>(), extraInIndonesian)

        val placeholderRegex = Regex("%\\d+\\$[sd]|%[sd]")
        for (key in expectedTranslatableKeys) {
            val enPlaceholders = placeholderRegex.findAll(enEntries.getValue(key).value).map { it.value }.sorted().toList()
            val idPlaceholders = placeholderRegex.findAll(idEntries.getValue(key).value).map { it.value }.sorted().toList()
            assertEquals("Format placeholder mismatch for key '$key'", enPlaceholders, idPlaceholders)
        }

        val forbiddenOperationalTerms = listOf("laundry", "cucian", "setrika", "jemur")
        for ((key, entry) in enEntries) {
            val lower = entry.value.lowercase()
            for (term in forbiddenOperationalTerms) {
                assertTrue("English key '$key' contains industry-specific term '$term'", !lower.contains(term))
            }
        }
        for ((key, entry) in idEntries) {
            val lower = entry.value.lowercase()
            for (term in forbiddenOperationalTerms) {
                assertTrue("Indonesian key '$key' contains industry-specific term '$term'", !lower.contains(term))
            }
        }
    }

    private data class StringEntry(val value: String, val translatable: Boolean)

    private fun resolveResFile(relativePath: String): File {
        val direct = File("src/main/res/$relativePath")
        if (direct.exists()) return direct
        return File("app/src/main/res/$relativePath")
    }

    private fun parseStringsXml(file: File): Map<String, StringEntry> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.getElementsByTagName("string")
        val result = linkedMapOf<String, StringEntry>()
        for (i in 0 until nodes.length) {
            val el = nodes.item(i) as Element
            val name = el.getAttribute("name")
            val translatable = el.getAttribute("translatable") != "false"
            result[name] = StringEntry(value = el.textContent, translatable = translatable)
        }
        return result
    }
}
