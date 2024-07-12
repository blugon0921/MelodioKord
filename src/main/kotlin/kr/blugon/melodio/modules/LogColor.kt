package kr.blugon.melodio.modules

import kr.blugon.melodio.modules.Modules.nowDate
import kr.blugon.melodio.modules.LogColor.Companion.color


val logger = Logger()
class Logger {
    fun log(msg: Any, displayTime: Boolean = true) {
        println("[${nowDate().color(LogColor.GREEN)}] $msg")
    }
}

class LogColor(val colorCode: String) {
    companion object {
        val BLACK = LogColor("\u001B[30m")
        val RED = LogColor("\u001B[31m")
        val GREEN = LogColor("\u001B[32m")
        val YELLOW = LogColor("\u001B[33m")
        val BLUE = LogColor("\u001B[34m")
        val PURPLE = LogColor("\u001B[35m")
        val CYAN = LogColor("\u001B[36m")
        val WHITE = LogColor("\u001B[37m")

        val BOLD = LogColor("\u001B[1m")

        val DEFAULT = LogColor("\u001B[0m")
        fun String.color(color: LogColor): String {
            return "${color.colorCode}${this}${LogColor.DEFAULT.colorCode}"
        }
    }

    fun inColor(text: String): String {
        return "${this.colorCode}${text}${DEFAULT.colorCode}"
    }
}