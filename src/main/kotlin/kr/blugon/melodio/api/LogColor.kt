package kr.blugon.melodio.api

object LogColor {
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    const val BOLD = "\u001B[1m"

    const val DEFAULT = "\u001B[0m"

    fun String.color(color: String): String {
        return "${color}${this}${DEFAULT}"
    }

    fun String.inColor(text: String): String {
        return "${this}${text}${DEFAULT}"
    }
}