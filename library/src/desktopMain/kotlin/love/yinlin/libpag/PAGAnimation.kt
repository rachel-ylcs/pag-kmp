package love.yinlin.libpag

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.libpag.PAGFile
import org.libpag.PAGPlayer
import org.libpag.PAGSurface

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
    Image(
        painter = rememberPAGPainter(
            data = data,
            progress = progress
        ),
        contentDescription = "",
        modifier = modifier
    )
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
    Image(
        painter = rememberPAGPainter(
            data = data,
            progress = progress
        ),
        contentDescription = "",
        modifier = modifier
    )
}

@Composable
actual fun rememberPAGPainter(
    data: ByteArray?,
    size: IntSize,
    progress: Double,
): Painter {
    val player = remember { PAGPlayer() }
    var surface by remember { mutableStateOf<PAGSurface?>(null) }
    var painter by remember { mutableStateOf<Painter>(BitmapPainter(ImageBitmap(1, 1))) }
    var imageInfo by remember { mutableStateOf(ImageInfo.makeUnknown(0, 0)) }
    var buffer by remember { mutableStateOf(byteArrayOf()) }

    LaunchedEffect(data) {
        if (data == null) return@LaunchedEffect
        PAGFile.Load(data)?.let { pagFile ->
            player.composition = pagFile
            val size = if (size == IntSize.Zero) IntSize(pagFile.width(), pagFile.height()) else size
            imageInfo = ImageInfo(size.width, size.height, ColorType.RGBA_8888, ColorAlphaType.PREMUL, ColorSpace.sRGB)
            buffer = ByteArray(size.width * size.height * 4)
            surface?.release()
            surface = PAGSurface.MakeOffscreen(size.width, size.height)
            surface?.let { surface ->
                player.surface = surface
            }
        }
    }

    LaunchedEffect(progress, surface) {
        player.progress = progress
        player.flush()
        surface?.let {
            if (it.copyPixelsTo(buffer, imageInfo.width * 4)) {
                val image = Image.makeRaster(imageInfo, buffer, imageInfo.width * 4)
                image.use {
                    painter = BitmapPainter(image.toComposeImageBitmap())
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            surface?.release()
            surface = null
            player.release()
        }
    }

    return painter
}
