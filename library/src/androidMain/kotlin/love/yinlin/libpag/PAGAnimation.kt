package love.yinlin.libpag

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import org.libpag.PAGFile
import org.libpag.PAGImageView
import org.libpag.PAGImageView.PAGImageViewListener
import org.libpag.PAGPlayer
import org.libpag.PAGScaleMode
import org.libpag.PAGSurface
import org.libpag.PAGView
import org.libpag.PAGView.PAGViewListener
import kotlin.math.min

private class PAGViewListenerImpl(val listener: PAGConfig.AnimationListener) : PAGViewListener {
    override fun onAnimationStart(v: PAGView) = listener.onAnimationStart(v)
    override fun onAnimationEnd(v: PAGView) = listener.onAnimationEnd(v)
    override fun onAnimationCancel(v: PAGView) = listener.onAnimationCancel(v)
    override fun onAnimationRepeat(v: PAGView) = listener.onAnimationRepeat(v)
    override fun onAnimationUpdate(v: PAGView) = listener.onAnimationUpdate(v, v.progress)
}

private class PAGImageViewListenerImpl(val listener: PAGConfig.AnimationListener) : PAGImageViewListener {
    override fun onAnimationStart(v: PAGImageView) = listener.onAnimationStart(v)
    override fun onAnimationEnd(v: PAGImageView) = listener.onAnimationEnd(v)
    override fun onAnimationCancel(v: PAGImageView) = listener.onAnimationCancel(v)
    override fun onAnimationRepeat(v: PAGImageView) = listener.onAnimationRepeat(v)
    override fun onAnimationUpdate(v: PAGImageView) =
        listener.onAnimationUpdate(v, v.currentFrame().toDouble() / v.numFrames())
}

@Composable
actual fun PAGAnimation(
    data: ByteArray?,
    modifier: Modifier,
    isPlaying: Boolean,
    progress: Double,
    repeatCount: Int,
    scaleMode: PAGConfig.ScaleMode,
    listener: PAGConfig.AnimationListener,
) {
    val pagView: MutableState<PAGView?> = remember { mutableStateOf(null) }
    val pagViewListener: MutableState<PAGViewListenerImpl> =
        remember { mutableStateOf(PAGViewListenerImpl(listener)) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            pagView.value ?: PAGView(context).apply {
                pagView.value = this
                addListener(pagViewListener.value)
            }
        },
        update = { view ->
            view.setRepeatCount(repeatCount)
            view.setScaleMode(when (scaleMode) {
                PAGConfig.ScaleMode.None -> PAGScaleMode.None
                PAGConfig.ScaleMode.Stretch -> PAGScaleMode.Stretch
                PAGConfig.ScaleMode.LetterBox -> PAGScaleMode.LetterBox
                PAGConfig.ScaleMode.Zoom -> PAGScaleMode.Zoom
            })
        }
    )

    LaunchedEffect(data) {
        pagView.value?.let { view ->
            view.composition = if (data != null) PAGFile.Load(data) else null
        }
    }

    LaunchedEffect(isPlaying) {
        pagView.value?.let { view ->
            if (isPlaying) view.play()
            else view.pause()
        }
    }

    LaunchedEffect(progress) {
        pagView.value?.let { view ->
            view.progress = progress
            view.flush()
        }
    }

    LaunchedEffect(repeatCount, scaleMode) {
        pagView.value?.let { view ->
            view.setRepeatCount(repeatCount)
            view.setScaleMode(when (scaleMode) {
                PAGConfig.ScaleMode.None -> PAGScaleMode.None
                PAGConfig.ScaleMode.Stretch -> PAGScaleMode.Stretch
                PAGConfig.ScaleMode.LetterBox -> PAGScaleMode.LetterBox
                PAGConfig.ScaleMode.Zoom -> PAGScaleMode.Zoom
            })
        }
    }

    LaunchedEffect(listener) {
        if (listener == pagViewListener.value.listener) {
            return@LaunchedEffect
        }
        pagView.value?.let { view ->
            view.removeListener(pagViewListener.value)
            pagViewListener.value = PAGViewListenerImpl(listener)
            view.addListener(pagViewListener.value)
        }
    }
}

@Composable
actual fun PAGImageAnimation(
    data: ByteArray?,
    modifier: Modifier,
    isPlaying: Boolean,
    progress: Double,
    repeatCount: Int,
    renderScale: Float,
    cacheAllFramesInMemory: Boolean,
    listener: PAGConfig.AnimationListener,
) {
    val pagImageView: MutableState<PAGImageView?> = remember { mutableStateOf(null) }
    val pagImageViewListener: MutableState<PAGImageViewListenerImpl> =
        remember { mutableStateOf(PAGImageViewListenerImpl(listener)) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            pagImageView.value ?: PAGImageView(context).apply {
                pagImageView.value = this
                addListener(pagImageViewListener.value)
            }
        },
        update = { view ->
            view.setRepeatCount(repeatCount)
            view.setRenderScale(renderScale)
            view.setCacheAllFramesInMemory(cacheAllFramesInMemory)
        }
    )

    LaunchedEffect(data) {
        pagImageView.value?.let { view ->
            view.composition = if (data != null) PAGFile.Load(data) else null
        }
    }

    LaunchedEffect(isPlaying) {
        pagImageView.value?.let { view ->
            if (isPlaying) view.play()
            else view.pause()
        }
    }

    LaunchedEffect(progress) {
        pagImageView.value?.let { view ->
            view.setCurrentFrame(min((progress * view.numFrames()).toInt(), view.numFrames()))
            view.flush()
        }
    }

    LaunchedEffect(repeatCount, renderScale, cacheAllFramesInMemory) {
        pagImageView.value?.let { view ->
            view.setRepeatCount(repeatCount)
            view.setRenderScale(renderScale)
            view.setCacheAllFramesInMemory(cacheAllFramesInMemory)
        }
    }

    LaunchedEffect(listener) {
        if (listener == pagImageViewListener.value.listener) {
            return@LaunchedEffect
        }
        pagImageView.value?.let { view ->
            view.removeListener(pagImageViewListener.value)
            pagImageViewListener.value = PAGImageViewListenerImpl(listener)
            view.addListener(pagImageViewListener.value)
        }
    }
}

@Composable
actual fun rememberPAGPainter(
    data: ByteArray?,
    size: IntSize,
    progress: Double,
): Painter {
    val player = remember { PAGPlayer() }
    val surface = remember { mutableStateOf<PAGSurface?>(null) }
    val painter = remember { mutableStateOf<Painter>(BitmapPainter(ImageBitmap(1, 1))) }

    LaunchedEffect(data) {
        if (data == null) return@LaunchedEffect
        PAGFile.Load(data)?.let { pagFile ->
            player.composition = pagFile
            val size = if (size == IntSize.Zero) IntSize(pagFile.width(), pagFile.height()) else size
            surface.value?.release()
            surface.value = PAGSurface.MakeOffscreen(size.width, size.height)
            surface.value?.let { surface ->
                player.surface = surface
            }
        }
    }

    LaunchedEffect(progress, surface.value) {
        player.progress = progress
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
