package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.modules.*

class RepeatBtn(bot: Kord): Button(bot) {
    override val name = "repeat"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        when(link.queue.repeatMode) {
            RepeatMode.OFF -> link.queue.repeatMode = RepeatMode.QUEUE
            RepeatMode.QUEUE -> link.queue.repeatMode = RepeatMode.TRACK
            RepeatMode.TRACK -> link.queue.repeatMode = RepeatMode.OFF
        }

        val (_, component) = QueueUtils.embed(link, interaction)

        interaction.updatePublicMessage {
            components = component
        }
    }
}