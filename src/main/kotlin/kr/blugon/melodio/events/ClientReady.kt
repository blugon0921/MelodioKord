package kr.blugon.melodio.events

import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kr.blugon.melodio.bot
import kr.blugon.melodio.isReady
import kr.blugon.melodio.modules.*

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