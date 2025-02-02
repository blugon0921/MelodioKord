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

class BeforePageBtn(bot: Kord): Button(bot) {
    override val name = "beforePage"

    override suspend fun GuildButtonInteractionCreateEvent.onClick() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

//        val footerText = when(interaction.message.embeds[0].footer == null) {
//            true -> "undefined"
//            false -> interaction.message.embeds[0].footer!!.text.replace(" ", "")
//        }.split("|").last().split("┃").first()
//        var nowPage = footerText.split("/")[0].replace("페이지", "").toInt()-1
//        val pages = queuePage(link, current)
//
//        val (beforePageButton, nextPageButton, reloadPageButton) = Buttons.queue
//
//        if(nowPage <= 1) {
//            nowPage = 1
//            beforePageButton.disabled = true
//        }
//        if(pages.size <= 1) {
//            nextPageButton.disabled = true
//            beforePageButton.disabled = true
//        }
//
//        val pageButtons = ActionRowBuilder().apply {
//            components.add(beforePageButton)
//            components.add(nextPageButton)
//            components.add(reloadPageButton)
//        }
        val (queueButtons, nowPage) = Buttons.queue(interaction, link, -1)
        val pages = queuePage(link, current)

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
            components = mutableListOf(queueButtons, Buttons.controlls(link))
        }
    }
}