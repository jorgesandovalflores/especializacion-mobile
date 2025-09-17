package com.example.example.declarativo

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.viewinterop.AndroidView

import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.tooling.preview.Preview

class DeclarativoActivityAndroidView : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VolumeControl()
        }
    }

}

@Composable
fun VolumeControl() {
    var progress by rememberSaveable { mutableStateOf(25) }

    Column (modifier = Modifier.padding(16.dp)) {
        Text(text = "Volume: $progress")

        AndroidView(
            factory = { context ->
                // Crear el View clásico
                SeekBar(context).apply {
                    max = 100

                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            value: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                progress = value
                            }
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) { /* no-op */ }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) { /* no-op */ }
                    })
                }
            },
            update = { view ->
                if (view.progress != progress) {
                    view.progress = progress
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@Preview
fun VolumeControlPreview() {
    VolumeControl()
}