package kr.blugon.melodio.buttons

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons

class SkipBtn: Button {
    override val name = "skipButton"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            //만약 대기열 반복중이면
            if(link.repeatMode == RepeatMode.QUEUE) {
                //스킵한곡 다시 대기열에 추가
                link.queue.add(current)
            }

            interaction.respondPublic {
                embed {
                    val track = link.skip()
                    title = ":track_next: 노래 1개를 건너뛰었습니다".bold
                    description = if(track != null) {
                        """
                            ${current.info.displayTitle}
                            
                            
                            :arrow_forward: ${track.info.displayTitle}
                        """.trimIndent()
                    } else {
                        """
                            ${current.info.displayTitle}
                            
                            
                            곡 없음
                        """.trimIndent()
                    }
                    color = Settings.COLOR_NORMAL
                    this.interactedUser(interaction)
                }
                components = mutableListOf(buttons)
            }
        }
    }
}