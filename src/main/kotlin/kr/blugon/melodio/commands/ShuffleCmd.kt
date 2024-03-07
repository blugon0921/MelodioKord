package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.api.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.BooleanOption
import kr.blugon.melodio.api.LinkAddon.isRepeatedShuffle
import kr.blugon.melodio.api.LinkAddon.repeatMode
import kr.blugon.melodio.api.LinkAddon.repeatedShuffleCount
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.RepeatMode

class ShuffleCmd: Command, Runnable {
    override val command = "shuffle"
    override val description = "현재 대기열 순서를 섞습니다"
    override val options = listOf(
        BooleanOption("repeat", "대기열을 반복중일때 대기열을 모두 재생하면 자동으로 대기열을 섞습니다")
    )

    override fun run() {
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

            if(link.queue.size < 2) {
                interaction.respondEphemeral {
                    embed {
                        title = "**대기열에 노래가 2개 이상이어야 합니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val isRepeat = interaction.command.booleans["repeat"]
            if(isRepeat == null) {
                link.queue.shuffle()
                interaction.respondPublic {
                    embed {
                        title = "**:twisted_rightwards_arrows: 대기열 순서를 섞었습니다**"
                        color = Settings.COLOR_NORMAL
                    }
                    components = mutableListOf(buttons)
                }
            } else {
                if(!isRepeat) {
                    link.isRepeatedShuffle = false
                    link.repeatedShuffleCount = 0
                    interaction.respondPublic {
                        embed {
                            title = "**:arrow_right: 대기열 순서 섞기 반복을 해제했습니다**"
                            color = Settings.COLOR_NORMAL
                        }
                        components = mutableListOf(buttons)
                    }
                    return@on
                }
                if(link.repeatMode != RepeatMode.QUEUE) {
                    interaction.respondEphemeral {
                        embed {
                            title = "**대기열을 반복 중이어야 합니다**"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
                link.queue.shuffle()
                link.isRepeatedShuffle = true
                link.repeatedShuffleCount = 0
                interaction.respondPublic {
                    embed {
                        title = "**:twisted_rightwards_arrows: 대기열 순서 섞기를 반복합니다**"
                        color = Settings.COLOR_NORMAL
                    }
                    components = mutableListOf(buttons)
                }
            }
        }
    }
}