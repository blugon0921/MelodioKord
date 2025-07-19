package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import kr.blugon.melodio.modules.*

class PauseBtn(bot: Kord): Button(bot) {
    override val name = "pause"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return
        player.pause(!player.paused)

        val (_, component) = QueueUtils.embed(link, interaction)

        interaction.updatePublicMessage {
            components = component
        }
    }
}