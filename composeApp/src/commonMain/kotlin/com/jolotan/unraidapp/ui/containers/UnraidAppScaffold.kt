package com.jolotan.unraidapp.ui.containers

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.back_button_content_description


@Composable
fun UnraidAppScaffold(navigateUp: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(
                onClick = navigateUp,
            ) {
                Image(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        Res.string.back_button_content_description
                    )
                )
            }
        })
    }) {
        content()
    }
}