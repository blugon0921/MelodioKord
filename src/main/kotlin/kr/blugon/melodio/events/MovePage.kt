package kr.blugon.melodio.events

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildSelectMenuInteractionCreateEvent
import dev.kord.core.on
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.NamedRegistrable
import kr.blugon.melodio.modules.QueueUtils
import kr.blugon.melodio.modules.defaultCheck

class MovePage: NamedRegistrable {
    override val name = "moveQueuePage"

    override fun registerEvent() {
        bot.on<GuildSelectMenuInteractionCreateEvent> {
            if(interaction.component.customId != "moveQueuePage") return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            val targetPage = interaction.values.first().toInt()

            val (embed, component) = QueueUtils.embed(link, interaction) { targetPage }

            interaction.updatePublicMessage {
                embeds = mutableListOf(embed)
                components = component
            }
        }
    }
}