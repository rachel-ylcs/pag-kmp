package love.yinlin.libpag

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.viewinterop.AndroidView
import org.libpag.PAGFile
import org.libpag.PAGImageView
import org.libpag.PAGImageView.PAGImageViewListener
import org.libpag.PAGPlayer
import org.libpag.PAGScaleMode
import org.libpag.PAGSurface

@Composable
actual fun PAGAnimation(
    state: PAGAnimationState,
    modifier: Modifier,
) {
    val imageView: MutableState<PAGImageView?> = remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            imageView.value ?: PAGImageView(context).apply {
                imageView.value = this
                addListener(object : PAGImageViewListener {
                    override fun onAnimationStart(v: PAGImageView) {
                        state.isCompleted = false
                    }

                    override fun onAnimationEnd(v: PAGImageView) {
                        state.isCompleted = true
                    }

                    override fun onAnimationCancel(v: PAGImageView) {}

                    override fun onAnimationRepeat(v: PAGImageView) {}

                    override fun onAnimationUpdate(v: PAGImageView) {
//                        state.progress = v.currentFrame().toDouble() / v.numFrames()
                    }
                })
            }
        },
        update = { view ->
            view.setRepeatCount(state.repeatCount)
            view.setRenderScale(state.renderScale)
            view.setScaleMode(when (state.scaleMode) {
                PAGConfig.ScaleMode.None -> PAGScaleMode.None
                PAGConfig.ScaleMode.Stretch -> PAGScaleMode.Stretch
                PAGConfig.ScaleMode.LetterBox -> PAGScaleMode.LetterBox
                PAGConfig.ScaleMode.Zoom -> PAGScaleMode.Zoom
            })
            view.setCacheAllFramesInMemory(state.cacheAllFramesInMemory)
        }
    )

    LaunchedEffect(state.data) {
        imageView.value?.let { view ->
            view.composition = PAGFile.Load(state.data)
        }
    }

    LaunchedEffect(state.isPlaying) {
        imageView.value?.let { view ->
            if (state.isPlaying) view.play()
            else view.pause()
        }
    }

    LaunchedEffect(state.progress) {
        imageView.value?.let { view ->
            view.setCurrentFrame((state.progress * view.numFrames()).toInt())
            view.flush()
        }
    }
}

@Composable
actual fun rememberPAGPainter(
    state: PAGAnimationState,
): Painter {
    val player = remember { PAGPlayer() }
    val surface = remember { mutableStateOf<PAGSurface?>(null) }
    val painter = remember { mutableStateOf<Painter>(BitmapPainter(ImageBitmap(1, 1))) }

    LaunchedEffect(state.data) {
        PAGFile.Load(state.data)?.let { pagFile ->
            player.composition = pagFile
            val width = pagFile.width()
            val height = pagFile.height()
            surface.value?.release()
            surface.value = PAGSurface.MakeOffscreen(width, height)
            surface.value?.let { surface ->
                player.surface = surface
            }
        }
    }

    LaunchedEffect(state.progress, surface.value) {
        player.progress = state.progress
        player.flush()
        surface.value?.makeSnapshot()?.let {
            painter.value = BitmapPainter(it.asImageBitmap())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            surface.value?.release()
            player.release()
        }
    }

    return painter.value
}
