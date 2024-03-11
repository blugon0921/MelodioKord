package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.StringOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Modules.timeToSecond
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger

class MoveCmd: Command, OnCommand {
    override val command = "move"
    override val description = "노래의 재생 위치를 이동합니다"
    override val options = listOf(
        StringOption("location", "이동할 위치를 적어주세요(00:00:00)").apply {
            this.required = true
        }
    )

    override fun on() {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        onRun(bot) {
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
            val time = interaction.command.strings["location"]!!

            val ms: Long
            try {
                ms = timeToSecond(time)*1000L
            } catch (e: Exception) {
                interaction.respondEphemeral {
                    embed {
                        title = "**시간 형식이 잘못되었습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }
            try {
                if(ms >= current.info.length) player.seekTo(current.info.length-100)
                else player.seekTo(ms)
            } catch (e: Exception) {
                interaction.respondEphemeral {
                    embed {
                        title = "**${timeFormat(ms/1000)} 위치로 이동할 수 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val embed = EmbedBuilder().apply {
                title = "**:left_right_arrow: ${timeFormat(ms)} 위치로 이동했어요**"
                color = Settings.COLOR_NORMAL
            }

            if(ms >= current.info.length) {
                embed.title = "**:left_right_arrow: ${timeFormat(current.info.length)} 위치로 이동했어요**"
            }

            interaction.respondPublic {
                embeds = mutableListOf(embed)
                components = mutableListOf(buttons)
            }
            return@onRun
        }
    }
}