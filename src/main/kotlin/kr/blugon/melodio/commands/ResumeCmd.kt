package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.displayTitle
import kr.blugon.melodio.modules.respondError

class ResumeCmd: Command, Registable {
    override val command = "resume"
    override val description = "노래 일시정지를 해제합니다"
    override val options = null

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            if(!player.paused) {
                interaction.respondError("노래가 이미 재생중입니다")
            } else {
                player.pause(false)
                interaction.respondPublic {
                    embed {
                        title = ":arrow_forward: 노래 일시정지를 해제했습니다"
                        color = Settings.COLOR_NORMAL
                        description = current.info.displayTitle
                    }
                    components = mutableListOf(buttons)
                }
            }
        }
    }
}