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
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.IntegerOption
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue

class RemoveCmd: Command, Runnable {
    override val command = "remove"
    override val description = "대기열에 있는 노래를 삭제합니다"
    override val options = listOf(
        IntegerOption("number", "삭제할 노래의 번호를 적어주세요", 1, Int.MAX_VALUE.toLong()).apply {
            required = true
        }
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

            val number = interaction.command.integers["number"]!!.toInt()
            if(link.queue.isEmpty()) {
                interaction.respondEphemeral {
                    embed {
                        title = "**대기열이 비어있습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }
            if(link.queue.size < number) {
                interaction.respondEphemeral {
                    embed {
                        title = "**${link.queue.size}이하의 숫자를 입력해주세요**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val rmTrack = link.queue[number-1]
            interaction.respondPublic {
                embed {
                    title = "**<:minus:1104057498727632906> ${number}번 노래를 삭제했어요**"
                    color = Settings.COLOR_NORMAL
                    description = "[**${stringLimit(rmTrack.track.info.title.replace("[", "［").replace("]", "［"))}**](${rmTrack.track.info.uri})"
                }
                components = mutableListOf(buttons)
            }
            link.queue.removeAt(number-1)
        }
    }
}