package com.jarvis.android.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.android.core.data.Author
import com.jarvis.android.core.data.ChatMessage

private const val MAX_INPUT_CHARS = 1000

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        state = state,
        onSend = viewModel::send,
        onClearHistory = viewModel::clearHistory,
        onSaveAddress = viewModel::setAddressAs,
        onErrorShown = viewModel::dismissError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    state: ChatUiState,
    onSend: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSaveAddress: (String) -> Unit,
    onErrorShown: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    val itemCount = state.messages.size + if (state.isThinking) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    fun submit() {
        if (input.isNotBlank() && !state.isThinking) {
            onSend(input)
            input = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("JARVIS", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Núcleo local · sin Internet · sin IA generativa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Ajustes") },
                                onClick = { menuOpen = false; showSettings = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Borrar historial") },
                                onClick = { menuOpen = false; showClear = true },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    !state.isLoaded -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.messages.isEmpty() -> Text(
                        "Hola, soy JARVIS. Probá con «245 por 18», «qué hora es» o «ayuda».",
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.messages, key = { it.id }) { MessageBubble(it) }
                        if (state.isThinking) {
                            item { Bubble(text = "JARVIS está pensando…", fromUser = false) }
                        }
                    }
                }
            }
            InputBar(
                value = input,
                enabled = !state.isThinking,
                onValueChange = { if (it.length <= MAX_INPUT_CHARS) input = it },
                onSend = ::submit,
            )
        }
    }

    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text("¿Borrar historial?") },
            text = { Text("Se borrarán todos los mensajes guardados en este teléfono. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showClear = false; onClearHistory() }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showClear = false }) { Text("Cancelar") }
            },
        )
    }

    if (showSettings) {
        SettingsDialog(
            initial = state.addressAs,
            onDismiss = { showSettings = false },
            onSave = { showSettings = false; onSaveAddress(it) },
        )
    }
}

@Composable
private fun SettingsDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var draft by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 30) draft = it },
                    label = { Text("Cómo querés que te llame") },
                    placeholder = { Text("Ej.: señor") },
                    singleLine = true,
                )
                Text(
                    "Todo se guarda solo en este teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(draft) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Bubble(text = message.text, fromUser = message.author == Author.USER)
}

@Composable
private fun Bubble(text: String, fromUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (fromUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (fromUser) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribile a JARVIS…") },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
        )
        IconButton(onClick = onSend, enabled = enabled && value.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
        }
    }
}
