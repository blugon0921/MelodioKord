package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.displayTitle
import kr.blugon.melodio.modules.respondError

class PauseCmd(bot: Kord): Command(bot) {
    override val command = "pause"
    override val description = "노래를 일시정지합니다"
    override val options = null


    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        if(player.paused) {
            interaction.respondError("노래가 이미 일시정지되어있습니다")
        } else {
            player.pause(true)
            interaction.respondPublic {
                embed {
                    title = ":pause_button: 노래를 일시정지 했습니다"
                    color = Settings.COLOR_NORMAL
                    description = current.info.displayTitle
                }
            }
        }
    }
}