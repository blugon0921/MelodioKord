package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.kord.getLink
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger
import kotlin.math.floor

class NowCmd: Command, OnCommand {
    override val command = "now"
    override val description = "현재 재생중인 노래를 알려줍니다"
    override val options = null

    override fun on() {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        onRun(bot) {
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val link = kord.manager.getLink(interaction.guildId)

            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            interaction.respondPublic {
                embed {
                    title = "**:musical_note: 현재 재생중인 노래**"
                    description = "[**${current.info.title.replace("[", "［").replace("]", "］")}**](${current.info.uri})"
                    image = getThumbnail(current)
                    color = Settings.COLOR_NORMAL
                    field {
                        name = "**채널**"
                        value = "**`${current.info.author}`**"
                        inline = true
                    }
                    val duration = timeFormat(current.info.length)
                    var timestamp = "${timeFormat(player.position)} / $duration"
                    if(current.info.isStream) timestamp = "LIVE"
                    field {
                        name = "**길이**"
                        value = "**`${timestamp}`**"
                        inline = true
                    }

                    val barLength = 15
                    //●▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
                    val progress = floor(player.position/1000.0) / floor(current.info.length/1000.0) *100
                    var bar = ""
                    for(i in 0 until floor(progress*barLength/100.0).toInt()) {
                        bar += "▬"
                    }
                    bar+="●"
                    for(i in barLength downTo floor(progress*barLength/100.0).toInt()) {
                        bar += "▬"
                    }
                    description+="\n\n$bar"
                }
                components = mutableListOf(addThisButtons)
            }
        }
    }
}