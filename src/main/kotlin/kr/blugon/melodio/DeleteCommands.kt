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

suspend fun deleteCommands(isTest: Boolean = false) {
    print("삭제할 명령어를 입력해주세요(쉼표로구분)> ")
    val read = readln()

    val token = when(isTest) {
        true -> Settings.TEST_TOKEN?: throw ConfigException("testToken")
        false -> Settings.TOKEN
    }
    val bot = Kord(token)

    val commands = read.split(",")
    bot.getGlobalApplicationCommands().toList().forEach {
        if(!commands.contains(it.name)) return@forEach
        it.delete()
        Logger.log("${LogColor.Red.inColor("✔")} ${LogColor.Cyan.inColor(it.name)} 커맨드 삭제 완료")
    }
    bot.logout()
}