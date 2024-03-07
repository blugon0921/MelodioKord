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
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue

class PauseCmd: Command, Runnable {
    override val command = "pause"
    override val description = "노래를 일시정지합니다"
    override val options = null

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

            if(player.paused) {
                interaction.respondEphemeral {
                    embed {
                        title = "**노래가 이미 일시정지 중입니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
            } else {
                player.pause(true)
                interaction.respondPublic {
                    embed {
                        title = "**:pause_button: 노래를 일시정지 했습니다**"
                        color = Settings.COLOR_NORMAL
                        description = "[**${stringLimit(current.info.title.replace("[", "［").replace("]", "［"))}**](${current.info.uri})"
                    }
                    components = mutableListOf(buttons)
                }
            }
        }
    }
}