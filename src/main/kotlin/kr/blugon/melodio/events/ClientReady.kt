package kr.blugon.melodio.events

import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.bot
import kr.blugon.melodio.isReady
import kr.blugon.melodio.modules.*

class ClientReady: NamedRegistrable {
    override val name = "clientReady"

    override fun registerEvent() {
        bot.on<ReadyEvent> {
            Logger.log("")
            Logger.log("접속 서버(${bot.guilds.toList().size})".color(LogColor.Cyan))
            for(guild in bot.guilds.toList()) {
                Logger.log(guild.name.color(LogColor.Blue))
            }
            bot.isReady = true
        }
    }
}