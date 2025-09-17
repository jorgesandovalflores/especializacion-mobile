package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class DeclarativoActivityRemember : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CounterScreen()
        }
    }

}

@Composable
fun CounterScreen() {
    Scaffold(
        contentWindowInsets = WindowInsets.safeContent
    ) { innerPadding ->
        //CounterMutableState(Modifier.padding(innerPadding))
        CounterState(Modifier.padding(innerPadding))
    }
}

@Composable
fun CounterMutableState(modifier: Modifier = Modifier) {
    val count = remember { mutableStateOf(0) }
    //val count = rememberSaveable { mutableStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Count: ${count.value}")
        Button(onClick = { count.value++ }) {
            Text("Increment")
        }
    }
}

@Composable
fun CounterState(modifier: Modifier = Modifier) {
    //val state = remember { InternalCounterState() }
    val CounterStateSaver: Saver<InternalCounterState, Int> = Saver(
        save = { it.count.value },
        restore = { InternalCounterState(it) }
    )
    val state = rememberSaveable(saver = CounterStateSaver) {
        InternalCounterState()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Count: ${state.count.value}")
        Button(onClick = { state.increment() }) {
            Text("Increment")
        }
    }
}

private class InternalCounterState(initial: Int = 0) {
    private val _count = mutableStateOf(initial)
    val count: State<Int> get() = _count
    fun increment() { _count.value++ }
}

@Composable
@Preview
fun CounterPreview() {
    CounterScreen()
}