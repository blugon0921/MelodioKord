package kr.blugon.melodio.buttons

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.models.TrackResponse
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Modules.usedUser
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.color
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.PlayerAddon.destroy
import kr.blugon.melodio.api.PlayerAddon.isEventAdded
import kr.blugon.melodio.api.PlayerAddon.repeatMode
import kr.blugon.melodio.api.PlayerAddon.varVolume
import kr.blugon.melodio.api.Queue.Companion.addEvent
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.QueueItem
import kr.blugon.melodio.api.RepeatMode
import java.lang.IndexOutOfBoundsException
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AddThisBtn {
    val name = "addThisButton"

    fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(name)} 버튼 불러오기 성공")
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

            val player = link.player
            var url = interaction.message.embeds[0].description!!
            var urlStart = 0
            for(i in 7 until url.length) {
                if(
                    url[i] != 'h' ||
                    url[i+1] != 't' ||
                    url[i+2] != 't' ||
                    url[i+3] != 'p' ||
                    url[i+4] != 's' ||
                    url[i+5] != ':' ||
                    url[i+6] != '/' ||
                    url[i+7] != '/'
                ) continue
                urlStart = i
                break
            }

            // by Play command
            url = if(interaction.message.embeds[0].title != "**:musical_note: 현재 재생중인 노래**") url.substring(urlStart, url.length-1)
            // by Now command
                else url.substring(urlStart, url.length-20)
            val response = interaction.deferPublicResponse()

            val item = link.loadItem(url)

            if(!player.isEventAdded) {
                player.addEvent(link)
                player.isEventAdded = true
            }
            if(item.loadType != TrackResponse.LoadType.NO_MATCHES && item.loadType != TrackResponse.LoadType.LOAD_FAILED) {
                link.connectAudio(voiceChannel.channelId!!)
            }
            when(item.loadType) {
                TrackResponse.LoadType.TRACK_LOADED -> {
                    val track = item.tracks.first()
                    player.queue.add(track) {
                        this.volume = player.varVolume
                    }
                    response.respond {
                        embed {
                            title = "**:musical_note: 대기열에 노래를 추가하였습니다**"
                            description = "[**${track.info.title.replace("[", "［").replace("]", "］")}**](${track.info.uri})"
                            image = getThumbnail(track)
                            color = Settings.COLOR_NORMAL
                            field {
                                name = "**채널**"
                                value = "**`${track.info.author}`**"
                                inline = true
                            }
                            var duration = Modules.timeFormat(track.info.length)
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "**길이**"
                                value = "**`${duration}`**"
                                inline = true
                            }
                            this.usedUser(interaction)
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                TrackResponse.LoadType.PLAYLIST_LOADED -> {
                    val tracks = item.tracks
                    player.queue.add(tracks[0]) {
                        this.volume = player.varVolume
                    }
                    response.respond {
                        embed {
                            title = "**:musical_note: 대기열에 재생목록을 추가하였습니다**"
                            description = "[**${item.getPlaylistInfo().name.replace("[", "［").replace("]", "］")}**](${url})"
                            image = getThumbnail(tracks[0])
                            color = Settings.COLOR_NORMAL
                            field {
                                name = "**영상 개수**"
                                value = "**`${tracks.size}`**"
                                inline = true
                            }
                            var duration = 0L
                            tracks.forEach { track ->
                                duration+=track.info.length
                            }
                            field {
                                name = "**길이**"
                                value = "**`${Modules.timeFormat(duration)}`**"
                                inline = true
                            }
                            this.usedUser(interaction)
                        }
                        components = mutableListOf(addThisButtons)
                    }
                    for(i in 1 until tracks.size) {
                        player.queue.add(QueueItem(tracks[i]) {
                            this.volume = player.varVolume
                        })
                    }
                }
                TrackResponse.LoadType.SEARCH_RESULT -> {
                    val track = item.tracks.first()
                    player.queue.add(track) {
                        this.volume = player.varVolume
                    }
                    response.respond {
                        embed {
                            title = "**:musical_note: 대기열에 노래를 추가하였습니다**"
                            description = "[**${track.info.title.replace("[", "［").replace("]", "］")}**](${track.info.uri})"
                            image = getThumbnail(track)
                            color = Settings.COLOR_NORMAL
                            field {
                                name = "**채널**"
                                value = "**`${track.info.author}`**"
                                inline = true
                            }
                            var duration = "${track.info.length}"
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "**길이**"
                                value = "**`${duration}`**"
                                inline = true
                            }
                            this.usedUser(interaction)
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                TrackResponse.LoadType.NO_MATCHES -> {
                    if(player.playingTrack == null) link.destroy()
                    response.respond {
                        embed {
                            title = "**영상을 찾을 수 없습니다**"
                            color = Settings.COLOR_ERROR
                            this.usedUser(interaction)
                        }
                    }
                    return@on
                }
                TrackResponse.LoadType.LOAD_FAILED -> {
                    if(player.playingTrack == null) link.destroy()
                    response.respond {
                        embed {
                            title = "**영상을 검색하는중 오류가 발생했습니다**"
                            color = Settings.COLOR_ERROR
                            this.usedUser(interaction)
                        }
                    }
                    return@on
                }
            }
        }
    }
}