package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.audio.player.timescale
import kr.blugon.melodio.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.IntegerOption
import kr.blugon.melodio.api.LinkAddon.varVolume
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.NumberOption
import kr.blugon.melodio.api.Queue.Companion.queue
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SpeedCmd: Command {
    override val command = "speed"
    override val description = "노래의 속도를 설정합니다"
    override val options = listOf(
        NumberOption("speed", "0.5부터 2사이의 숫자를 적어주세요") {
            minValue = 0.5
            maxValue = 2.0
            required = true
        }
    )

    suspend fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName != command) return@on
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@on

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val speed = round(interaction.command.numbers["speed"]!!*100)/100
            var icon = ":arrow_forward:"
            if(1 < speed) icon = ":fast_forward:"
            if(speed < 1) icon = ":rewind:"

            player.applyFilters {
                this.timescale {
                    this.speed = speed.toFloat()
                }
            }

            interaction.respondPublic {
                embed {
                    title = "**${icon} 속도가 곧 ${speed}배로 설정됩니다**"
                    color = Settings.COLOR_NORMAL
                }
                components.add(buttons)
            }
        }
    }
}