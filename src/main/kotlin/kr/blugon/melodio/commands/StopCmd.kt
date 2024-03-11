package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.destroyPlayer
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger

class StopCmd: Command, OnCommand {
    override val command = "stop"
    override val description = "노래를 정지하고 통화방에서 퇴장합니다"
    override val options = null

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

            if(link.queue.current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            link.destroyPlayer()

            interaction.respondPublic {
                embed {
                    title = "**:stop_button: 노래를 정지하고 통화방에서 퇴장했습니다**"
                    color = Settings.COLOR_NORMAL
                }
            }
        }
    }
}