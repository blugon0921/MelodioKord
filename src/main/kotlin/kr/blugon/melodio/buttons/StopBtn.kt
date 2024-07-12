package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class StopBtn: Button {
    override val name = "stopButton"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            link.destroyPlayer()
            interaction.respondPublic {
                embed {
                    title = ":stop_button: 노래를 정지하고 통화방에서 퇴장했습니다".bold
                    color = Settings.COLOR_NORMAL
                    this.interactedUser(interaction)
                }
            }
        }
    }
}