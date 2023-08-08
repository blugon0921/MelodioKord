package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.BooleanOption
import kr.blugon.melodio.api.IntegerOption
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.PlayerAddon.repeatMode
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.RepeatMode

class RepeatCmd: Command {
    override val command = "repeat"
    override val description = "대기열 혹은 노래를 반복합니다"
    override val options = listOf(
        IntegerOption("mode", "반복 모드를 선택해주세요") {
            choice("현재 노래", 1)
            choice("대기열", 2)
            choice("해제", 3)
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
            if(link.state != Link.State.CONNECTED && link.state != Link.State.CONNECTING) {
                interaction.respondEphemeral {
                    embed {
                        title = "**봇이 음성 채널에 접속해 있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val player = link.player

            val current = player.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val embed = EmbedBuilder()
            val mode = interaction.command.integers["mode"]
            when(mode) {
                1L -> {
                    player.repeatMode = RepeatMode.TRACK
                    embed.title = "**:repeat_one: 현재 노래를 반복합니다**"
                }
                2L -> {
                    player.repeatMode = RepeatMode.QUEUE
                    embed.title = "**:repeat: 대기열을 반복합니다**"
                }
                3L -> {
                    player.repeatMode = RepeatMode.OFF
                    embed.title = "**:arrow_right_hook: 노래 반복을 해제했습니다**"
                }
                else -> {
                    if(player.repeatMode == RepeatMode.OFF) {
                        player.repeatMode = RepeatMode.TRACK
                        embed.title = "**:repeat_one: 현재 노래를 반복합니다**"
                    } else {
                        player.repeatMode = RepeatMode.OFF
                        embed.title = "**:arrow_right_hook: 노래 반복을 해제했습니다**"
                    }
                }
            }

            interaction.respondPublic {
                embeds.add(embed.apply {
                    color = Settings.COLOR_NORMAL
                })
                components.add(buttons)
            }
        }
    }
}