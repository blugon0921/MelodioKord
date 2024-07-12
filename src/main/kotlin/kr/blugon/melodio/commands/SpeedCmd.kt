package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.audio.player.timescale
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.NumberOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.logger
import kr.blugon.melodio.modules.queue
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
            if(interaction.command.rootName != command) return@onRun
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "재생중인 노래가 없습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

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
                    title = "${icon} 속도가 곧 ${speed}배로 설정됩니다".bold
                    color = Settings.COLOR_NORMAL
                }
                components = mutableListOf(buttons)
            }
        }
    }
}