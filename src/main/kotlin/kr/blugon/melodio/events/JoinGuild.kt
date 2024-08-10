package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.bot
import kr.blugon.melodio.isReady
import kr.blugon.melodio.modules.*

class JoinGuild: Event {
    override val name = "joinGuild"

    override suspend fun register() {
        bot.on<GuildCreateEvent> {
            if(!bot.isReady) return@on
            logger.log("${LogColor.CYAN.inColor("✔")} ${guild.name.color(LogColor.BLUE)}서버에 접속했어요")
            logger.log("${"현재 서버 수:".color(LogColor.GREEN)} ${bot.guilds.toList().size.toString().color(LogColor.BLUE)}")
        }
    }
}