package kr.blugon.melodio.events

import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.NamedRegistrable
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.color
import kr.blugon.melodio.modules.Logger

class LeaveGuild: NamedRegistrable {
    override val name = "leaveGuild"

    override fun registerEvent() {
        bot.on<GuildDeleteEvent> {
            Logger.log("${LogColor.Red.inColor("✖")} ${guild?.name?.color(LogColor.Blue)}서버에 추방당했어요")
            Logger.log("${"현재 서버 수:".color(LogColor.Green)} ${bot.guilds.toList().size.toString().color(LogColor.Blue)}")
        }
    }
}