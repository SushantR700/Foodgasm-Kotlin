package com.example.foodgasm.utils

import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun EditText.textInputAsFlow() = callbackFlow {
    val watcher: TextWatcher = doAfterTextChanged { input ->
        this.trySend(input).isSuccess
    }
    awaitClose { this@textInputAsFlow.removeTextChangedListener(watcher) }
}