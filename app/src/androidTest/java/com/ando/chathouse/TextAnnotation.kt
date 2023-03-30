package com.ando.chathouse

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun Annotation() {
    val text = "1234\\n\\n5678"
    val annotatedString = buildAnnotatedString {
        append(text.replace("\\n", "\n"))
    }
    Column {
        Text(text = text)
        Text(text = annotatedString)
    }
}