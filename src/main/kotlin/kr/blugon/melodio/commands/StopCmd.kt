package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class StopCmd: Command, Registable {
    override val command = "stop"
    override val description = "노래를 정지하고 통화방에서 퇴장합니다"
    override val options = null

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


            if(link.queue.current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "재생중인 노래가 없습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            link.destroyPlayer()

            interaction.respondPublic {
                embed {
                    title = ":stop_button: 노래를 정지하고 통화방에서 퇴장했습니다".bold
                    color = Settings.COLOR_NORMAL
                }
            }
        }
    }
}