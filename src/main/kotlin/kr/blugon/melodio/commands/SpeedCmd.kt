package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.audio.player.timescale
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.NumberOption
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.defaultCheck
import kotlin.math.round

class SpeedCmd: Command, Registable {
    override val command = "speed"
    override val description = "노래의 속도를 설정합니다"
    override val options = listOf(
        NumberOption("speed", "0.5부터 2사이의 숫자를 적어주세요", 0.5, 2.0).apply {
            required = true
        }
    )

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            val speed = round(interaction.command.numbers["speed"]!!*100)/100
            var icon = ":arrow_forward:"
            if(1 < speed) icon = ":fast_forward:"
            if(speed < 1) icon = ":rewind:"

            player.applyFilters {
                this.timescale {
                    this.speed = speed
                }
            }

            interaction.respondPublic {
                embed {
                    title = "$icon 속도가 곧 ${speed}배로 설정됩니다"
                    color = Settings.COLOR_NORMAL
                }
                components = mutableListOf(buttons)
            }
        }
    }
}