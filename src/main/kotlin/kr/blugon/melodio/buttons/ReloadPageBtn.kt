package kr.blugon.melodio.buttons

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Settings
import kr.blugon.melodio.commands.queuePage
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.timeFormat

class ReloadPageBtn: Button {
    override val name = "reloadPage"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@on

            val beforePageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "beforePage").apply {
                this.label = "◀이전"
            }
            val nextPageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "nextPage").apply {
                this.label = "다음▶"
            }
            val reloadPageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "reloadPage").apply {
                this.label = "🔄️새로고침"
            }

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

            interaction.deferPublicMessageUpdate()
            interaction.message.edit {
                embed {
                    title = ":clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]"
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
                components = mutableListOf(
                    pageButtons,
                    buttons
                )
            }
        }
    }
}