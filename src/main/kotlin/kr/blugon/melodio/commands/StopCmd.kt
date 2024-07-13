package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.bold
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.destroyPlayer

class StopCmd: Command, Registable {
    override val command = "stop"
    override val description = "노래를 정지하고 통화방에서 퇴장합니다"
    override val options = null

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            link.destroyPlayer()

            interaction.respondPublic {
                embed {
                    title = ":stop_button: 노래를 정지하고 통화방에서 퇴장했습니다"
                    color = Settings.COLOR_NORMAL
                }
            }
        }
    }
}