package com.aprilarn.washflow.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object MarkdownUtils {
    private val HEADING_REGEX = Regex("^\\s*(#{1,6})\\s+(.*)$")
    private val HR_REGEX = Regex("^\\s*([-*_])\\s*\\1\\s*\\1(?:\\s|\\1)*$")
    private val QUOTE_REGEX = Regex("^\\s*>\\s?(.*)$")
    private val INLINE_PATTERN = Regex(
        "(\\*\\*\\*\\S[\\s\\S]*?\\S\\*\\*\\*|\\*\\*\\*\\S\\*\\*\\*)|" +
        "(\\*\\*\\S[\\s\\S]*?\\S\\*\\*|\\*\\*\\S\\*\\*)|" +
        "(__\\S[\\s\\S]*?\\S__|__\\S__)|" +
        "(\\*\\S[\\s\\S]*?\\S\\*|\\*\\S\\*)|" +
        "(_\\S[\\s\\S]*?\\S_|(_\\S_))|" +
        "(`[\\s\\S]*?`)|" +
        "(~~\\S[\\s\\S]*?\\S~~|~~\\S~~)"
    )

    private val QUOTE_ACCENT_COLOR = Color(0xFF566B92)
    private val QUOTE_TEXT_COLOR = Color(0xFF475569)
    private val HR_COLOR = Color(0xFF94A3B8)

    /**
     * Parses common markdown formats into AnnotatedString.
     * Supports:
     * - Headings (# through ######)
     * - Bullet lists (* or - at line start -> • )
     * - Blockquotes (> quote)
     * - Horizontal rules (---, ***, ___)
     * - ***bold-italic***
     * - **bold** or __bold__
     * - *italic* or _italic_
     * - `inline code`
     * - ~~strikethrough~~
     */
    fun parseMarkdown(text: String): AnnotatedString {
        val lines = text.lines()
        return buildAnnotatedString {
            lines.forEachIndexed { index, line ->
                if (index > 0) append("\n")

                val headingMatch = HEADING_REGEX.matchEntire(line)
                when {
                    headingMatch != null -> {
                        val level = headingMatch.groupValues[1].length
                        val content = headingMatch.groupValues[2].trimEnd()
                        val headingStyle = when (level) {
                            1 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = (-0.2).sp)
                            2 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = (-0.15).sp)
                            3 -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.5.sp, letterSpacing = (-0.1).sp)
                            else -> SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                        }
                        withStyle(headingStyle) {
                            appendInlineMarkdown(content)
                        }
                    }

                    HR_REGEX.matches(line) -> {
                        withStyle(SpanStyle(color = HR_COLOR)) {
                            append("────────────")
                        }
                    }

                    QUOTE_REGEX.matches(line) -> {
                        val content = QUOTE_REGEX.matchEntire(line)?.groupValues?.get(1).orEmpty()
                        withStyle(SpanStyle(color = QUOTE_ACCENT_COLOR, fontWeight = FontWeight.Bold)) {
                            append("▎ ")
                        }
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = QUOTE_TEXT_COLOR)) {
                            appendInlineMarkdown(content)
                        }
                    }

                    else -> {
                        val trimmedLine = line.trimStart()
                        val processedLine = if (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) {
                            val leadingWhitespace = line.takeWhile { it.isWhitespace() }
                            "$leadingWhitespace• ${trimmedLine.substring(2)}"
                        } else {
                            line
                        }
                        appendInlineMarkdown(processedLine)
                    }
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendInlineMarkdown(input: String) {
        var lastIndex = 0
        INLINE_PATTERN.findAll(input).forEach { matchResult ->
            append(input.substring(lastIndex, matchResult.range.first))

            val matchValue = matchResult.value
            when {
                matchValue.startsWith("***") && matchValue.endsWith("***") && matchValue.length >= 7 -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(matchValue.substring(3, matchValue.length - 3))
                    }
                }
                (matchValue.startsWith("**") && matchValue.endsWith("**") && matchValue.length >= 5) ||
                (matchValue.startsWith("__") && matchValue.endsWith("__") && matchValue.length >= 5) -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchValue.substring(2, matchValue.length - 2))
                    }
                }
                (matchValue.startsWith("*") && matchValue.endsWith("*") && matchValue.length >= 3) ||
                (matchValue.startsWith("_") && matchValue.endsWith("_") && matchValue.length >= 3) -> {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchValue.substring(1, matchValue.length - 1))
                    }
                }
                matchValue.startsWith("`") && matchValue.endsWith("`") && matchValue.length >= 3 -> {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.LightGray.copy(alpha = 0.3f),
                            color = Color(0xFFD32F2F)
                        )
                    ) {
                        append(matchValue.substring(1, matchValue.length - 1))
                    }
                }
                matchValue.startsWith("~~") && matchValue.endsWith("~~") && matchValue.length >= 5 -> {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(matchValue.substring(2, matchValue.length - 2))
                    }
                }
                else -> append(matchValue)
            }

            lastIndex = matchResult.range.last + 1
        }

        if (lastIndex < input.length) {
            append(input.substring(lastIndex))
        }
    }
}
