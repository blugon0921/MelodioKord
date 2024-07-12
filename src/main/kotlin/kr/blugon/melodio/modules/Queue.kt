package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import dev.schlaubi.lavakord.audio.Event
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.PlayOptions
import kotlin.time.Duration

private val playerQueue = HashMap<ULong, Queue>()
val Link.queue: Queue
    get() {
        if(playerQueue[this.guildId] == null) playerQueue[this.guildId] = Queue(this)
        return playerQueue[this.guildId]!!
    }

suspend fun Link.addEvent() {
    val link = this
    val player = this.player

    player.on<Event, TrackEndEvent> { //END Event
        if(this.reason != Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED) return@on
        try {
            when(link.repeatMode) {
                RepeatMode.OFF -> { //반복 안할때 다음곡 재생
                    if (link.queue.isEmpty()) { //다음 노래 없으면 종료
                        link.destroyPlayer()
                        return@on
                    }
                    link.playTrack(link.queue.first().track)
                    if (link.queue.isNotEmpty()) link.queue.removeAt(0)
                    link.queue.current = null
                }
                RepeatMode.TRACK -> { //한곡 반복일때 끝난 곡 다시 재생
                    link.playTrack(track)
                }
                RepeatMode.QUEUE -> { //대기열 반복일때 다음곡 재생 후 끝난 곡 대기열에 추가
                    link.queue.add(track)
                    link.queue.current = null
                    link.playTrack(link.queue.first().track)
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
//        kordLogger.log(
//            "[${stringLimit(track.title, 40).color(LogColor.BLUE)}]노래가" +
//                 "[${guild.name.color(LogColor.BLUE)}]서버에서 재생되었습니다"
//        )
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
        link.playTrack(link.queue[0].track)
        link.queue.removeAt(0)
    }
    link.playTrack(track)
    return track
}


suspend fun Link.playTrack(track: Track, playOptions: PlayOptions.() -> Unit = {}) {
    this.player.playTrack(track) {
        playOptions(this)
        this.volume = Modules.DEFAULT_VOLUME
    }
}

class Queue(private val link: Link): ArrayList<QueueItem>() {
    val duration: Long //MS
        get() {
            if(current == null) return 0
            var duration = current!!.info.length
            for (q in this) {
                duration+=q.track.info.length
            }
            return duration
        }
    var current: Track? = null

    companion object {
        fun Queue.destroy() {
            playerQueue.remove(this.link.guildId)
        }
    }

    suspend fun add(track: Track, index: Int = size, playOptions: PlayOptions.() -> Unit = {}): Boolean { //return isFirstTrack
        if(playerQueue[link.guildId] == null) playerQueue[link.guildId] = this
//        return if(link.player.playingTrack == null) {
        return if(this.current == null) {
            link.playTrack(track) {
                playOptions(this)
                this.volume = Modules.DEFAULT_VOLUME
            }
            true
        } else {
            this.add(index, QueueItem(track) {
                playOptions(this)
                this.volume = Modules.DEFAULT_VOLUME
            })
            false
        }
    }

    suspend fun add(tracks: List<Track>, index: Int = size, playOptions: PlayOptions.() -> Unit = {}) {
        if(playerQueue[link.guildId] == null) playerQueue[link.guildId] = this
        var queueItems = mutableListOf<QueueItem>()
        for(track in tracks) {
            queueItems.add(QueueItem(track) {
                playOptions(this)
                this.volume = Modules.DEFAULT_VOLUME
            })
        }
//        if(link.player.playingTrack == null) {
        if(this.current == null) {
            queueItems = queueItems.subList(1, queueItems.size)
            link.playTrack(tracks[0]) {
                playOptions(this)
                this.volume = Modules.DEFAULT_VOLUME
            }
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