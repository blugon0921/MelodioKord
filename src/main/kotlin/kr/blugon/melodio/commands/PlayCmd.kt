package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.models.TrackResponse
import kr.blugon.melodio.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.varVolume
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.addEvent
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.QueueItem
import kr.blugon.melodio.api.StringOption

class PlayCmd: Command {
    override val command = "play"
    override val description = "대기열에 노래를 추가합니다"
    override val options = listOf(
        StringOption("song", "노래 제목이나 링크을 적어주세요(유튜브, 사운드클라우드, Spotify)") {
            this.required = true
        }
    )

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

            val link = bot.manager.getLink(interaction.guildId.value)
            if(link.voiceChannel == null) link.voiceChannel = voiceChannel.channelId
            if(link.state == Link.State.CONNECTED || link.state == Link.State.CONNECTING) { //이미 연결 되어 있으면
                if(voiceChannel.channelId != link.voiceChannel) {
                    interaction.respondEphemeral {
                        embed {
                            title = "**봇과 같은 음성 채널에 접속해있지 않습니다**"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
            }

            val player = link.player
            var url = interaction.command.strings["song"]
            val file = interaction.command.attachments["file"]
            val isShuffle = interaction.command.booleans["shuffle"]?: false

            if(file != null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**아직 지원하지 않는 기능입니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }
            if(url == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생할 노래를 적어주세요**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val response = interaction.deferPublicResponse()
            link.addEvent()

            if(!url.startsWith("http")) {
                url = "ytsearch:$url"
            }
            val item = link.loadItem(url)
            if(item.loadType != TrackResponse.LoadType.NO_MATCHES && item.loadType != TrackResponse.LoadType.LOAD_FAILED) {
                link.connectAudio(voiceChannel.channelId!!)
            }

            when(item.loadType) {
                TrackResponse.LoadType.TRACK_LOADED -> {
                    val track = item.tracks.first()
                    link.queue.add(track) {
                        this.volume = link.varVolume
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
                            var duration = timeFormat(track.info.length)
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "**길이**"
                                value = "**`${duration}`**"
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                TrackResponse.LoadType.PLAYLIST_LOADED -> {
                    val tracks = item.tracks
                    link.queue.add(tracks[0]) {
                        this.volume = link.varVolume
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
                                value = "**`${timeFormat(duration)}`**"
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                    for(i in 1 until tracks.size) {
                        link.queue.add(QueueItem(tracks[i]) {
                            this.volume = link.varVolume
                        })
                    }
                }
                TrackResponse.LoadType.SEARCH_RESULT -> {
                    val track = item.tracks.first()
                    link.queue.add(track) {
                        this.volume = link.varVolume
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
                            var duration = timeFormat(track.info.length)
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "**길이**"
                                value = "**`${duration}`**"
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                TrackResponse.LoadType.NO_MATCHES -> {
                    if(link.queue.current == null) link.destroy()
                    response.respond {
                        embed {
                            title = "**영상을 찾을 수 없습니다**"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
                TrackResponse.LoadType.LOAD_FAILED -> {
                    if(link.queue.current == null) link.destroy()
                    response.respond {
                        embed {
                            title = "**영상을 검색하는중 오류가 발생했습니다**"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
            }
            if(player.paused) player.unPause()
            if(isShuffle) link.queue.shuffle()
        }
    }
}