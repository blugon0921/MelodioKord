package kr.blugon.melodio

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.Command.Companion.registerGlobalCommand
import kr.blugon.kordmand.Command.Companion.registerGuildCommand
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.logger

suspend fun main(args: Array<String>) {
    val bot = if (args[0] == "test") Kord(Settings.TEST_TOKEN)
               else Kord(Settings.TOKEN)

    val rootPackage = Main.javaClass.`package`

    val commands = ArrayList<Command>()
    rootPackage.classes<Command>("commands").forEach { clazz ->
        try {
            val instance = clazz.getDeclaredConstructor().newInstance()
            commands.add(instance as Command)
        } catch (e: Exception) {
            return@forEach
        }
    }
    for(command in commands) {
        bot.registerGuildCommand(command, Snowflake(
            if(args[0] == "test") Settings.TEST_GUILD_ID
            else Settings.GUILD_ID
        ))
        bot.registerGlobalCommand(command)
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command.command)} 커맨드 등록 완료")
    }
}