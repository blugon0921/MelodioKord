package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.QueueUtils
import kr.blugon.melodio.modules.defaultCheck

class ReloadPageBtn(bot: Kord): Button(bot) {
    override val name = "reloadPage"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        val (embed, component) = QueueUtils.embed(link, interaction)

        interaction.updatePublicMessage {
            embeds = mutableListOf(embed)
            components = component
        }
    }
}