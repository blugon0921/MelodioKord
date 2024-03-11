package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.Companion.color
import kr.blugon.melodio.api.logger

class LeaveGuild {
    val name = "leaveGuild"

    init {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(name)} 이벤트 불러오기 성공")
        bot.on<GuildDeleteEvent> {
            logger.log("${LogColor.RED.inColor("✖")} ${guild?.name?.color(LogColor.BLUE)}서버에 추방당했어요")
            logger.log("${"현재 서버 수:".color(LogColor.GREEN)} ${bot.guilds.toList().size.toString().color(LogColor.BLUE)}")
        }
    }
}