package kr.blugon.melodio

import dev.kord.core.Kord
import dev.kord.core.kordLogger
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.api.Command
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor

suspend fun main(args: Array<String>) {
    val bot = Kord(Settings.TOKEN)
//    val bot = Kord(Settings.TEST_TOKEN)

    val rootPackage = Main.javaClass.`package`

    val commands = ArrayList<Command>()
    rootPackage.classes("commands").forEach { clazz ->
        try {
            val instance = clazz.getDeclaredConstructor().newInstance()
            commands.add((instance as Command))
        } catch (e: Exception) {
            return@forEach
        }
    }
    for(command in commands) {
        command.deploy(bot)
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command.command)} 커맨드 등록 완료")
    }
}