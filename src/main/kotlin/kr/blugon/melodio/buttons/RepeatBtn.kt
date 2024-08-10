package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons

class RepeatBtn: Button {
    override val name = "repeatQueueButton"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            interaction.respondPublic {
                embed {
                    title = if(link.repeatMode != RepeatMode.QUEUE) {
                        link.repeatMode = RepeatMode.QUEUE
                        ":repeat: 대기열을 반복합니다".bold
                    } else {
                        link.repeatMode = RepeatMode.OFF
                        ":arrow_right_hook: 노래 반복을 해제했습니다".bold
                    }
                    color = Settings.COLOR_NORMAL
                    interactedUser(interaction)
                }
                components = mutableListOf(buttons)
            }
        }
    }
}