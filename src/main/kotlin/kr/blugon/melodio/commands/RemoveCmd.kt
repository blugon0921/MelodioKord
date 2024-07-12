package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.displayTitle
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.logger
import kr.blugon.melodio.modules.queue

class RemoveCmd: Command, Registable {
    override val command = "remove"
    override val description = "대기열에 있는 노래를 삭제합니다"
    override val options = listOf(
        IntegerOption("number", "삭제할 노래의 번호를 적어주세요", 1, Int.MAX_VALUE.toLong()).apply {
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

            val number = interaction.command.integers["number"]!!.toInt()
            if(link.queue.isEmpty()) {
                interaction.respondEphemeral {
                    embed {
                        title = "대기열이 비어있습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }
            if(link.queue.size < number) {
                interaction.respondEphemeral {
                    embed {
                        title = "${link.queue.size}이하의 숫자를 입력해주세요".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val rmTrack = link.queue[number-1]
            interaction.respondPublic {
                embed {
                    title = "<:minus:1104057498727632906> ${number}번 노래를 삭제했어요".bold
                    color = Settings.COLOR_NORMAL
                    description = rmTrack.track.info.displayTitle
                }
                components = mutableListOf(buttons)
            }
            link.queue.removeAt(number-1)
        }
    }
}