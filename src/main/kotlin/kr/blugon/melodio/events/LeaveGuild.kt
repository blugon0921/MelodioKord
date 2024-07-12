package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.modules.Event
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.LogColor.Companion.color
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.logger

class LeaveGuild: Event {
    override val name = "leaveGuild"

    override suspend fun register() {
        bot.on<GuildDeleteEvent> {
            logger.log("${LogColor.RED.inColor("✖")} ${guild?.name?.color(LogColor.BLUE)}서버에 추방당했어요")
            logger.log("${"현재 서버 수:".color(LogColor.GREEN)} ${bot.guilds.toList().size.toString().color(LogColor.BLUE)}")
        }
    }
}