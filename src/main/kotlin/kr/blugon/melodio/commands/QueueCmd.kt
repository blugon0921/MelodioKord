package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.displayTitle
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.modules.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.LogColor
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.logger
import kr.blugon.melodio.modules.queue
import kotlin.math.ceil

class QueueCmd: Command, Registable {
    override val command = "queue"
    override val description = "현재 대기열을 보여줍니다"
    override val options = null

    override suspend fun register() {
        onRun(bot) {
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun


            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "재생중인 노래가 없습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            if(link.queue.isEmpty()) {
                interaction.respondPublic {
                    embed {
                        title = ":clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]".bold
                        color = Settings.COLOR_NORMAL
                        description = "💿 ${current.info.displayTitle}\n"
                    }
                    components = mutableListOf(buttons)
                }
                return@onRun
            }

            val pages = queuePage(link, current)

            interaction.respondPublic {
                embed {
                    title = ":clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]".bold
                    color = Settings.COLOR_NORMAL
                    description = pages[0]
                    footer {
                        text = "페이지 1/${pages.size}"
                    }
                }
                components = mutableListOf(ActionRowBuilder().apply {
                    this.interactionButton(ButtonStyle.Primary, "beforePage") {
                        this.label = "◀이전"
                        this.disabled = true
                    }
                    this.interactionButton(ButtonStyle.Primary, "nextPage") {
                        this.label = "다음▶"
                        if(pages.size == 1) this.disabled = true
                    }
                    this.interactionButton(ButtonStyle.Primary, "reloadPage") {
                        this.label = "🔄️새로고침"
                    }
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
//            println(pageDescription)
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