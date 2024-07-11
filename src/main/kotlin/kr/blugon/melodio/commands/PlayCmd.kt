package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem
import kr.blugon.kordmand.BooleanOption
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.kordmand.StringOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.addThisButtons
import kr.blugon.melodio.Modules.bold
import kr.blugon.melodio.Modules.box
import kr.blugon.melodio.Modules.displayTitle
import kr.blugon.melodio.Modules.hyperlink
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.*
import kr.blugon.melodio.api.LinkAddon.volume
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.Queue.Companion.addEvent
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.getStringOrNull
import org.json.JSONObject
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URLEncoder

class PlayCmd: Command, OnCommand {
    override val command = "play"
    override val description = "대기열에 노래를 추가합니다"
    override val options = listOf(
        StringOption("song", "노래 제목이나 링크을 적어주세요(유튜브, 사운드클라우드)").apply {
            this.required = true
            this.autoComplete = true
        },
        BooleanOption("shuffle", "셔플 여부를 적어주세요(기본값 false)"),
        IntegerOption("index", "노래를 추가할 위치를 적어주세요(0이 바로 다음)").apply {
            this.minValue = 0
        }
    )

    override fun on() {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        onRun(bot) {
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
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
                    return@onRun
                }
            }
            val player = link.player
            var url = interaction.command.strings["song"]!!
            val isShuffle = interaction.command.booleans["shuffle"]?: false
            val index = interaction.command.integers["index"]?.toInt() ?: link.queue.size
            val response = interaction.deferPublicResponse()
            link.addEvent()
            if(!url.startsWith("http")) url = "ytsearch:$url"
            val item = link.loadItem(url)
            if(item.loadType != ResultStatus.NONE && item.loadType != ResultStatus.ERROR) {
                link.connectAudio(voiceChannel.channelId!!)
            }
            when(item) {
                is LoadResult.TrackLoaded -> {
                    val track = item.data
                    link.queue.add(track, index) {
                        this.volume = link.volume
                    }
                    response.respond {
                        embed {
                            title = ":musical_note: 대기열에 노래를 추가하였습니다".bold
                            description = track.info.displayTitle
                            image = track.info.artworkUrl
                            color = Settings.COLOR_NORMAL
                            field {
                                name = (if(track.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                                value = track.info.author.box.bold
                                inline = true
                            }
                            var duration = timeFormat(track.info.length)
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "길이".bold
                                value = duration.box.bold
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.PlaylistLoaded -> {
                    val playlist = item.data
                    link.queue.add(playlist.tracks, index) {
                        this.volume = link.volume
                    }
                    val pluginInfo = JSONObject(playlist.pluginInfo.toString())
                    response.respond {
                        embed {
                            title = ":musical_note: 대기열에 재생목록을 추가하였습니다".bold
                            description = playlist.info.name.hyperlink(pluginInfo.getStringOrNull("url")?: url)
                            image = pluginInfo.getStringOrNull("artworkUrl")?: playlist.tracks[0].info.artworkUrl
                            color = Settings.COLOR_NORMAL
                            field {
                                name = "재생목록 제작자".bold
                                value = pluginInfo.getStringOrNull("author")?.box?.bold?: "Unknown"
                                inline = true
                            }
                            field {
                                name = (if(playlist.tracks[0].info.sourceType == TrackSourceType.Spotify) "곡 개수" else "영상 개수").bold
                                value = "${playlist.tracks.size}".box.bold
                                inline = true
                            }
                            var duration = 0L
                            playlist.tracks.forEach { track ->
                                duration+=track.info.length
                            }
                            field {
                                name = "길이".bold
                                value = timeFormat(duration).box.bold
                                inline = true
                            }
                        }
                        components = mutableListOf(addThisButtons)
                    }
                }
                is LoadResult.SearchResult -> { //검색
                    val track = item.data.tracks[0]
                    link.queue.add(track, index) {
                        this.volume = link.volume
                    }
                    response.respond {
                        embed {
                            title = ":musical_note: 대기열에 노래를 추가하였습니다".bold
                            description = track.info.displayTitle
                            image = track.info.artworkUrl
                            color = Settings.COLOR_NORMAL
                            field {
                                name = (if(track.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                                value = track.info.author.box.bold
                                inline = true
                            }
                            var duration = timeFormat(track.info.length)
                            if(track.info.isStream) duration = "LIVE"
                            field {
                                name = "길이".bold
                                value = duration.box.bold
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
                            title = "영상을 찾을 수 없습니다".bold
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@onRun
                }
                is LoadResult.LoadFailed -> {
                    if(link.queue.current == null) link.destroy()
                    println(item.data)
                    response.respond {
                        embed {
                            title = "영상을 검색하는중 오류가 발생했습니다".bold
                            color = Settings.COLOR_ERROR
                        }
                    }
                    return@onRun
                }
            }
            if(player.paused) player.unPause()
            if(isShuffle) link.queue.shuffle()
        }

        onAutoComplete(bot) {
            val focusedValue = interaction.focusedOption.value
            if(focusedValue.startsWith("http") && focusedValue.contains(":")) {
                interaction.suggestString {
                    choice(focusedValue, focusedValue)
                }
                return@onAutoComplete
            }
            val response = getAutoCompletes(focusedValue)
            if(focusedValue.replace(" ", "") == "" || response.isEmpty()) {
                interaction.suggestString {
                    choice("🔍URL 또는 검색어 입력", "🔍URL 또는 검색어 입력")
                }
                return@onAutoComplete
            }
            interaction.suggestString {
                response.forEach {
                    this.choice(it, it)
                }
            }
        }
    }

    fun request(url: String): JSONArray {
        val connection = URI(url).toURL().openConnection()
        BufferedReader(InputStreamReader(connection.getInputStream())).use {
            return JSONParser().parse(it.readText()) as JSONArray
        }
    }

    fun getAutoCompletes(search: String): List<String> {
        val url = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=${URLEncoder.encode(search, Charsets.UTF_8)}"
        return arrayListOf<String>().apply {
            val data = request(url)
            (data[1] as JSONArray).forEach {
                this.add(it.toString())
            }
        }
    }
}