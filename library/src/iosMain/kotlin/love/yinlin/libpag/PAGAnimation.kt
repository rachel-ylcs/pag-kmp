package love.yinlin.libpag

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.IntSize
import cocoapods.libpag.*
import kotlinx.cinterop.*
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreGraphics.*
import platform.CoreVideo.*
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
fun PAGFile.Companion.Load(data: ByteArray) = data.usePinned {
    PAGFile.Load(it.addressOf(0), it.get().size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun IntSize.toCGSize() = CGSizeMake(width.toDouble(), height.toDouble())

@OptIn(ExperimentalForeignApi::class)
private class PAGViewListenerImpl(val listener: PAGConfig.AnimationListener) : NSObject(), PAGViewListenerProtocol {
    override fun onAnimationStart(v: PAGView?) = listener.onAnimationStart(v)
    override fun onAnimationEnd(v: PAGView?) = listener.onAnimationEnd(v)
    override fun onAnimationCancel(v: PAGView?) = listener.onAnimationCancel(v)
    override fun onAnimationRepeat(v: PAGView?) = listener.onAnimationRepeat(v)
    override fun onAnimationUpdate(v: PAGView?) = listener.onAnimationUpdate(v, v?.getProgress() ?: 0.0)
}

@OptIn(ExperimentalForeignApi::class)
private class PAGImageViewListenerImpl(val listener: PAGConfig.AnimationListener) : NSObject(), PAGImageViewListenerProtocol {
    override fun onAnimationStart(v: PAGImageView) = listener.onAnimationStart(v)
    override fun onAnimationEnd(v: PAGImageView) = listener.onAnimationEnd(v)
    override fun onAnimationCancel(v: PAGImageView) = listener.onAnimationCancel(v)
    override fun onAnimationRepeat(v: PAGImageView) = listener.onAnimationRepeat(v)
    override fun onAnimationUpdate(v: PAGImageView) =
        listener.onAnimationUpdate(v, v.currentFrame().toDouble() / v.numFrames().toDouble())
}

@OptIn(ExperimentalForeignApi::class)
fun createImageFromPixelBuffer(pixelBuffer: CVPixelBufferRef): Image? {
    CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)

    val width = CVPixelBufferGetWidth(pixelBuffer).toInt()
    val height = CVPixelBufferGetHeight(pixelBuffer).toInt()
    val bytesPerRow = CVPixelBufferGetBytesPerRow(pixelBuffer).toInt()
    val baseAddress = CVPixelBufferGetBaseAddress(pixelBuffer)

    if (baseAddress == null) {
        CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
        return null
    }

    val pixels = ByteArray(height * bytesPerRow)
    memcpy(pixels.refTo(0), baseAddress, (height * bytesPerRow).toULong())

    CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)

    return Image.makeRaster(
        imageInfo = ImageInfo.makeN32Premul(width, height),
        bytes = pixels,
        rowBytes = bytesPerRow
    )
}

@OptIn(ExperimentalForeignApi::class)
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
    val pagView = remember { mutableStateOf<PAGView?>(null) }
    val pagViewListener: MutableState<PAGViewListenerImpl> =
        remember { mutableStateOf(PAGViewListenerImpl(listener)) }

    UIKitView(
        modifier = modifier,
        factory = {
            pagView.value ?: PAGView().apply {
                pagView.value = this
                addListener(pagViewListener.value)
            }
        },
        update = { view ->
            view.setRepeatCount(repeatCount)
            view.setScaleMode(when (scaleMode) {
                PAGConfig.ScaleMode.None -> PAGScaleModeNone
                PAGConfig.ScaleMode.Stretch -> PAGScaleModeStretch
                PAGConfig.ScaleMode.LetterBox -> PAGScaleModeLetterBox
                PAGConfig.ScaleMode.Zoom -> PAGScaleModeZoom
            })
        }
    )

    LaunchedEffect(data) {
        pagView.value?.let { view ->
            view.setComposition(if (data != null) PAGFile.Load(data) else null)
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
            view.setProgress(progress)
            view.flush()
        }
    }

    LaunchedEffect(repeatCount, scaleMode) {
        pagView.value?.let { view ->
            view.setRepeatCount(repeatCount)
            view.setScaleMode(when (scaleMode) {
                PAGConfig.ScaleMode.None -> PAGScaleModeNone
                PAGConfig.ScaleMode.Stretch -> PAGScaleModeStretch
                PAGConfig.ScaleMode.LetterBox -> PAGScaleModeLetterBox
                PAGConfig.ScaleMode.Zoom -> PAGScaleModeZoom
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

@OptIn(ExperimentalForeignApi::class)
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
    val pagImageView = remember { mutableStateOf<PAGImageView?>(null) }
    val pagImageViewListener: MutableState<PAGImageViewListenerImpl> =
        remember { mutableStateOf(PAGImageViewListenerImpl(listener)) }

    UIKitView(
        modifier = modifier,
        factory = {
            pagImageView.value ?: PAGImageView().apply {
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
            view.setComposition(if (data != null) PAGFile.Load(data) else null)
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
            view.setCurrentFrame(min((progress * view.numFrames().toInt()).toULong(), view.numFrames()))
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

@OptIn(ExperimentalForeignApi::class)
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
            player.setComposition(pagFile)
            val size = if (size == IntSize.Zero) IntSize(pagFile.width().toInt(), pagFile.height().toInt()) else size
//            surface.value?.objcPtr()?.let { objc_release(it) }
            surface.value = PAGSurface.MakeOffscreen(size.toCGSize())
            surface.value?.let { surface ->
                player.setSurface(surface)
            }
        }
    }

    LaunchedEffect(progress, surface.value) {
        player.setProgress(progress)
        player.flush()
        surface.value?.getCVPixelBuffer()?.let {
            createImageFromPixelBuffer(it)?.let {
                painter.value = BitmapPainter(it.toComposeImageBitmap())
            }
        }
    }

    // DisposableEffect(Unit) {
    //     onDispose {
    //         surface.value?.objcPtr()?.let { objc_release(it) }
    //         surface.value = null
    //         objc_release(player.objcPtr())
    //     }
    // }

    return painter.value
}
