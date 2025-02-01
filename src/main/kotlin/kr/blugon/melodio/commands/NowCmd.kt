package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import kr.blugon.kordmand.Command
import kr.blugon.lavakordqueue.TrackSourceType
import kr.blugon.lavakordqueue.sourceType
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.timeFormat
import kotlin.math.floor

class NowCmd(bot: Kord): Command(bot) {
    override val command = "now"
    override val description = "현재 재생중인 노래를 표시합니다"
    override val options = null

    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        interaction.respondPublic {
            embed {
                title = ":musical_note: 현재 재생중인 노래"
                description = current.info.displayTitle(appendArtist = false)
                image = current.info.artworkUrl
                color = Settings.COLOR_NORMAL
                field {
                    name = (if(current.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                    value = when(current.lavaSrcInfo.artistUrl != null) {
                        true -> "[${current.info.author}](${current.lavaSrcInfo.artistUrl})".bold
                        false -> current.info.author.box.bold
                    }
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
            components = mutableListOf(Buttons.addTrack)
        }
    }
}