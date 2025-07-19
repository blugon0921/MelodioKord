package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.GuildButtonInteraction
import dev.kord.core.entity.interaction.GuildSelectMenuInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.component.StringSelectBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Modules.timeFormat
import kotlin.math.ceil

object QueueUtils {
    fun embed(link: Link, interaction: GuildSelectMenuInteraction, page: (Int) -> Int? = { null }): Pair<EmbedBuilder, MutableList<MessageComponentBuilder>>
        = embed(link, interaction.message, page)
    fun embed(link: Link, interaction: GuildButtonInteraction, page: (Int) -> Int? = { null }): Pair<EmbedBuilder, MutableList<MessageComponentBuilder>>
        = embed(link, interaction.message, page)
    fun embed(
        link: Link,
        message: Message? = null,
        page: (Int) -> Int? = { null }
    ): Pair<EmbedBuilder, MutableList<MessageComponentBuilder>> {
        var nowPage = when(message == null) {
            true -> 1
            false -> {
                if(message.embeds.firstOrNull()?.footer == null) 1
                else message.embeds[0].footer!!.text.replace(" ", "")
                    .split("/")[0].replace("페이지", "").toInt()
            }
        }
        val current = link.queue.current!!
        val pages = queuePage(link, current)

        if(nowPage <= 1) nowPage = 1
        if(pages.size <= nowPage) nowPage = pages.size
        if(page(nowPage) != null) nowPage = page(nowPage)!!

        return embedBuilder {
            title = ":clipboard: 대기열 [${timeFormat(link.queue.duration)}]"
            color = Settings.COLOR_NORMAL
            description = pages[nowPage-1]
            this.thumbnail {
                this.url = current.info.artworkUrl?: return@thumbnail
            }
            footer {
                text = "페이지 ${nowPage}/${pages.size}${
                    when(link.repeatMode) {
                        RepeatMode.TRACK -> "┃🔂 현재 곡 반복중"
                        RepeatMode.QUEUE -> "┃🔂 대기열 반복중"
                        else -> ""
                    }
                }"
            }
        } to mutableListOf(ActionRowBuilder().apply {
            this.components.add(StringSelectBuilder("moveQueuePage").apply {
                this.placeholder = "페이지 이동(현재 페이지에서 ±11페이지까지 표시)"
                var start = nowPage-11
                if(start < 2 || pages.size <= 25) start = 2
                var end = start+22
                if(pages.size < end) end = pages.size
                this.option("1 페이지", "1")
                for(i in start..end) {
                    this.option("$i 페이지", "$i")
                }
                if(end != pages.size) this.option("${pages.size} 페이지", "${pages.size}")
            })
        }, Buttons.controlls(link))
    }

    fun queuePage(link: Link, current: Track, itemCountInPage: Int = 15): List<String> {
        val page = ArrayList<String>()
        if(itemCountInPage < link.queue.size) { //2페이지 이상
            var count = 0
            repeat(ceil(link.queue.size/itemCountInPage.toDouble()).toInt()) {
                var pageDescription = "💿 ${current.info.displayTitle}\n\n"
                val beforeCount = count
                inner@for(i in beforeCount until itemCountInPage+beforeCount) {
                    try {
                        val track = link.queue[i]
                        pageDescription += "${"${i+1}.".bold} ${track.info.displayTitle}\n"
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
            repeat(link.queue.size) {
                description += "${"${it+1}.".bold} ${link.queue[it].info.displayTitle}\n"
            }
            page.add(description)
        }
        return page
    }
}