package love.yinlin.libpag.example

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "libpag-compose example",
    ) {
        PAGApp()
    }
}
