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
import platform.posix.memcpy

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

    UIKitView(
        modifier = modifier,
        factory = {
            pagView.value ?: PAGView().apply {
                pagView.value = this
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

    UIKitView(
        modifier = modifier,
        factory = {
            pagImageView.value ?: PAGImageView().apply {
                pagImageView.value = this
            }
        },
        update = { view ->
            view.setRepeatCount(repeatCount)
            view.setCacheAllFramesInMemory(cacheAllFramesInMemory)
        }
    )

    LaunchedEffect(data) {
        pagImageView.value?.let { view ->
            val pagFile = data?.usePinned {
                PAGFile.Load(it.addressOf(0), it.get().size.toULong())
            }
            view.setComposition(pagFile)
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
            view.setCurrentFrame((progress * view.numFrames().toDouble()).toULong())
            view.flush()
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
        data?.usePinned {
            PAGFile.Load(it.addressOf(0), it.get().size.toULong())
        }?.let { pagFile ->
            player.setComposition(pagFile)
            val width = pagFile.width().toDouble()
            val height = pagFile.height().toDouble()
//            surface.value?.release()
            surface.value = PAGSurface.MakeOffscreen(CGSizeMake(width, height))
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

    DisposableEffect(player, surface.value) {
        onDispose {
//            surface.value?.release()
//            player.release()
        }
    }

    return painter.value
}
