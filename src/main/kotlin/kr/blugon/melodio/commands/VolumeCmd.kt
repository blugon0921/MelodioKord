package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.player.applyFilters
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
import kr.blugon.melodio.api.Queue.Companion.queue
import kotlin.time.Duration.Companion.seconds

class VolumeCmd: Command {
    override val command = "volume"
    override val description = "노래의 볼륨을 설정합니다"
    override val options = listOf(
        IntegerOption("volume", "조절할 볼륨을 입력해주세요(기본 50)") {
            minValue = 0
            maxValue = 100
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

            val volume = interaction.command.integers["volume"]!!.toInt()
            var icon = ":loud_sound:"
            if (volume <= 50) icon = ":sound:"
            if (volume == 0) icon = ":mute:"

            link.varVolume = volume
            link.player.playTrack(current) {
                this.volume = link.varVolume
                this.position = link.player.positionDuration.plus(0.4.seconds)
            }
            player.applyFilters {
                this.volume = volume.toFloat()/100
            }

            interaction.respondPublic {
                embed {
                    title = "**${icon} 볼륨을 ${volume}%로 설정했어요**"
                    color = Settings.COLOR_NORMAL
                }
                components.add(buttons)
            }
        }
    }
}