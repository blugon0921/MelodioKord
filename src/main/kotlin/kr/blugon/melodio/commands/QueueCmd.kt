package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.modules.bold
import kr.blugon.melodio.modules.displayTitle
import kotlin.math.ceil

//class QueueCmd(bot: Kord): Command(bot) {
//    override val command = "queue"
//    override val description = "대기열을 표시합니다"
//    override val options = null
//
//
//    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
//        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return
//
////        val pages = queuePage(link, current)
//
////        interaction.respondPublic {
////            embed {
////                title = ":clipboard: 대기열 [${timeFormat(link.queue.duration)}]"
////                color = Settings.COLOR_NORMAL
////                description = pages[0]
////                footer {
////                    text = "페이지 1/${pages.size}${
////                        when(link.repeatMode) {
////                            RepeatMode.TRACK -> "┃🔂 현재 곡 반복중"
////                            RepeatMode.QUEUE -> "┃🔂 대기열 반복중"
////                            else -> ""
////                        }
////                    }"
////                }
////            }
////            val (beforePageButton, nextPageButton, reloadPageButton) = Buttons.queue
////            components = mutableListOf(ActionRowBuilder().apply {
////                this.components.add(beforePageButton.apply { this.disabled = true })
////                this.components.add(nextPageButton.apply { if(pages.size == 1) this.disabled = true })
////                this.components.add(reloadPageButton)
////            })
////        }
//        Buttons.reloadControllerInChannel(link, interaction.channel)
//        interaction.deferEphemeralResponse()
//    }
//}

fun queuePage(link: Link, current: Track, itemCountInPage: Int = 20): List<String> {
    val page = ArrayList<String>()
    if(itemCountInPage < link.queue.size) { //2페이지 이상
        var count = 0
        for(p in 0 until  ceil(link.queue.size/itemCountInPage.toDouble()).toInt()) {
            var pageDescription = "💿 ${current.info.displayTitle}\n\n"
            val beforeCount = count
            inner@for(i in beforeCount until itemCountInPage+beforeCount) {
                try {
                    val track = link.queue[i]
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
            description += "${"${i+1}.".bold}ﾠ${link.queue[i].info.displayTitle}\n"
        }
        page.add(description)
    }
    return page
}