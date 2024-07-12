package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.kord.getLink
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.addThisButtons
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.box
import kr.blugon.melodio.modules.Modules.displayTitle
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.modules.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*
import kotlin.math.floor

class NowCmd: Command, Registable {
    override val command = "now"
    override val description = "현재 재생중인 노래를 알려줍니다"
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

            val link = kord.manager.getLink(interaction.guildId)

            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun

            val player = link.player

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

            interaction.respondPublic {
                embed {
                    title = ":musical_note: 현재 재생중인 노래".bold
                    description = current.info.displayTitle
                    image = current.info.artworkUrl
                    color = Settings.COLOR_NORMAL
                    field {
                        name = (if(current.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                        value = current.info.author.box.bold
                        inline = true
                    }
                    val duration = timeFormat(current.info.length)
                    var timestamp = "${timeFormat(player.position)} / $duration"
                    if(current.info.isStream) timestamp = "LIVE"
                    field {
                        name = "길이".bold
                        value = timestamp.box.bold
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