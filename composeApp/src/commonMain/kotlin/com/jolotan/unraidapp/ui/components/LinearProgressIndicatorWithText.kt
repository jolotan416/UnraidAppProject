package com.jolotan.unraidapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LinearProgressIndicatorWithText(
    progress: () -> Float,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Right
        )
        LinearProgressIndicator(
            modifier = Modifier.width(150.dp).fillMaxWidth(),
            progress = progress,
            trackColor = MaterialTheme.colorScheme.inversePrimary,
            color = MaterialTheme.colorScheme.primary,
            gapSize = (-12).dp,
            drawStopIndicator = {}
        )
    }
}

@Preview
@Composable
fun LinearProgressIndicatorWithTextPreview() {
    LinearProgressIndicatorWithText(
        progress = { 0.65f },
        text = "650GB used out of 1TB"
    )
}
