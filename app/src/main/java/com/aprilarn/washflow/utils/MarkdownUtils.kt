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
import kotlin.text.Regex

object MarkdownUtils {
    /**
     * Parses common markdown formats into AnnotatedString.
     * Supports:
     * - ***bold-italic***
     * - **bold** or __bold__
     * - *italic* or _italic_
     * - `inline code`
     * - ~~strikethrough~~
     * 
     * Refined to avoid matching bullet points (* ) as italic.
     */
    fun parseMarkdown(text: String): AnnotatedString {
        // Pre-process bullet points: replace "* " or "- " at the start of a line with "• "
        val processedText = text.lines().joinToString("\n") { line ->
            val trimmedLine = line.trimStart()
            if (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) {
                val leadingContent = line.takeWhile { it.isWhitespace() }
                val content = trimmedLine.substring(2)
                "$leadingContent• $content"
            } else {
                line
            }
        }

        val pattern = Regex(
            "(\\*\\*\\*\\S[\\s\\S]*?\\S\\*\\*\\*|\\*\\*\\*\\S\\*\\*\\*)|" +
            "(\\*\\*\\S[\\s\\S]*?\\S\\*\\*|\\*\\*\\S\\*\\*)|" +
            "(__\\S[\\s\\S]*?\\S__|__\\S__)|" +
            "(\\*\\S[\\s\\S]*?\\S\\*|\\*\\S\\*)|" +
            "(_\\S[\\s\\S]*?\\S_|(_\\S_))|" +
            "(`[\\s\\S]*?`)|" +
            "(~~\\S[\\s\\S]*?\\S~~|~~\\S~~)"
        )
        
        return buildAnnotatedString {
            var lastIndex = 0
            pattern.findAll(processedText).forEach { matchResult ->
                append(processedText.substring(lastIndex, matchResult.range.first))
                
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
            
            if (lastIndex < processedText.length) {
                append(processedText.substring(lastIndex))
            }
        }
    }
}
