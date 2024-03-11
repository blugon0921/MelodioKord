package kr.blugon.melodio.buttons

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.volume
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.Queue.Companion.addEvent
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger

class AddThisBtn {
    val name = "addThisButton"

    init {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(name)} 버튼 불러오기 성공")
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
            link.addEvent()

            val item = link.loadItem(url)
            if(item.loadType != ResultStatus.NONE && item.loadType != ResultStatus.ERROR) {
                link.connectAudio(voiceChannel.channelId!!)
            }


            when(item) {
                is LoadResult.TrackLoaded -> {
                    val track = item.data
                    link.queue.add(track) {
                        this.volume = link.volume
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
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.PlaylistLoaded -> {
                    val playlist = item.data
                    link.queue.add(playlist.tracks) {
                        this.volume = link.volume
                    }

                    response.respond {
                        embed {
                            title = "**:musical_note: 대기열에 재생목록을 추가하였습니다**"
                            description = "[**${playlist.info.name.replace("[", "［").replace("]", "］")}**](${url})"
                            image = getThumbnail(playlist.tracks[0])
                            color = Settings.COLOR_NORMAL
                            field {
                                name = "**영상 개수**"
                                value = "**`${playlist.tracks.size}`**"
                                inline = true
                            }
                            var duration = 0L
                            playlist.tracks.forEach { track ->
                                duration+=track.info.length
                            }
                            field {
                                name = "**길이**"
                                value = "**`${Modules.timeFormat(duration)}`**"
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.SearchResult -> {
                    val track = item.data.tracks[0]
                    link.queue.add(track) {
                        this.volume = link.volume
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
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.NoMatches -> {
                    if(link.queue.current == null) link.destroy()
                    response.respond {
                        embed {
                            title = "**영상을 찾을 수 없습니다**"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
                is LoadResult.LoadFailed -> {
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
        }
    }
}