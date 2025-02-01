package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.bot
import kr.blugon.melodio.isReady
import kr.blugon.melodio.modules.*

class JoinGuild: NamedRegistrable {
    override val name = "joinGuild"

    override fun registerEvent() {
        bot.on<GuildCreateEvent> {
            if(!bot.isReady) return@on
            Logger.log("${"✔".color(LogColor.Cyan)} ${guild.name.color(LogColor.Blue)}서버에 접속했어요")
            Logger.log("${"현재 서버 수:".color(LogColor.Green)} ${bot.guilds.toList().size.toString().color(LogColor.Blue)}")
        }
    }
}