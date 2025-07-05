package love.yinlin.libpag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize

object PAGConfig {
    /** 重复播放 */
    const val INFINITY = -1

    /** 缩放模式 */
    enum class ScaleMode {
        None, Stretch, LetterBox, Zoom
    }

    interface AnimationListener {
        fun onAnimationStart(view: Any?) {}
        fun onAnimationEnd(view: Any?) {}
        fun onAnimationCancel(view: Any?) {}
        fun onAnimationRepeat(view: Any?) {}
        fun onAnimationUpdate(view: Any?, progress: Double) {}
    }

    data class DefaultAnimationListener(
        val onStart: (view: Any?) -> Unit = {},
        val onEnd: (view: Any?) -> Unit = {},
        val onCancel: (view: Any?) -> Unit = {},
        val onRepeat: (view: Any?) -> Unit = {},
        val onUpdate: (view: Any?, progress: Double) -> Unit = { _, _ -> },
    ) : AnimationListener {
        override fun onAnimationStart(view: Any?) { onStart(view) }
        override fun onAnimationEnd(view: Any?) { onEnd(view) }
        override fun onAnimationCancel(view: Any?) { onCancel(view) }
        override fun onAnimationRepeat(view: Any?) { onRepeat(view) }
        override fun onAnimationUpdate(view: Any?, progress: Double) { onUpdate(view, progress) }
    }

    @Composable
    fun rememberAnimationListener(
        onStart: (view: Any?) -> Unit = {},
        onEnd: (view: Any?) -> Unit = {},
        onCancel: (view: Any?) -> Unit = {},
        onRepeat: (view: Any?) -> Unit = {},
        onUpdate: (view: Any?, progress: Double) -> Unit = { _, _ -> },
    ) = remember {
        DefaultAnimationListener(onStart, onEnd, onCancel, onRepeat, onUpdate)
    }
}

/**
 * 原生 PAGView 控件封装, 每个 PAGAnimation 都会创建一个 GPU 渲染上下文
 *
 * 适用于只渲染单个 pag 动画的场景
 *
 * @param data 动画源
 * @param modifier
 * @param isPlaying 是否正在播放
 * @param progress 播放进度 (0~1)
 * @param repeatCount 重复次数
 * @param scaleMode 缩放模式
 * @param listener 动画事件回调
 */
@Composable
expect fun PAGAnimation(
    data: ByteArray?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    progress: Double = 0.0,
    repeatCount: Int = PAGConfig.INFINITY,
    scaleMode: PAGConfig.ScaleMode = PAGConfig.ScaleMode.LetterBox,
    listener: PAGConfig.AnimationListener = PAGConfig.rememberAnimationListener(),
)

/**
 * 原生 PAGImageView 控件封装, 会将渲染结果缓存到磁盘或内存中, 渲染完毕或缓存命中则销毁渲染上下文, 直接绘制缓存
 *
 * 适合列表等页面中含有多个 pag 动画同时渲染的场景
 *
 * @param data 动画源
 * @param modifier
 * @param isPlaying 是否正在播放
 * @param progress 播放进度 (0~1)
 * @param repeatCount 重复次数
 * @param renderScale 缩放比例
 * @param cacheAllFramesInMemory 在内存中缓存所有帧 (会极大增加内存占用量, 仅在必要时开启)
 * @param listener 动画事件回调
 */
@Composable
expect fun PAGImageAnimation(
    data: ByteArray?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    progress: Double = 0.0,
    repeatCount: Int = PAGConfig.INFINITY,
    renderScale: Float = 1.0f,
    cacheAllFramesInMemory: Boolean = false,
    listener: PAGConfig.AnimationListener = PAGConfig.rememberAnimationListener(),
)

/**
 * 将 pag 动画无头渲染成位图, 并使用 Compose 的 Image 绘制
 *
 * 暂时用于解决使用 skia 渲染的平台在和原生视图互作时的打孔问题, 但为纯 cpu 渲染, 性能较差
 *
 * @param data 动画源
 * @param size 位图尺寸, IntSize.Zero 表示使用 pag 动画文件中的默认尺寸
 * @param progress 播放进度 (0~1)
 * @return 用于在 Image 中渲染的 pag 动画 Painter
 */
@Composable
expect fun rememberPAGPainter(
    data: ByteArray?,
    size: IntSize = IntSize.Zero,
    progress: Double = 0.0,
) : Painter
