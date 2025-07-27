package love.yinlin.libpag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.WebElementView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.toInt8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import kotlin.math.round

val pag = lazy { PAGInit() }

internal fun PAGViewOptions(): PAGViewOptions = js("({})")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun _PAGAnimation(
    data: ByteArray?,
    modifier: Modifier,
    isPlaying: Boolean,
    progress: Double,
    repeatCount: Int,
    scaleMode: PAGConfig.ScaleMode,
    listener: PAGConfig.AnimationListener,
    enableCache: Boolean = false,
    useCanvas2D: Boolean = false,
) {
    val pagCanvas: MutableState<HTMLCanvasElement?> = remember { mutableStateOf(null) }
    val pagView: MutableState<PAGView?> = remember { mutableStateOf(null) }
    val pagViewListener: MutableState<PAGConfig.AnimationListener> =
        remember { mutableStateOf(listener) }

    WebElementView(
        modifier = modifier.onSizeChanged { size ->
            pagCanvas.value?.let {
                it.width = size.width
                it.height = size.height
                it.style.width = "${round(size.width / window.devicePixelRatio)}px"
                it.style.height = "${round(size.height / window.devicePixelRatio)}px"
            }
            pagView.value?.updateSize()
        },
        factory = {
            (document.createElement("canvas") as HTMLCanvasElement).also { canvas ->
                pagCanvas.value = canvas
            }
        },
        update = { view ->
            pagCanvas.value?.let { canvas ->
                canvas.style.setProperty("pointer-events", "none")
                (canvas.parentElement as? HTMLDivElement)?.style?.setProperty("pointer-events", "none")
            }
        },
        onRelease = {
            pagCanvas.value = null
            pagView.value?.destroy()
            pagView.value = null
        }
    )

    LaunchedEffect(data) {
        val buffer = data?.toInt8Array()
        if (buffer == null) {
            return@LaunchedEffect
        }
        val pag = pag.value.await<PAG?>()
        if (pag == null) {
            return@LaunchedEffect
        }
        val file = pag.PAGFile.load(buffer.buffer).await<PAGFile>()
        if (pagView.value == null) {
            val options = PAGViewOptions().apply {
                this.useCanvas2D = useCanvas2D
            }
            pagView.value = pag.PAGView.init(file, pagCanvas.value!!, options).await<PAGView>()
            return@LaunchedEffect
        }
        pagView.value?.setComposition(file)
    }

    LaunchedEffect(pagView.value, listener) {
        pagView.value?.let { view ->
            view.removeListener("onAnimationStart", null)
            view.removeListener("onAnimationEnd", null)
            view.removeListener("onAnimationCancel", null)
            view.removeListener("onAnimationRepeat", null)
            view.removeListener("onAnimationUpdate", null)
            pagViewListener.value = listener
            view.addListener("onAnimationStart", pagViewListener.value::onAnimationStart)
            view.addListener("onAnimationEnd", pagViewListener.value::onAnimationEnd)
            view.addListener("onAnimationCancel", pagViewListener.value::onAnimationCancel)
            view.addListener("onAnimationRepeat", pagViewListener.value::onAnimationRepeat)
            view.addListener("onAnimationUpdate") { v ->
                pagViewListener.value.onAnimationUpdate(v, view.getProgress())
            }
        }
    }

    LaunchedEffect(pagView.value, isPlaying) {
        pagView.value?.let { view ->
            if (isPlaying) view.play()
            else view.pause()
        }
    }

    LaunchedEffect(pagView.value, progress) {
        pagView.value?.let { view ->
            view.setProgress(progress)
            view.flush()
        }
    }

    LaunchedEffect(pagView.value, repeatCount, scaleMode, enableCache) {
        pagView.value?.let { view ->
            view.setRepeatCount(if (repeatCount == PAGConfig.INFINITY) 0 else repeatCount)
            // https://github.com/Tencent/libpag/issues/2948
//            view.setScaleMode(when (scaleMode) {
//                PAGConfig.ScaleMode.None -> 0
//                PAGConfig.ScaleMode.Stretch -> 1
//                PAGConfig.ScaleMode.LetterBox -> 2
//                PAGConfig.ScaleMode.Zoom -> 3
//            })
            view.setCacheEnabled(enableCache)
        }
    }
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
) = _PAGAnimation(
    data = data,
    modifier = modifier,
    isPlaying = isPlaying,
    progress = progress,
    repeatCount = repeatCount,
    scaleMode = scaleMode,
    listener = listener
)

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
) = _PAGAnimation(
    data = data,
    modifier = modifier,
    isPlaying = isPlaying,
    progress = progress,
    repeatCount = repeatCount,
    scaleMode = PAGConfig.ScaleMode.LetterBox,
    listener = listener,
    enableCache = cacheAllFramesInMemory,
    useCanvas2D = true
)

@Composable
actual fun rememberPAGPainter(
    data: ByteArray?,
    size: IntSize,
    progress: Double,
): Painter = TODO()
