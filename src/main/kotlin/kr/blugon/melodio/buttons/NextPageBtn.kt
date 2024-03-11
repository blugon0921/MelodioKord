package kr.blugon.melodio.buttons

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger
import kr.blugon.melodio.commands.QueueCmd.Companion.queuePage

class NextPageBtn {
    val name = "nextPage"

    init {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(name)} 버튼 불러오기 성공")
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
            if(!link.isSameChannel(interaction, voiceChannel)) return@on

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val beforePageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "beforePage").apply {
                this.label = "◀이전"
            }
            val nextPageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "nextPage").apply {
                this.label = "다음▶"
            }
            val reloadPageButton = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "reloadPage").apply {
                this.label = "🔄️새로고침"
            }

            var footerText = if(interaction.message.embeds[0].footer == null) "undefined"
                            else interaction.message.embeds[0].footer!!.text.replace(" ", "")
            if(footerText.contains("|")) footerText = footerText.split("|")[1]
            var nowPage = footerText.split("/")[0].replace("페이지", "").toInt()+1
            val pages = queuePage(link, current)

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
                    title = "**:clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]**"
                    color = Settings.COLOR_NORMAL
                    description = pages[nowPage-1]
                    footer {
                        text = "${interaction.user.globalName?: interaction.user.username} | 페이지 ${nowPage}/${pages.size}"
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