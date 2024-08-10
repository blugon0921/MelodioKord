package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.timeFormat
import kotlin.math.ceil

class QueueCmd: Command, Registable {
    override val command = "queue"
    override val description = "현재 대기열을 보여줍니다"
    override val options = null

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            val pages = queuePage(link, current)

            interaction.respondPublic {
                embed {
                    title = ":clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]"
                    color = Settings.COLOR_NORMAL
                    description = pages[0]
                    footer {
                        text = "페이지 1/${pages.size}${
                            when(link.repeatMode) {
                                RepeatMode.TRACK -> "┃🔂 현재 곡 반복중"
                                RepeatMode.QUEUE -> "┃🔂 대기열 반복중"
                                else -> ""
                            }
                        }"
                    }
                }
                val (beforePageButton, nextPageButton, reloadPageButton) = QueueButtons.buttons
                components = mutableListOf(ActionRowBuilder().apply {
                    this.components.add(beforePageButton.apply { this.disabled = true })
                    this.components.add(nextPageButton.apply { if(pages.size == 1) this.disabled = true })
                    this.components.add(reloadPageButton)
                }, buttons)
            }
        }
    }
}

fun queuePage(link: Link, current: Track, itemCountInPage: Int = 20): List<String> {
    val page = ArrayList<String>()
    if(itemCountInPage < link.queue.size) { //2페이지 이상
        var count = 0
        for(p in 0 until  ceil(link.queue.size/itemCountInPage.toDouble()).toInt()) {
            var pageDescription = "💿 ${current.info.displayTitle}\n\n"
            val beforeCount = count
            inner@for(i in beforeCount until itemCountInPage+beforeCount) {
                try {
                    val track = link.queue[i].track
                    pageDescription += "${"${i+1}.".bold}ﾠ${track.info.displayTitle}\n"
                    count++
                } catch (_: IndexOutOfBoundsException) { break@inner }
            }
            if(0 < link.queue.size-count) {
                pageDescription += "\n"+"+${link.queue.size-count}개".bold
            }
            page.add(pageDescription)
        }
    } else { //1페이지
        var description = "💿 ${current.info.displayTitle}\n\n"
        for(i in 0 until link.queue.size) {
            description += "${"${i+1}.".bold}ﾠ${link.queue[i].track.info.displayTitle}\n"
        }
        page.add(description)
    }
    return page
}


class QueueButtons {
    val beforePageButton: ButtonBuilder
        get() = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "beforePage").apply {
            this.label = "◀이전"
        }
    val nextPageButton: ButtonBuilder
        get() = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "nextPage").apply {
            this.label = "다음▶"
        }
    val reloadPageButton: ButtonBuilder
        get() = ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "reloadPage").apply {
            this.label = "🔄️새로고침"
        }

    operator fun component1(): ButtonBuilder = beforePageButton
    operator fun component2(): ButtonBuilder = nextPageButton
    operator fun component3(): ButtonBuilder = reloadPageButton

    companion object {
        val buttons: QueueButtons
            get() = QueueButtons()
    }
}