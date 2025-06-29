package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun CounterScreen() {
    val container = remember { CounterViewModel().container }
    val snackBar = remember { SnackbarHostState() }
    with(container.store) {
        LaunchedEffect(this) { start(this).awaitUntilClosed() }
        val state by subscribe()
        // Collect actions for showing messages
        LaunchedEffect(Unit) {
            container.actions.collectLatest { action ->
                when (action) {
                    is CounterAction.ShowMessage -> snackBar.showSnackbar(action.message)
                }
            }
        }
        CounterScreenContent(
            state = state,
            snackbarHostState = snackBar,
        )
    }
}

@Composable
private fun IntentReceiver<CounterIntent>.CounterScreenContent(
    state: CounterState,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is CounterState.Loading -> CircularProgressIndicator()
                is CounterState.Error -> ErrorView(state.error.message ?: "Unknown error")
                is CounterState.Content -> CounterContent(state.counter.value)
            }
        }
    }
}

@Composable
private fun IntentReceiver<CounterIntent>.CounterContent(counter: Int) {
    Column(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .padding(32.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        CounterValueView(counter)
        CounterButtonRow()
    }
}

@Composable
private fun CounterValueView(counter: Int) {
    Text(
        text = "Current Value",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = "$counter",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 56.sp
    )
}

@Composable
private fun IntentReceiver<CounterIntent>.CounterButtonRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Button(
            onClick = { intent(CounterIntent.ClickedIncrement) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increment")
            Spacer(Modifier.width(8.dp))
            Text(text = "Increment")
        }
        OutlinedButton(
            onClick = { intent(CounterIntent.ClickedReset) },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
            Spacer(Modifier.width(8.dp))
            Text(text = "Reset")
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Preview
@Composable
fun PreviewCounterScreen() {
    CounterScreen()
}