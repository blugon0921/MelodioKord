package kr.blugon.melodio.commands

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.audio.player.Track
import kr.blugon.melodio.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue
import java.lang.IndexOutOfBoundsException
import kotlin.math.ceil

class QueueCmd: Command {
    override val command = "queue"
    override val description = "현재 대기열을 보여줍니다"
    override val options = null

    val pageItemCount = 20

    suspend fun execute() {
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
            if(link.state != Link.State.CONNECTED && link.state != Link.State.CONNECTING) {
                interaction.respondEphemeral {
                    embed {
                        title = "**봇이 음성 채널에 접속해 있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val player = link.player

            val current = player.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            if(player.queue.isEmpty()) {
                interaction.respondPublic {
                    embed {
                        title = "**:clipboard: 현재 대기열 [${timeFormat(player.queue.duration)}]**"
                        color = Settings.COLOR_NORMAL
                        description = "**💿 [${stringLimit(current.title).replace("[", "［").replace("]", "［")}](${current.uri})\n**"
                    }
                    components.add(buttons)
                }
                return@on
            }

            val page = queuePage(player, pageItemCount, current)

            interaction.respondPublic {
                embed {
                    title = "**:clipboard: 현재 대기열 [${timeFormat(player.queue.duration)}]**"
                    color = Settings.COLOR_NORMAL
                    description = page[0]
                    footer {
                        text = "페이지 1/${page.size}"
                    }
                }
                components.add(ActionRowBuilder().apply {
                    this.interactionButton(ButtonStyle.Primary, "beforePage") {
                        this.label = "◀이전"
                        this.disabled = true
                    }
                    this.interactionButton(ButtonStyle.Primary, "nextPage") {
                        this.label = "다음▶"
                        if(page.size == 1) this.disabled = true
                    }
                    this.interactionButton(ButtonStyle.Primary, "reloadPage") {
                        this.label = "🔄️새로고침"
                    }
                })
                components.add(buttons)
            }
        }
    }

    fun queuePage(player: Player, maxLength: Int, current: Track): List<String> {
        val page = ArrayList<String>()
        if(maxLength < player.queue.size) { //2페이지 이상
            var count = 0
            for(p in 0 until  ceil(player.queue.size/maxLength+0.0).toInt()) {
                var pageDescription = ""
                pageDescription += "**💿 [${stringLimit(current.title).replace("[", "［").replace("]", "］")}](${current.uri})**\n\n"
                pageDescription += "**"
                val beforeCount = count
                for(i in beforeCount..maxLength+beforeCount) {
                    try {
                        var title = stringLimit(player.queue[i].track.title)
                        title = title.replace("[", "［").replace("]", "］")
                        pageDescription += "${i+1}.ﾠ[${title}](${player.queue[i].track.uri})\n"
                        count++
                    } catch (_: IndexOutOfBoundsException) { break }
                }
                if(0 < player.queue.size-count) {
                    pageDescription += "\n+${player.queue.size-count}개"
                }
                pageDescription += "**"
                page.add(pageDescription)
            }
        } else { //1페이지
            var description = ""
            description += "**💿 [${stringLimit(current.title).replace("[", "［").replace("]", "］")}](${current.uri})**\n\n**"
            for(i in 0 until player.queue.size) {
                var title = stringLimit(player.queue[i].track.title)
                title = title.replace("[", "［").replace("]", "］")
                description += "${i+1}.ﾠ[${title}](${player.queue[i].track.uri})\n"
            }
            description += "**"
            page.add(description)
        }
        return page
    }
}