package love.yinlin.libpag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

object PAGConfig {
    enum class ScaleMode {
        None, Stretch, LetterBox, Zoom
    }

    const val INFINITY = -1
}

/**
 * PAG 动画状态
 */
@Stable
class PAGAnimationState {
    /** 动画源 */
    var data: ByteArray? by mutableStateOf(null)
    /** 是否正在播放 */
    var isPlaying: Boolean by mutableStateOf(false)
    /** 是否播放完毕 */
    var isCompleted: Boolean by mutableStateOf(false)
    /** 播放进度 (0~1) */
    var progress: Double by mutableStateOf(0.0)
    /** 重复次数 */
    var repeatCount: Int by mutableStateOf(PAGConfig.INFINITY)
    /** 缩放比例 */
    var renderScale: Float by mutableStateOf(1.0f)
    /** 缩放模式 */
    var scaleMode: PAGConfig.ScaleMode by mutableStateOf(PAGConfig.ScaleMode.LetterBox)
    /** 在内存中缓存所有帧 (会极大增加内存占用量, 请谨慎开启) */
    var cacheAllFramesInMemory: Boolean by mutableStateOf(false)
}

/**
 * PAG 动画控件
 *
 * @param state 动画状态
 * @param modifier
 */
@Composable
expect fun PAGAnimation(
    state: PAGAnimationState,
    modifier: Modifier = Modifier,
)

/**
 * PAG 位图动画
 *
 * @param state 动画状态
 * @return
 */
@Composable
expect fun rememberPAGPainter(
    state: PAGAnimationState,
) : Painter
