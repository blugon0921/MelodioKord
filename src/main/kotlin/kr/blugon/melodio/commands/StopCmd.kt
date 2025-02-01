package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.destroyPlayer

class StopCmd(bot: Kord): Command(bot) {
    override val command = "stop"
    override val description = "노래를 정지하고 통화방에서 퇴장합니다"
    override val options = null


    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        link.destroyPlayer()

        interaction.respondPublic {
            embed {
                title = ":stop_button: 노래를 정지하고 통화방에서 퇴장했습니다"
                color = Settings.COLOR_NORMAL
            }
        }
    }
}