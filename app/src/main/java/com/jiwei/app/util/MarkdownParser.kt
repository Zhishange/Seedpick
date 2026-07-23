package com.jiwei.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object MarkdownParser {

    private val header1Style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
    private val header2Style = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    private val header3Style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    private val header4Style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    private val header5Style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    private val header6Style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    private val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
    private val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)
    private val codeStyle = SpanStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontSize = 14.sp
    )
    private val quoteStyle = SpanStyle(color = Color(0xFF666666))
    private val linkStyle = SpanStyle(
        color = Color(0xFF1565C0),
        textDecoration = TextDecoration.Underline
    )

    fun parse(markdown: String): AnnotatedString = buildAnnotatedString {
        val lines = markdown.split("\n")
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("###### ") -> {
                    withStyle(header6Style) { append(line.removePrefix("###### ")) }
                }
                line.startsWith("##### ") -> {
                    withStyle(header5Style) { append(line.removePrefix("##### ")) }
                }
                line.startsWith("#### ") -> {
                    withStyle(header4Style) { append(line.removePrefix("#### ")) }
                }
                line.startsWith("### ") -> {
                    withStyle(header3Style) { append(line.removePrefix("### ")) }
                }
                line.startsWith("## ") -> {
                    withStyle(header2Style) { append(line.removePrefix("## ")) }
                }
                line.startsWith("# ") -> {
                    withStyle(header1Style) { append(line.removePrefix("# ")) }
                }
                line.startsWith("> ") -> {
                    withStyle(quoteStyle) { append(line) }
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("  ")  // bullet indentation
                    appendInlineFormatted(line.removePrefix("- ").removePrefix("* "))
                }
                line.startsWith("```") -> {
                    // Code block markers are shown as-is but in code style
                    withStyle(codeStyle) { append(line) }
                }
                line.isBlank() -> {
                    append(line)
                }
                else -> {
                    appendInlineFormatted(line)
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }

    fun parseInline(content: String): AnnotatedString = buildAnnotatedString {
        appendInlineFormatted(content)
    }

    private fun AnnotatedString.Builder.appendInlineFormatted(text: String) {
        val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
        val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")
        val inlineCodeRegex = Regex("`([^`]+)`")
        val linkRegex = Regex("\\[\\[([^\\]]+)\\]\\]")

        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldMatch = boldRegex.find(remaining)
            val italicMatch = italicRegex.find(remaining)
            val codeMatch = inlineCodeRegex.find(remaining)
            val linkMatch = linkRegex.find(remaining)

            val matches = listOfNotNull(
                boldMatch?.let { "bold" to it },
                italicMatch?.let { "italic" to it },
                codeMatch?.let { "code" to it },
                linkMatch?.let { "link" to it }
            ).sortedBy { it.second.range.first }

            if (matches.isEmpty()) {
                append(remaining)
                break
            }

            val (type, match) = matches.first()
            append(remaining.substring(0, match.range.first))

            when (type) {
                "bold" -> {
                    withStyle(boldStyle) { append(match.groupValues[1]) }
                }
                "italic" -> {
                    withStyle(italicStyle) { append(match.groupValues[1]) }
                }
                "code" -> {
                    withStyle(codeStyle) { append(match.groupValues[1]) }
                }
                "link" -> {
                    val linkText = match.groupValues[1]
                    pushStringAnnotation("bidirectional_link", linkText)
                    withStyle(linkStyle) { append(linkText) }
                    pop()
                }
            }
            remaining = remaining.substring(match.range.last + 1)
        }
    }
}
