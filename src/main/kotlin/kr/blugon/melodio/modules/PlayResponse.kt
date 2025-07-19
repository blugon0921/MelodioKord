package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Playlist
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import kotlinx.coroutines.delay
import kr.blugon.lavakordqueue.TrackSourceType
import kr.blugon.lavakordqueue.queue
import kr.blugon.lavakordqueue.sourceType
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Modules.timeFormat

suspend fun DeferredPublicMessageInteractionResponseBehavior.completePlay(
    item: LoadResult,
    link: Link,
    url: String,
    index: Int = -1,
    isShuffle: Boolean = false,
    channel: MessageChannelBehavior
) {
    val data = item.data
    when(item) {
        is LoadResult.TrackLoaded -> {
            link.queue.add((data as Track), if(index == -1) link.queue.size else index)  //data is Track
            respond {
                embed {
                    title = ":musical_note: 대기열에 노래를 추가하였습니다"
                    description = data.info.displayTitle(appendArtist = false)
                    this.thumbnail {
                        this.url = data.info.artworkUrl?: return@thumbnail
                    }
                    color = Settings.COLOR_NORMAL
                    field {
                        name = (if(data.info.sourceType == TrackSourceType.Youtube) "채널" else "아티스트").bold
                        value = when(data.lavaSrcInfo.artistUrl != null) {
                            true -> "[${data.info.author}](${data.lavaSrcInfo.artistUrl})".bold
                            false -> data.info.author.box.bold
                        }
                        inline = true
                    }
                    var duration = timeFormat(data.info.length)
                    if(data.info.isStream) duration = "LIVE"
                    field {
                        name = "길이".bold
                        value = duration.bold
                        inline = true
                    }
                }
                components = mutableListOf(Buttons.addTrack)
            }
        }
        is LoadResult.PlaylistLoaded -> {
            val tracks = (data as Playlist).tracks //data is Playlist
            link.queue.add(
                if(isShuffle) tracks.shuffled() else tracks,
                if(index == -1) link.queue.size else index
            )
            respond {
                embed {
                    title = ":musical_note: 대기열에 재생목록을 추가하였습니다"
                    description = data.info.name.hyperlink(data.lavaSrcInfo.url?: url)
                    thumbnail {
                        this.url = (data.lavaSrcInfo.artworkUrl?: tracks[0].info.artworkUrl)?: return@thumbnail
                    }
                    color = Settings.COLOR_NORMAL
                    field {
                        name = "재생목록 제작자".bold
                        value = (data.lavaSrcInfo.author?: "Unknown").box.bold
                        inline = true
                    }
                    field {
                        name = (if(tracks[0].info.sourceType == TrackSourceType.Spotify) "곡 개수" else "영상 개수").bold
                        value = "${tracks.size}".box.bold
                        inline = true
                    }
                    var duration = 0L
                    tracks.forEach { track ->
                        duration+=track.info.length
                    }
                    field {
                        name = "길이".bold
                        value = timeFormat(duration).box.bold
                        inline = true
                    }
                }
                components = mutableListOf(Buttons.addTrack)
            }
        }
        is LoadResult.SearchResult -> { //검색
            val track = item.data.tracks.firstOrNull()?: run {
                if(link.queue.current == null) link.destroyPlayer()
                return respondEmbed(errorEmbed(Messages.NO_SEARCH_RESULT))
            }
            link.queue.add(track, if(index == -1) link.queue.size else index)
            respond {
                embed {
                    title = ":musical_note: 대기열에 노래를 추가하였습니다"
                    description = track.info.displayTitle(appendArtist = false)
                    this.thumbnail {
                        this.url = track.info.artworkUrl?: return@thumbnail
                    }
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
                components = mutableListOf(Buttons.addTrack)
            }
        }
        is LoadResult.NoMatches -> {
            if(link.queue.current == null) link.destroyPlayer()
            return respondEmbed(errorEmbed(Messages.NO_SEARCH_RESULT))
        }
        is LoadResult.LoadFailed -> {
            if(link.queue.current == null) link.destroyPlayer()
            Logger.error(item.data)
            return respondEmbed(errorEmbed(Messages.SEARCH_EXCEPTION))
        }
    }
    if(link.player.paused) link.player.unPause()
    if(isShuffle) link.queue.shuffle()
    delay(1000)
    Buttons.resendController(link, channel)
}