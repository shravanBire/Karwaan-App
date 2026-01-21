package com.example.karwaan.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun TopSearchBar(
    query: String,
    placeholder: String,
    isEditable: Boolean,
    onActivate: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEmpty = query.isBlank()

    LaunchedEffect(isEditable) {
        if (isEditable) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (!isEditable) onActivate()
            }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),


            readOnly = !isEditable,
            singleLine = true,
            placeholder = { Text(placeholder) },

            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch()
                }
            ),

            leadingIcon = {
                if (isEditable || query.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            },

            trailingIcon = {
                IconButton(
                    onClick = {
                        if (!isEditable) onActivate()
                        else onSearch()
                    }
                ) {
                    Icon(Icons.Default.Search, null)
                }
            },

            shape = MaterialTheme.shapes.large,

            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F3F4),
                unfocusedContainerColor = Color(0xFFF1F3F4),

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                focusedPlaceholderColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,
                unfocusedPlaceholderColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,

                focusedLeadingIconColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,
                unfocusedLeadingIconColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,

                focusedTrailingIconColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,
                unfocusedTrailingIconColor =
                if (query.isBlank()) Color(0xFF80868B) else Color.Black,

                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color.Black
            )
        )
    }


}

