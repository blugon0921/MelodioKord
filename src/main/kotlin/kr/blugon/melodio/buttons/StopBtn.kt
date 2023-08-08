package kr.blugon.melodio.buttons

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.models.TrackResponse
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Modules.usedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.color
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.PlayerAddon.destroy
import kr.blugon.melodio.api.PlayerAddon.isEventAdded
import kr.blugon.melodio.api.PlayerAddon.varVolume
import kr.blugon.melodio.api.Queue.Companion.queue
import java.lang.IndexOutOfBoundsException

class StopBtn {
    val name = "stopButton"

    fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(name)} 버튼 불러오기 성공")
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
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

            player.destroy(link)

            interaction.respondPublic {
                embed {
                    title = "**:stop_button: 노래를 정지하고 통화방에서 퇴장했습니다**"
                    color = Settings.COLOR_NORMAL
                    this.usedUser(interaction)
                }
            }
        }
    }
}