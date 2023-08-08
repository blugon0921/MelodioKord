package kr.blugon.melodio.events

import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.color
import kr.blugon.melodio.api.LogColor.inColor

class ClientReady {
    val name = "clientReady"

    suspend fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(name)} 이벤트 불러오기 성공")
        bot.on<ReadyEvent> {
            kordLogger.log("")
            kordLogger.log("접속 서버(${bot.guilds.toList().size})".color(LogColor.CYAN))
            for(guild in bot.guilds.toList()) {
                kordLogger.log(guild.name.color(LogColor.BLUE))
            }
        }
    }
}