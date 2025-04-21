package com.jolotan.unraidapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    dialogText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.background(color = MaterialTheme.colors.background)
                .padding(20.dp)
        ) {
            Text(text = dialogText)
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                modifier = Modifier.fillMaxWidth(),
                buttonText = buttonText,
                onClick = onButtonClick
            )
        }
    }
}