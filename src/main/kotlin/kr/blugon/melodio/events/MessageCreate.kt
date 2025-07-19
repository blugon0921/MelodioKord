package kr.blugon.melodio.events

import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.kord.getLink
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.bot
import kr.blugon.melodio.manager
import kr.blugon.melodio.modules.*

class MessageCreate: NamedRegistrable {
    override val name = "messageCreate"

    override fun registerEvent() {
        bot.on<MessageCreateEvent> {
            val link = bot.manager.getLink(this.guildId?: return@on)
            if(link.queue.current == null) return@on

            if(this.message.author?.id == bot.selfId) {
                if(this.message.embeds.firstOrNull()?.title == ":clipboard: 대기열") return@on
                if(this.message.embeds.firstOrNull()?.title == ":musical_note: 대기열에 재생목록을 추가하였습니다") return@on
            }

            val controllerMessage = Buttons.controllerMessages[this.guildId]?: return@on
            if(this.message.channelId.value != controllerMessage.channelId.value) return@on
            Buttons.resendController(link, message.channel)
        }
    }
}