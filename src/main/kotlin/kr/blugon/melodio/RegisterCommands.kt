package kr.blugon.melodio

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import kotlinx.coroutines.flow.toList
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.registerGlobalCommand
import kr.blugon.kordmand.registerGuildCommand
import kr.blugon.melodio.exception.ConfigException
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.Logger

suspend fun registerCommands(isTest: Boolean = false) {
    println("등록할 곳을 선택해주세요:")
    println("1. Guild")
    println("2. Global")
    println("3. Both")
    print("> ")
    val read = readln().toIntOrNull()
    if(read == null || read !in 1..3) {
        println("잘못된 입력입니다.")
        return
    }

    val token = when(isTest) {
        true -> Settings.TEST_TOKEN?: throw ConfigException("testToken")
        false -> Settings.TOKEN
    }
    val guildId = when(isTest) {
        true -> Settings.TEST_GUILD_ID?: throw ConfigException("testGuildId")
        false -> Settings.GUILD_ID
    }
    val bot = Kord(token)

    val rootPackage = Main.javaClass.`package`
    for(command in rootPackage.botArgClasses<Command>("commands", bot)) {
        when(read) {
            1 -> bot.registerGuildCommand(command, Snowflake(guildId))
            2 -> bot.registerGlobalCommand(command)
            3 -> {
                bot.registerGuildCommand(command, Snowflake(guildId))
                bot.registerGlobalCommand(command)
            }
        }
        Logger.log("${LogColor.Cyan.inColor("✔")} ${LogColor.Cyan.inColor(command.command)} 커맨드 등록 완료")
    }
    bot.logout()
}