package kr.blugon.melodio.api

object LogColor {
    val BLACK = "\u001B[30m"
    val RED = "\u001B[31m"
    val GREEN = "\u001B[32m"
    val YELLOW = "\u001B[33m"
    val BLUE = "\u001B[34m"
    val PURPLE = "\u001B[35m"
    val CYAN = "\u001B[36m"
    val WHITE = "\u001B[37m"

    val BOLD = "\u001B[1m"

    val DEFAULT = "\u001B[0m"

    fun String.color(color: String): String {
        return "${color}${this}${DEFAULT}"
    }

    fun String.inColor(text: String): String {
        return "${this}${text}${DEFAULT}"
    }
}