package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.sample.ui.theme.CodeBackground
import com.mohamedrejeb.calf.sample.ui.theme.CodeCall
import com.mohamedrejeb.calf.sample.ui.theme.CodeComment
import com.mohamedrejeb.calf.sample.ui.theme.CodeKeyword
import com.mohamedrejeb.calf.sample.ui.theme.CodeNumber
import com.mohamedrejeb.calf.sample.ui.theme.CodeString
import com.mohamedrejeb.calf.sample.ui.theme.CodeText
import com.mohamedrejeb.calf.sample.ui.theme.Spacing

private val CodeFontSize = 12.5.sp
private val CodeLineHeight = 19.sp

/**
 * The few lines of Kotlin a demo is about, on a dark card in both themes. Long lines scroll
 * sideways instead of wrapping. Pass the code with `trimIndent()`.
 */
@Composable
fun CodeSnippet(
    code: String,
    modifier: Modifier = Modifier,
) {
    val highlighted = remember(code) { highlightKotlin(code, SnippetColors) }
    Surface(
        color = CodeBackground,
        contentColor = CodeText,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = highlighted,
            fontFamily = FontFamily.Monospace,
            fontSize = CodeFontSize,
            lineHeight = CodeLineHeight,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(Spacing.l),
        )
    }
}

/** The colours of each kind of token in [highlightKotlin]. */
data class CodeColors(
    val keyword: Color,
    val string: Color,
    val call: Color,
    val comment: Color,
    val number: Color,
)

private val SnippetColors = CodeColors(
    keyword = CodeKeyword,
    string = CodeString,
    call = CodeCall,
    comment = CodeComment,
    number = CodeNumber,
)

private val KotlinKeywords = setOf(
    "val", "var", "fun", "if", "else", "when", "true", "false", "null", "by", "return",
    "private", "object", "class", "is", "in", "import", "for", "it",
)

// Groups: 1 comment, 2 string, 3 number, 4 word.
private val TokenRegex = Regex("""(//[^\n]*)|("(?:[^"\\]|\\.)*")|(\b\d+(?:\.\d+)?[fL]?\b)|([A-Za-z_][A-Za-z0-9_]*)""")

/**
 * A small Kotlin highlighter, enough for a demo's snippet: comments, strings, numbers, keywords,
 * and calls (a name followed by `(` or `{`, or any capitalised name). Everything else keeps the
 * text colour.
 */
fun highlightKotlin(code: String, colors: CodeColors): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    TokenRegex.findAll(code).forEach { match ->
        append(code.substring(cursor, match.range.first))
        val color = tokenColor(match, code, colors)
        if (color == null) {
            append(match.value)
        } else {
            withStyle(SpanStyle(color = color)) { append(match.value) }
        }
        cursor = match.range.last + 1
    }
    append(code.substring(cursor))
}

private fun tokenColor(match: MatchResult, code: String, colors: CodeColors): Color? {
    val groups = match.groups
    return when {
        groups[1] != null -> colors.comment
        groups[2] != null -> colors.string
        groups[3] != null -> colors.number
        match.value in KotlinKeywords -> colors.keyword
        match.value.first().isUpperCase() || code.nextNonBlankAfter(match.range.last) in CallOpeners -> colors.call
        else -> null
    }
}

private val CallOpeners = setOf('(', '{')

private fun String.nextNonBlankAfter(index: Int): Char? {
    var next = index + 1
    while (next < length && this[next] == ' ') next++
    return getOrNull(next)
}
