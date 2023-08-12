package kr.blugon.melodio.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.PlayOptions
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.models.PartialTrack
import kotlinx.coroutines.delay
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.api.LinkAddon.destroyPlayer
import kr.blugon.melodio.api.LinkAddon.getGuild
import kr.blugon.melodio.api.LinkAddon.isEventAdded
import kr.blugon.melodio.api.LinkAddon.isRepeatedShuffle
import kr.blugon.melodio.api.LinkAddon.repeatMode
import kr.blugon.melodio.api.LinkAddon.repeatedShuffleCount
import kr.blugon.melodio.api.LinkAddon.setGuild
import kr.blugon.melodio.api.LinkAddon.varVolume
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.commands.PartLoopCmd.Companion.partloopThread

//import dev.schlaubi.lavakord.rest.models.PartialTrack

class Queue(val link: Link): ArrayList<QueueItem>() {
    val duration: Long //MS
        get() {
            if(link.player.playingTrack == null) return 0L
            var ms = link.player.playingTrack!!.length.inWholeMilliseconds
            for(t in this) {
                ms+=t.track.length.inWholeMilliseconds
            }
            return ms
        }
    var current: Track? = null

    companion object {
        val playerQueue = HashMap<Snowflake, Queue>()
        val Link.queue: Queue
            get() {
                if(playerQueue[Snowflake(this.guildId)] == null) playerQueue[Snowflake(this.guildId)] = Queue(this)
                return playerQueue[Snowflake(this.guildId)]!!
            }

        fun Queue.destroy() {
            playerQueue.remove(Snowflake(this.link.guildId))
        }

        suspend fun Link.addEvent() {
            val link = this
            val player = this.player
            link.setGuild(bot.getGuild(Snowflake(link.guildId)))
            if(link.isEventAdded()) return
            link.isEventAdded(true)

            player.on<Event, TrackEndEvent> { //END Event
                val track = this.getTrack()
                link.queue.current = null
                if(this.reason != TrackEndEvent.EndReason.FINISHED) return@on
                if(link.partloopThread != null) {
                    link.partloopThread!!.stopFlag = true
                    link.partloopThread = null
                }
                try {
                    when(link.repeatMode) {
                        RepeatMode.OFF -> { //반복 안할때 다음곡 재생
                            if (link.queue.isEmpty()) { //다음 노래 없으면 종료
                                link.destroyPlayer()
                                return@on
                            }
                            link.player.playTrack(link.queue.first().track) {
                                this.volume = link.varVolume
                            }
                            if (link.queue.isNotEmpty()) link.queue.removeAt(0)
                        }
                        RepeatMode.TRACK -> { //한곡 반복일때 끝난 곡 다시 재생
                            player.playTrack(track) {
                                this.volume = link.varVolume
                                end = track.length
                            }
                        }
                        RepeatMode.QUEUE -> { //대기열 반복일때 다음곡 재생 후 끝난 곡 대기열에 추가
                            link.queue.add(QueueItem(track) {
                                this.volume = link.varVolume
                                end = track.length
                            })
                            player.playTrack(link.queue[0].track) {
                                this.volume = link.varVolume
                                end = link.queue[0].track.length
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
                val guild = bot.getGuild(Snowflake(this.guildId))
                val track = this.getTrack()
                delay(100)
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

            lateinit var track: Track
            for(i in 0 until count) {
                if(i != count-1) {
                    link.queue.removeAt(0)
                    continue
                }
                track = link.queue[0].track
                link.player.playTrack(link.queue[0].track) {
                    this.volume = link.varVolume
                }
                link.queue.removeAt(0)
            }
            while(link.queue.current == null) {
                link.player.playTrack(track) {
                    this.volume = link.varVolume
                }
            }
            return track
        }
    }

    suspend fun add(track: QueueItem, playOptions: PlayOptions.() -> Unit = {}): Boolean = add(track.track, playOptions) //return isFirstTrack
    suspend fun add(track: PartialTrack, playOptions: PlayOptions.() -> Unit = {}): Boolean = add(track.toTrack(), playOptions) //return isFirstTrack
    suspend fun add(track: Track, playOptions: PlayOptions.() -> Unit = {}): Boolean { //return isFirstTrack
        if(playerQueue[Snowflake(link.guildId)] == null) playerQueue[Snowflake(link.guildId)] = this
        return if(link.player.playingTrack == null) {
            link.player.playTrack(track, playOptions)
            true
        } else {
            playerQueue[Snowflake(link.guildId)]!!.add(QueueItem(track, playOptions))
            false
        }
    }
}

data class QueueItem(val track: Track, val option: PlayOptions.() -> Unit)
suspend fun QueueItem(track: PartialTrack, option: PlayOptions.() -> Unit): QueueItem {
    return QueueItem(track.toTrack(), option)
}