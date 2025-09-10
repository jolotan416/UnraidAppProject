package com.jolotan.unraidapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.then(Modifier.size(16.dp)),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = buttonText)
            }
        }
    }
}