package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.lavakordqueue.skip
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class SkipBtn(bot: Kord): Button(bot) {
    override val name = "skip"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        val newTrack = link.skip()

        if(newTrack == null) { //스킵한 노래가 없으면
            interaction.respondPublic {
                embed {
                    title = ":track_next: 노래 1개를 건너뛰었습니다".bold
                    description = """
                        ${current.info.displayTitle}
                        
                        곡 없음
                    """.trimIndent()
                    color = Settings.COLOR_NORMAL
                    this.interactedUser(interaction)
                }
            }
            return
        }

        interaction.respondPublic {
            embed {
                title = ":track_next: 노래 1개를 건너뛰었습니다".bold
                description = """
                    ${current.info.displayTitle}
                    
                    :arrow_forward: ${newTrack.info.displayTitle}
                """.trimIndent()
                color = Settings.COLOR_NORMAL
                this.interactedUser(interaction)
            }
        }
    }
}