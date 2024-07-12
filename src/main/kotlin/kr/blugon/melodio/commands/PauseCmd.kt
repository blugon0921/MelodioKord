package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
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

class PauseCmd: Command, Registable {
    override val command = "pause"
    override val description = "노래를 일시정지합니다"
    override val options = null

    override suspend fun register() {
        onRun(bot) {
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

            val player = link.player


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

            if(player.paused) {
                interaction.respondEphemeral {
                    embed {
                        title = "노래가 이미 일시정지 중입니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
            } else {
                player.pause(true)
                interaction.respondPublic {
                    embed {
                        title = ":pause_button: 노래를 일시정지 했습니다".bold
                        color = Settings.COLOR_NORMAL
                        description = current.info.displayTitle
                    }
                    components = mutableListOf(buttons)
                }
            }
        }
    }
}