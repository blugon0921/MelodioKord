package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.guildId
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.getThumbnail
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.*
import kr.blugon.melodio.api.LinkAddon.varVolume
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.addEvent
import kr.blugon.melodio.api.Queue.Companion.queue

class PlayCmd: Command, Runnable, AutoComplete {
    override val command = "play"
    override val description = "대기열에 노래를 추가합니다"
    override val options = listOf(
        StringOption("song", "노래 제목이나 링크을 적어주세요(유튜브, 사운드클라우드)").apply {
            this.required = true
        },
        BooleanOption("shuffle", "셔플 여부를 적어주세요(기본값 false)"),
        IntegerOption("index", "노래를 추가할 위치를 적어주세요(0이 바로 다음)").apply {
            this.minValue = 0
        }
    )

     override fun run() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName != command) return@on
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val link = bot.manager.getLink(interaction.guildId)
            if(link.voiceChannel == null) link.voiceChannel = voiceChannel.channelId
            if(link.state == Link.State.CONNECTED || link.state == Link.State.CONNECTING) { //이미 연결 되어 있으면
                if(voiceChannel.channelId != link.voiceChannel) {
                    interaction.respondEphemeral {
                        embed {
                            title = "봇과 같은 음성 채널에 접속해있지 않습니다"
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@on
                }
            }

            val player = link.player
            var url = interaction.command.strings["song"]!!
            val isShuffle = interaction.command.booleans["shuffle"]?: false
            val index = (interaction.command.integers["index"] ?: ((link.queue.size - 1))).toInt()

            val response = interaction.deferPublicResponse()
            link.addEvent()

            if(!url.startsWith("http")) {
                url = "ytsearch:$url"
            }
            val item = link.loadItem(url)
            if(item.loadType != ResultStatus.NONE && item.loadType != ResultStatus.ERROR) {
                link.connectAudio(voiceChannel.channelId!!)
            }


            when(item) {
                is LoadResult.TrackLoaded -> {
                    val track = item.data
                    link.queue.add(track, index) {
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
                is LoadResult.PlaylistLoaded -> {
                    val playlist = item.data
                    link.queue.add(playlist.tracks, index) {
                        this.volume = link.varVolume
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
                                value = "**`${timeFormat(duration)}`**"
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.SearchResult -> {
                    val track = item.data.tracks[0]
                    link.queue.add(track, index) {
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
            if(player.paused) player.unPause()
            if(isShuffle) link.queue.shuffle()
        }
    }

    override fun autocomplete() {
        bot.on<AutoCompleteInteractionCreateEvent> {
            if(interaction.command.rootName != command) return@on
            val focusedValue = interaction.focusedOption.value
            if(focusedValue.startsWith("http") && focusedValue.contains(":")) {
                //TODO(아니 Kord에 자동완성 보낼 수 있는 코드가 없음;;;)
            }
        }
    }
}