package com.aprilarn.washflow.utils

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun testParseBulletList() {
        val input = "* Item 1\n- Item 2"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("• Item 1\n• Item 2", result.text)
    }

    @Test
    fun testParseHeadings() {
        val input = "# Main Title\n## Section\n### 1. In-App Navigation & Guidance\n#### Sub Detail"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("Main Title\nSection\n1. In-App Navigation & Guidance\nSub Detail", result.text)

        assertTrue(result.spanStyles.any { it.item.fontSize == 19.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.fontSize == 17.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.fontSize == 15.5.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.fontSize == 14.5.sp && it.item.fontWeight == FontWeight.SemiBold })
    }

    @Test
    fun testParseHeadingWithInlineFormatting() {
        val input = "### 1. **Navigasi** & *Panduan*"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("1. Navigasi & Panduan", result.text)
        assertTrue(result.spanStyles.any { it.item.fontSize == 15.5.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun testHashWithoutSpaceIsNotHeading() {
        val input = "Order #123 selesai"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("Order #123 selesai", result.text)
    }

    @Test
    fun testParseBlockquoteAndHorizontalRule() {
        val input = "> Catatan penting\n---"
        val result = MarkdownUtils.parseMarkdown(input)
        assertEquals("▎ Catatan penting\n────────────", result.text)
        assertTrue(result.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    }
}
