package love.yinlin.libpag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

@Composable
actual fun PAGAnimation(
    state: PAGAnimationState,
    modifier: Modifier,
) {

}

@Composable
actual fun rememberPAGPainter(
    state: PAGAnimationState,
): Painter = TODO()
