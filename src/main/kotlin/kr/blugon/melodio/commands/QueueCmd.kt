package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.Command
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue
import kotlin.math.ceil

class QueueCmd: Command, Runnable {
    override val command = "queue"
    override val description = "현재 대기열을 보여줍니다"
    override val options = null

    override fun run() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName != command) return@on
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

            if(link.queue.isEmpty()) {
                interaction.respondPublic {
                    embed {
                        title = "**:clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]**"
                        color = Settings.COLOR_NORMAL
                        description = "**💿 [${stringLimit(current.info.title).replace("[", "［").replace("]", "［")}](${current.info.uri})\n**"
                    }
                    components = mutableListOf(buttons)
                }
                return@on
            }

            val pages = queuePage(link, current)

            interaction.respondPublic {
                embed {
                    title = "**:clipboard: 현재 대기열 [${timeFormat(link.queue.duration)}]**"
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
                })
                components = mutableListOf(buttons)
            }
        }
    }

    companion object {
        val pageItemCount = 20

        fun queuePage(link: Link, current: Track, maxLength: Int = pageItemCount): List<String> {
            val page = ArrayList<String>()
            if(maxLength < link.queue.size) { //2페이지 이상
                var count = 0
                for(p in 0 until  ceil(link.queue.size/(maxLength-1.0)).toInt()) {
                    var pageDescription = ""
                    pageDescription += "**💿 [${stringLimit(current.info.title).replace("[", "［").replace("]", "］")}](${current.info.uri})**\n\n"
                    pageDescription += "**"
                    val beforeCount = count
                    inner@for(i in beforeCount until maxLength+beforeCount) {
                        try {
                            var title = stringLimit(link.queue[i].track.info.title)
                            title = title.replace("[", "［").replace("]", "］")
                            pageDescription += "${i+1}.ﾠ[${title}](${link.queue[i].track.info.uri})\n"
                            count++
                        } catch (_: IndexOutOfBoundsException) { break@inner }
                    }
                    if(0 < link.queue.size-count) {
                        pageDescription += "\n+${link.queue.size-count}개"
                    }
                    pageDescription += "**"
//                println(pageDescription)
                    page.add(pageDescription)
                }
            } else { //1페이지
                var description = ""
                description += "**💿 [${stringLimit(current.info.title).replace("[", "［").replace("]", "］")}](${current.info.uri})**\n\n**"
                for(i in 0 until link.queue.size) {
                    var title = stringLimit(link.queue[i].track.info.title)
                    title = title.replace("[", "［").replace("]", "］")
                    description += "${i+1}.ﾠ[${title}](${link.queue[i].track.info.uri})\n"
                }
                description += "**"
                page.add(description)
            }
            return page
        }
    }
}