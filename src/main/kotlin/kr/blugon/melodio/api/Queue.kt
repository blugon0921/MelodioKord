package kr.blugon.melodio.api

import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.PlayOptions
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.hyperlink
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.api.LinkAddon.destroyPlayer
import kr.blugon.melodio.api.LinkAddon.isEventAdded
import kr.blugon.melodio.api.LinkAddon.isRepeatedShuffle
import kr.blugon.melodio.api.LinkAddon.repeatMode
import kr.blugon.melodio.api.LinkAddon.repeatedShuffleCount
import kr.blugon.melodio.api.LinkAddon.setGuild
import kr.blugon.melodio.api.LinkAddon.volume
import kr.blugon.melodio.api.Queue.Companion.queue
import kotlin.time.Duration

class Queue(private val link: Link): ArrayList<QueueItem>() {
    val duration: Long //MS
        get() {
            if(link.player.playingTrack == null) return 0L
            var ms = link.player.playingTrack!!.info.length
            for(t in this) {
                ms+=t.track.info.length
            }
            return ms
        }
    var current: Track? = null

    companion object {
        private val playerQueue = HashMap<ULong, Queue>()
        val Link.queue: Queue
            get() {
                if(playerQueue[this.guildId] == null) playerQueue[this.guildId] = Queue(this)
                return playerQueue[this.guildId]!!
            }

        fun Queue.destroy() {
            playerQueue.remove(this.link.guildId)
        }

        suspend fun Link.addEvent() {
            val link = this
            val player = this.player
            link.setGuild(bot.getGuild(Snowflake(link.guildId)))
            if(link.isEventAdded()) return
            link.isEventAdded(true)

            player.on<Event, TrackEndEvent> { //END Event
                link.queue.current = null
                if(this.reason != Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED) return@on
                try {
                    when(link.repeatMode) {
                        RepeatMode.OFF -> { //반복 안할때 다음곡 재생
                            if (link.queue.isEmpty()) { //다음 노래 없으면 종료
                                link.destroyPlayer()
                                return@on
                            }
                            link.player.playTrack(link.queue.first().track) {
                                this.volume = link.volume
                            }
                            if (link.queue.isNotEmpty()) link.queue.removeAt(0)
                        }
                        RepeatMode.TRACK -> { //한곡 반복일때 끝난 곡 다시 재생
                            player.playTrack(track) {
                                this.volume = link.volume
                                end = Duration.parse("${track.info.length}ms")
                            }
                        }
                        RepeatMode.QUEUE -> { //대기열 반복일때 다음곡 재생 후 끝난 곡 대기열에 추가
                            link.queue.add(QueueItem(track) {
                                this.volume = link.volume
                                end = Duration.parse("${track.info.length}ms")
                            })
                            player.playTrack(link.queue[0].track) {
                                this.volume = link.volume
                                end = Duration.parse("${link.queue[0].track.info.length}ms")
                            }
                            if (link.queue.isNotEmpty()) link.queue.removeAt(0)
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {
                    println(e.message)
                    link.destroyPlayer()
                }
            }
            player.on<Event, TrackStartEvent> { //StartEvent
                link.queue.current = track
                if(link.isRepeatedShuffle) {
                    if(link.repeatMode != RepeatMode.QUEUE) {
                        link.isRepeatedShuffle = false
                        link.repeatedShuffleCount = 0
                        return@on
                    }
                    if(link.queue.size <= link.repeatedShuffleCount) {
                        link.repeatedShuffleCount = 0
                        link.queue.shuffle()
                    } else link.repeatedShuffleCount++
                }
//                kordLogger.log(
//                    "[${stringLimit(track.title, 40).color(LogColor.BLUE)}]노래가" +
//                         "[${guild.name.color(LogColor.BLUE)}]서버에서 재생되었습니다"
//                )
            }
        }

        suspend fun Link.skip(count: Int = 1): Track? {
            val link = this
            if(link.queue.isEmpty()) {
                link.destroyPlayer()
                return null
            }
            link.player.stopTrack()

            lateinit var track: Track
            for(i in 0 until count) {
                if(i != count-1) {
                    link.queue.removeAt(0)
                    continue
                }
                track = link.queue[0].track
                link.player.playTrack(link.queue[0].track) {
                    this.volume = link.volume
                }
                link.queue.removeAt(0)
            }
//            while(link.queue.current == null) {
//                link.player.playTrack(track) {
//                    this.volume = link.volume
//                }
//            }
            link.player.playTrack(track) {
                this.volume = link.volume
            }
            return track
        }
    }

    suspend fun add(track: Track, index: Int = size, playOptions: PlayOptions.() -> Unit = {}): Boolean { //return isFirstTrack
        if(playerQueue[link.guildId] == null) playerQueue[link.guildId] = this
        return if(link.player.playingTrack == null) {
            link.player.playTrack(track, playOptions)
            true
        } else {
            this.add(index, QueueItem(track, playOptions))
            false
        }
    }

    suspend fun add(tracks: List<Track>, index: Int = size, playOptions: PlayOptions.() -> Unit = {}) {
        if(playerQueue[link.guildId] == null) playerQueue[link.guildId] = this
        var queueItems = mutableListOf<QueueItem>()
        for(track in tracks) {
            queueItems.add(QueueItem(track, playOptions))
        }
        if(link.player.playingTrack == null) {
            queueItems = queueItems.subList(1, queueItems.size)
            link.player.playTrack(tracks[0], playOptions)
        }
        this.addAll(index, queueItems)
    }
}

data class QueueItem(val track: Track, val option: PlayOptions.() -> Unit) {
    val type = TrackSourceType.get(track.info.sourceName)
}

val TrackInfo.sourceType: TrackSourceType get() = TrackSourceType.get(this.sourceName)
enum class TrackSourceType {
    Youtube,
    Spotify,
    HTTP;

    companion object {
        fun get(name: String): TrackSourceType {
            return when (name.lowercase()) {
                Youtube.name.lowercase() -> Youtube
                Spotify.name.lowercase() -> Spotify
                HTTP.name.lowercase() -> HTTP
                else -> throw IllegalArgumentException("Unknown QueueItemType: $name")
            }
        }
    }
}