package kr.blugon.melodio

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.Command.Companion.registerGlobalCommand
import kr.blugon.kordmand.Command.Companion.registerGuildCommand
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.logger

suspend fun registerCommands(isTest: Boolean = false) {
    val token = when(isTest) {
        true -> Settings.TEST_TOKEN?: ThrowConfigException("testToken")
        false -> Settings.TOKEN
    }
    val guildId = when(isTest) {
        true -> Settings.TEST_GUILD_ID?: ThrowConfigException("testGuildId")
        false -> Settings.GUILD_ID
    }
    val bot = Kord(token)

    val rootPackage = Main.javaClass.`package`

    val commands = ArrayList<Command>()
    rootPackage.classes<Command>("commands").forEach { clazz ->
        try {
            val instance = clazz.getDeclaredConstructor().newInstance()
            commands.add(instance)
        } catch (e: Exception) {
            return@forEach
        }
    }
    for(command in commands) {
        bot.registerGuildCommand(command, Snowflake(guildId))
        bot.registerGlobalCommand(command)
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command.command)} 커맨드 등록 완료")
    }
}