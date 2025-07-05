package love.yinlin.libpag

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.interop.UIKitView
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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PAGAnimation(
    state: PAGAnimationState,
    modifier: Modifier,
) {
    val pagView = remember { mutableStateOf<PAGImageView?>(null) }

    UIKitView(
        modifier = modifier,
        factory = {
            PAGImageView().apply {
                pagView.value = this
                setRepeatCount(state.repeatCount)
//                setScaleMode(when (state.scaleMode) {
//                    PAGConfig.ScaleMode.None -> PAGScaleModeNone
//                    PAGConfig.ScaleMode.Stretch -> PAGScaleModeStretch
//                    PAGConfig.ScaleMode.LetterBox -> PAGScaleModeLetterBox
//                    PAGConfig.ScaleMode.Zoom -> PAGScaleModeZoom
//                })
                setCacheAllFramesInMemory(state.cacheAllFramesInMemory)
            }
        },
        update = { view ->
            view.setRepeatCount(state.repeatCount)
//            view.setScaleMode(when (state.scaleMode) {
//                PAGConfig.ScaleMode.None -> PAGScaleModeNone
//                PAGConfig.ScaleMode.Stretch -> PAGScaleModeStretch
//                PAGConfig.ScaleMode.LetterBox -> PAGScaleModeLetterBox
//                PAGConfig.ScaleMode.Zoom -> PAGScaleModeZoom
//            })
            view.setCacheAllFramesInMemory(state.cacheAllFramesInMemory)
        }
    )

    LaunchedEffect(state.data) {
        pagView.value?.let { view ->
            val pagFile = state.data?.usePinned {
                PAGFile.Load(it.addressOf(0), it.get().size.toULong())
            }
            view.setComposition(pagFile)
        }
    }

    LaunchedEffect(state.isPlaying) {
        pagView.value?.let { view ->
            if (state.isPlaying) view.play()
            else view.pause()
        }
    }

    LaunchedEffect(state.progress) {
        pagView.value?.let { view ->
            view.setCurrentFrame((state.progress * view.numFrames().toDouble()).toULong())
            view.flush()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPAGPainter(
    state: PAGAnimationState,
): Painter {
    val player = remember { PAGPlayer() }
    val surface = remember { mutableStateOf<PAGSurface?>(null) }
    val painter = remember { mutableStateOf<Painter>(BitmapPainter(ImageBitmap(1, 1))) }

    LaunchedEffect(state.data) {
        state.data?.usePinned {
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

    LaunchedEffect(state.progress, surface.value) {
        player.setProgress(state.progress)
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
