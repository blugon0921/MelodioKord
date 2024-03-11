package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.isReady
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.Companion.color
import kr.blugon.melodio.api.logger

class JoinGuild {
    val name = "joinGuild"

    init {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(name)} 이벤트 불러오기 성공")
        bot.on<GuildCreateEvent> {
            if(!bot.isReady) return@on
            logger.log("${LogColor.CYAN.inColor("✔")} ${guild.name.color(LogColor.BLUE)}서버에 접속했어요")
            logger.log("${"현재 서버 수:".color(LogColor.GREEN)} ${bot.guilds.toList().size.toString().color(LogColor.BLUE)}")
        }
    }
}