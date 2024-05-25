package com.imbaland.movies.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.PendingIntentCompat.send
import com.imbaland.movies.R
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieAutoComplete(
    modifier: Modifier = Modifier,
    options: List<String> = listOf(),
    onTextChanged: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectionMade by remember { mutableStateOf(false) }

    val expanded = remember(text) { !selectionMade && text.isNotEmpty() }

    var movieResults by remember(options) { mutableStateOf(options) }
    LaunchedEffect(text) {
        channelFlow {
            snapshotFlow {
                text
            }.collectLatest {
                movieResults = listOf()
                send(it)
            }
        }.debounce(750).distinctUntilChanged().collectLatest {
            if (text.isNotEmpty()) {
                if(!selectionMade) {
                    onTextChanged(it)
                }
            }
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {},
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = text,
            onValueChange = {
                text = it
                selectionMade = false
            },
            leadingIcon = {
                Icon(modifier = Modifier.clickable { onSubmit(text) }, contentDescription = "Confirm", painter = painterResource(
                    R.drawable.ic_confirm), tint = Color.Green)
            }, trailingIcon = {
                Icon(modifier = Modifier.clickable { text = "" }, contentDescription = "Cancel", painter = painterResource(
                    R.drawable.ic_cancel), tint = Color.Red)
            },
            singleLine = false,
            label = { Text("Movie Title") },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            modifier = Modifier.heightIn(0.dp, 150.dp),
            onDismissRequest = { },
        ) {
            if(movieResults.isEmpty()) {
                CircularProgressIndicator()
            }
            movieResults.forEach {
                DropdownMenuItem(
                    modifier = Modifier.padding(vertical = 1.dp).background(Color.DarkGray, RoundedCornerShape(4.dp)),
                    text = { Text(it, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        text = it
                        selectionMade = true
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}