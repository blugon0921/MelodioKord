package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.displayTitle
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.modules.Modules.interactedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.Button
import kr.blugon.melodio.modules.logger
import kr.blugon.melodio.modules.queue

class PauseBtn: Button {
    override val name = "pauseButton"

    override suspend fun register() {
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