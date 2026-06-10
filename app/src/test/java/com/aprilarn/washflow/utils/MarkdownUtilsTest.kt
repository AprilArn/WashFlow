package com.aprilarn.washflow.utils

import org.junit.Test
import org.junit.Assert.*
import androidx.compose.ui.text.font.FontWeight

class MarkdownUtilsTest {

    @Test
    fun testParseBold() {
        val input = "Hello **world**"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("Hello world", result.text)
        
        val spans = result.spanStyles
        assertTrue(spans.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun testParseListCurrentBehavior() {
        val input = "* Item 1\n* Item 2"
        val result = MarkdownUtils.parseMarkdown(input)
        // Currently it should NOT format the bullet points, and they should remain as is
        assertEquals("* Item 1\n* Item 2", result.text)
        assertEquals(0, result.paragraphStyles.size)
    }
}
