package kr.blugon.melodio.modules

import kr.blugon.melodio.modules.Modules.nowDate


val logger = Logger()
class Logger {
    fun log(msg: Any, displayTime: Boolean = true) {
        println("[${nowDate().color(LogColor.GREEN)}] $msg")
    }
}

fun String.color(color: LogColor): String {
    return "${color.colorCode}${this}${LogColor.DEFAULT.colorCode}"
}
enum class LogColor(val colorCode: String) {
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),

    BOLD("\u001B[1m"),

    DEFAULT("\u001B[0m");

    fun inColor(text: String): String {
        return "${this.colorCode}${text}${DEFAULT.colorCode}"
    }
}