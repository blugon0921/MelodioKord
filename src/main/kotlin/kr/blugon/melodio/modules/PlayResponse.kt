package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import kr.blugon.melodio.Main
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Modules.addThisButtons
import kr.blugon.melodio.modules.Modules.timeFormat

suspend fun DeferredPublicMessageInteractionResponseBehavior.completePlay(item: LoadResult, link: Link, url: String, index: Int = -1, isShuffle: Boolean = false) {
    when(item) {
        is LoadResult.TrackLoaded -> {
            val track = item.data
            link.queue.add(track, if(index == -1) link.queue.size else index)
            this.respond {
                embed {
                    title = ":musical_note: 대기열에 노래를 추가하였습니다"
                    description = track.info.displayTitle(appendArtist = false)
                    image = track.info.artworkUrl
                    color = Settings.COLOR_NORMAL
                    field {
                        name = (if(track.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                        value = when(track.lavaSrcInfo.artistUrl != null) {
                            true -> "[${track.info.author}](${track.lavaSrcInfo.artistUrl})".bold
                            false -> track.info.author.box.bold
                        }
                        inline = true
                    }
                    var duration = timeFormat(track.info.length)
                    if(track.info.isStream) duration = "LIVE"
                    field {
                        name = "길이".bold
                        value = duration.bold
                        inline = true
                    }
                }
                components = mutableListOf(addThisButtons)
            }
        }
        is LoadResult.PlaylistLoaded -> {
            val playlist = item.data
            link.queue.add(
                if(isShuffle) playlist.tracks.shuffled()
                else playlist.tracks
            , if(index == -1) link.queue.size else index)
            this.respond {
                embed {
                    title = ":musical_note: 대기열에 재생목록을 추가하였습니다"
                    description = playlist.info.name.hyperlink(playlist.lavaSrcInfo.url?: url)
                    image = playlist.lavaSrcInfo.artworkUrl?: playlist.tracks[0].info.artworkUrl
                    color = Settings.COLOR_NORMAL
                    field {
                        name = "재생목록 제작자".bold
                        value = (playlist.lavaSrcInfo.author?: "Unknown").box.bold
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
            link.queue.add(track, if(index == -1) link.queue.size else index)
            this.respond {
                embed {
                    title = ":musical_note: 대기열에 노래를 추가하였습니다"
                    description = track.info.displayTitle(appendArtist = false)
                    image = track.info.artworkUrl
                    color = Settings.COLOR_NORMAL
                    field {
                        name = (if(track.info.sourceType == TrackSourceType.Spotify) "아티스트" else "채널").bold
                        value = when(track.lavaSrcInfo.artistUrl != null) {
                            true -> "[${track.info.author}](${track.lavaSrcInfo.artistUrl})".bold
                            false -> track.info.author.box.bold
                        }
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
            this.respond { embeds = mutableListOf(errorEmbed(Messages.NO_SEARCH_RESULT)) }
            return
        }
        is LoadResult.LoadFailed -> {
            if(link.queue.current == null) link.destroy()
            println(item.data)
            this.respond { embeds = mutableListOf(errorEmbed(Messages.SEARCH_EXCEPTION)) }
            return
        }
    }
    if(link.player.paused) link.player.unPause()
    if(isShuffle) link.queue.shuffle()
}