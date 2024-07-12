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
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.modules.Modules.interactedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class RepeatBtn: Button {
    override val name = "repeatQueueButton"

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

            val embed = EmbedBuilder()
            embed.color = Settings.COLOR_NORMAL

            if(link.repeatMode != RepeatMode.QUEUE) {
                embed.title = ":repeat: 대기열을 반복합니다".bold
                link.repeatMode = RepeatMode.QUEUE
            } else {
                embed.title = ":arrow_right_hook: 노래 반복을 해제했습니다".bold
                link.repeatMode = RepeatMode.OFF
            }

            embed.interactedUser(interaction)
            interaction.respondPublic {
                embeds = mutableListOf(embed)
                components = mutableListOf(buttons)
            }
        }
    }
}