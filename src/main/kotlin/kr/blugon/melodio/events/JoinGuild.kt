package kr.blugon.melodio.events

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kr.blugon.melodio.Loadable
import kr.blugon.melodio.Main
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.isReady
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.nowDate
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.color
import kr.blugon.melodio.api.LogColor.inColor

class JoinGuild: Loadable, Runnable {
    val name = "joinGuild"

    override fun run() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(name)} 이벤트 불러오기 성공")
        bot.on<GuildCreateEvent> {
            if(!bot.isReady) return@on
            kordLogger.log("${LogColor.CYAN.inColor("✔")} ${guild.name.color(LogColor.BLUE)}서버에 접속했어요")
            kordLogger.log("${"현재 서버 수:".color(LogColor.GREEN)} ${bot.guilds.toList().size.toString().color(LogColor.BLUE)}")
        }
    }
}