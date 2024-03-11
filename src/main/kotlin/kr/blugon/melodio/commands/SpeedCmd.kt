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
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger
import kotlin.math.round

class SpeedCmd: Command, OnCommand {
    override val command = "speed"
    override val description = "노래의 속도를 설정합니다"
    override val options = listOf(
        NumberOption("speed", "0.5부터 2사이의 숫자를 적어주세요", 0.5, 2.0).apply {
            required = true
        }
    )

    override fun on() {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        onRun(bot) {
            if(interaction.command.rootName != command) return@onRun
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**음성 채널에 접속해있지 않습니다**"
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
                        title = "**재생중인 노래가 없습니다**"
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
                    title = "**${icon} 속도가 곧 ${speed}배로 설정됩니다**"
                    color = Settings.COLOR_NORMAL
                }
                components = mutableListOf(buttons)
            }
        }
    }
}