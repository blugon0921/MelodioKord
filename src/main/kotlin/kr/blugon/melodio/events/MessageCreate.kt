package kr.blugon.melodio.events

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.kord.getLink
import kotlinx.coroutines.flow.toList
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.bot
import kr.blugon.melodio.isReady
import kr.blugon.melodio.manager
import kr.blugon.melodio.modules.*

class MessageCreate: NamedRegistrable {
    override val name = "messageCreate"

    override fun registerEvent() {
        bot.on<MessageCreateEvent> {
            val link = bot.manager.getLink(this.guildId?: return@on)
            if(link.queue.current == null) return@on
//            val autherId = this.member?.id?.value?: return@on
//            if(autherId == bot.selfId.value) return@on

            if(this.guildId == null) return@on
            if(Buttons.beforeControllMessage[this.guildId]?.containsKey(this.message.channelId) == true) {
                Buttons.reloadControllerInChannel(link, message.channel)
            }
        }
    }
}