package kr.blugon.melodio.modules

import kr.blugon.melodio.modules.Modules.nowDate


object Logger {
    fun log(msg: Any, displayTime: Boolean = true) {
        println("${if(displayTime) "[${nowDate().color(LogColor.Green)}] " else ""}$msg")
    }

    fun error(msg: Any, displayTime: Boolean = true) {
        println("${if(displayTime) "[${nowDate().color(LogColor.Green)}] " else ""}${LogColor.Red.colorCode}$msg")
    }
}

fun String.color(color: LogColor): String {
    return "${color.colorCode}${this}${LogColor.Reset.colorCode}"
}
enum class LogColor(val colorCode: String) {
    Black("\u001B[30m"),
    Red("\u001B[31m"),
    Green("\u001B[32m"),
    Yellow("\u001B[33m"),
    Blue("\u001B[34m"),
    Purple("\u001B[35m"),
    Cyan("\u001B[36m"),
    White("\u001B[37m"),

    Bold("\u001B[1m"),

    Reset("\u001B[0m");

    fun inColor(text: String): String {
        return "${this.colorCode}${text}${Reset.colorCode}"
    }
}