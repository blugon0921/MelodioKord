package kr.blugon.melodio.events

import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.isReady
import kr.blugon.melodio.modules.Event
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.LogColor.Companion.color
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.logger

class ClientReady: Event {
    override val name = "clientReady"

    override suspend fun register() {
        bot.on<ReadyEvent> {
            logger.log("")
            logger.log("접속 서버(${bot.guilds.toList().size})".color(LogColor.CYAN))
            for(guild in bot.guilds.toList()) {
                logger.log(guild.name.color(LogColor.BLUE))
            }
            bot.isReady = true
        }
    }
}