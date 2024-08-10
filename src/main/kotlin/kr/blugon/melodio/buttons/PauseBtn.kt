package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.live.live
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.player.guildId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons

class PauseBtn: Button {
    override val name = "pauseButton"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            val isPaused = player.paused
            player.pause(!isPaused)

            interaction.respondPublic {
                embed {
                    title = when(isPaused) {
                        true -> ":arrow_forward: 노래 일시정지를 해제했습니다"
                        false -> ":pause_button: 노래를 일시정지 했습니다"
                    }
                    color = Settings.COLOR_NORMAL
                    description = current.info.displayTitle
                    this.interactedUser(interaction)
                }
                components = mutableListOf(buttons)
            }
        }
    }
}