package love.yinlin.libpag.example

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import libpag_compose.example.generated.resources.Res
import love.yinlin.libpag.PAGAnimation
import love.yinlin.libpag.PAGAnimationState
import love.yinlin.libpag.rememberPAGPainter

@Composable
fun PAGApp() {
    val pagState = remember { PAGAnimationState() }
    var usePainter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        pagState.data = Res.readBytes("files/test.pag")
        pagState.isPlaying = true

        while (true) {
            delay(1000 / 60)
            if (pagState.isPlaying) {
                pagState.progress = (pagState.progress + 1 / 174.0)
                if (pagState.progress > 1.0) pagState.progress = 0.0
            }
        }
    }

    LaunchedEffect(pagState.isCompleted) {
        if (pagState.isCompleted) {
            println("PAG 动画播放完毕")
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Green),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (!usePainter) {
            PAGAnimation(
                state = pagState,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = rememberPAGPainter(state = pagState),
                contentDescription = "",
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Test PAG Animation")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    pagState.isPlaying = !pagState.isPlaying
                }) {
                    Text(text = if (pagState.isPlaying) "Pause" else "Play")
                }

                Button(onClick = {
                    usePainter = !usePainter
                }) {
                    Text(text = "Switch Render")
                }
            }
        }
    }
}
