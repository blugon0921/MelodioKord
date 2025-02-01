package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.Settings
import kr.blugon.melodio.commands.queuePage
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.Modules.timeFormat
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.repeatMode

class ReloadPageBtn(bot: Kord): Button(bot) {
    override val name = "reloadPage"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        val (beforePageButton, nextPageButton, reloadPageButton) = Buttons.queue

        val footerText = (if(interaction.message.embeds[0].footer == null) "undefined"
                        else interaction.message.embeds[0].footer!!.text.replace(" ", "")).split("|").last().split("┃").first()
        var nowPage = footerText.split("/")[0].replace("페이지", "").toInt()
        val pages = queuePage(link, current)

        if(nowPage <= 1) {
            nowPage = 1
            beforePageButton.disabled = true
        }
        if(pages.size <= nowPage) {
            nowPage = pages.size
            nextPageButton.disabled = true
        }

        val pageButtons = ActionRowBuilder().apply {
            components.add(beforePageButton)
            components.add(nextPageButton)
            components.add(reloadPageButton)
        }

        interaction.updatePublicMessage {
            embed {
                title = ":clipboard: 대기열 [${timeFormat(link.queue.duration)}]"
                color = Settings.COLOR_NORMAL
                description = pages[nowPage-1]
                footer {
                    text = "${interaction.user.globalName?: interaction.user.username} | 페이지 ${nowPage}/${pages.size}${
                        when(link.repeatMode) {
                            RepeatMode.TRACK -> "┃🔂 현재 곡 반복중"
                            RepeatMode.QUEUE -> "┃🔂 대기열 반복중"
                            else -> ""
                        }
                    }"
                    this.icon = if(interaction.user.avatar == null) interaction.user.defaultAvatar.cdnUrl.toUrl()
                    else interaction.user.avatar!!.cdnUrl.toUrl()
                }
            }
            components = mutableListOf(pageButtons, Buttons.controlls(link))
        }
    }
}