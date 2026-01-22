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

    if (!isEditable) {
        // 🔹 DISPLAY MODE (whole bar clickable)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onActivate() }
        ) {
            OutlinedTextField(
                value = placeholder,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // 🔑 KEY LINE
                singleLine = true,

                leadingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },


                        shape = MaterialTheme.shapes.large,

                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color(0xFFF1F3F4),
                    disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color(0xFF80868B),
                    disabledLeadingIconColor = Color(0xFF80868B),
                    disabledTrailingIconColor = Color(0xFF80868B),
                    disabledBorderColor = Color.Transparent
                )
            )
        }
    } else {
        // 🔹 EDIT MODE (real search input)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),

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
                IconButton(onClick = onClear) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            },

            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, null)
                }
            },

            shape = MaterialTheme.shapes.large,

            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F3F4),
                unfocusedContainerColor = Color(0xFFF1F3F4),

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                focusedPlaceholderColor = Color(0xFF80868B),
                unfocusedPlaceholderColor = Color(0xFF80868B),

                focusedLeadingIconColor = Color.Black,
                unfocusedLeadingIconColor = Color.Black,

                focusedTrailingIconColor = Color.Black,
                unfocusedTrailingIconColor = Color.Black,

                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,

                cursorColor = Color.Black
            )
        )
    }



}

