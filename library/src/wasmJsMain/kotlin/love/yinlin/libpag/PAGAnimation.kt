package love.yinlin.libpag

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize

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

}

@Composable
actual fun rememberPAGPainter(
    data: ByteArray?,
    size: IntSize,
    progress: Double,
): Painter = TODO()
