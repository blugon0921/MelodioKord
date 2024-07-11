package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.bold
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.displayTitle
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.interactedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger

class PauseBtn {
    val name = "pauseButton"

    init {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(name)} 버튼 불러오기 성공")
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다".bold
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
                        title = "재생중인 노래가 없습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val isPaused = player.paused
            player.pause(!isPaused)

            val embed = EmbedBuilder()
            if(isPaused) embed.title = ":arrow_forward: 노래 일시정지를 해제했습니다".bold
            else embed.title = ":pause_button: 노래를 일시정지 했습니다".bold
            embed.color = Settings.COLOR_NORMAL
            embed.description = current.info.displayTitle
            embed.interactedUser(interaction)
            interaction.respondPublic {
                embeds = mutableListOf(embed)
                components = mutableListOf(buttons)
            }
        }
    }
}