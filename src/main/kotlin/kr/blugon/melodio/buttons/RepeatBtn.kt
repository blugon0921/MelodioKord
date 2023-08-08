package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.usedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.PlayerAddon.destroy
import kr.blugon.melodio.api.PlayerAddon.repeatMode
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.RepeatMode

class RepeatBtn {
    val name = "repeatQueueButton"

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

            val embed = EmbedBuilder()
            embed.color = Settings.COLOR_NORMAL

            if(player.repeatMode != RepeatMode.QUEUE) {
                embed.title = "**:repeat: 대기열을 반복합니다**"
                player.repeatMode = RepeatMode.QUEUE
            } else {
                embed.title = "**:arrow_right_hook: 노래 반복을 해제했습니다**"
                player.repeatMode = RepeatMode.OFF
            }

            embed.usedUser(interaction)
            interaction.respondPublic {
                embeds.add(embed)
                components.add(buttons)
            }
        }
    }
}