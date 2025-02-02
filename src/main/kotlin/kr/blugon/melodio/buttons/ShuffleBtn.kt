package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.modules.*

class ShuffleBtn(bot: Kord): Button(bot) {
    override val name = "shuffle"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        link.isRepeatedShuffle = !link.isRepeatedShuffle
        if(link.isRepeatedShuffle) {
            link.repeatedShuffleCount = 0
            link.queue.shuffle()
        }

        val (queueButtons, _) = Buttons.queue(interaction, link)
        interaction.updatePublicMessage {
            components = mutableListOf(queueButtons, Buttons.controlls(link))
        }
    }
}