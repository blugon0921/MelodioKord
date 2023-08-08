package kr.blugon.melodio.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.kordLogger
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.PlayOptions
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.models.PartialTrack
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.api.LogColor.color
import kr.blugon.melodio.api.PlayerAddon.destroy
import kr.blugon.melodio.api.PlayerAddon.isRepeatedShuffle
import kr.blugon.melodio.api.PlayerAddon.repeatMode
import kr.blugon.melodio.api.PlayerAddon.repeatedShuffleCount
import kr.blugon.melodio.api.PlayerAddon.varVolume
import java.lang.IndexOutOfBoundsException

//import dev.schlaubi.lavakord.rest.models.PartialTrack

class Queue(val player: Player): ArrayList<QueueItem>() {
    val duration: Long //MS
        get() {
            if(player.playingTrack == null) return 0L
            var ms = player.playingTrack!!.length.inWholeMilliseconds
            for(t in this) {
                ms+=t.track.length.inWholeMilliseconds
            }
            return ms
        }
    val current: Track?
        get() {
            return player.playingTrack
        }

    companion object {
        val playerQueue = HashMap<Player, Queue>()
        val Player.queue: Queue
            get() {
                if(playerQueue[this] == null) playerQueue[this] = Queue(this)
                return playerQueue[this]!!
            }

        fun Queue.destroy() {
            playerQueue.remove(this.player)
        }

        suspend fun Player.addEvent(link: Link) {
            val player = this
            player.on<Event, TrackStartEvent> event@{
                val guild = bot.getGuild(Snowflake(this.guildId))
                val track = this.getTrack()
                if(player.isRepeatedShuffle) {
                    if(player.repeatMode != RepeatMode.QUEUE) {
                        player.isRepeatedShuffle = false
                        player.repeatedShuffleCount = 0
                        return@event
                    }
                    if(player.queue.size <= player.repeatedShuffleCount) {
                        player.repeatedShuffleCount = 0
                        player.queue.shuffle()
                    } else player.repeatedShuffleCount++
                }
//                kordLogger.log(
//                    "[${stringLimit(track.title, 40).color(LogColor.BLUE)}]노래가" +
//                         "[${guild.name.color(LogColor.BLUE)}]서버에서 재생되었습니다"
//                )
            }
            player.on<Event, TrackEndEvent> {
                val track = this.getTrack()
                try {
                    when(player.repeatMode) {
                        RepeatMode.OFF -> { //반복 안할때 다음곡 재생
                            if (player.queue.isEmpty()) player.destroy(link) //다음 노래 없으면 종료
                            player.playTrack(player.queue[0].track) {
                                this.volume = player.varVolume
                            }
                            if (player.queue.isNotEmpty()) player.queue.removeAt(0)
                        }
                        RepeatMode.TRACK -> { //한곡 반복일때 끝난 곡 다시 재생
                            player.playTrack(track) {
                                this.volume = player.varVolume
                                end = track.length
                            }
                        }
                        RepeatMode.QUEUE -> { //대기열 반복일때 다음곡 재생 후 끝난 곡 대기열에 추가
                            player.queue.add(QueueItem(track) {
                                this.volume = player.varVolume
                                end = track.length
                            })
                            player.playTrack(player.queue[0].track) {
                                this.volume = player.varVolume
                                end = player.queue[0].track.length
                            }
                            if (player.queue.isNotEmpty()) player.queue.removeAt(0)
                        }
                    }
                } catch (e: IndexOutOfBoundsException) { player.destroy(link) }
            }
        }
    }

    suspend fun add(track: QueueItem, playOptions: PlayOptions.() -> Unit = {}): Boolean = add(track.track, playOptions) //return isFirstTrack
    suspend fun add(track: PartialTrack, playOptions: PlayOptions.() -> Unit = {}): Boolean = add(track.toTrack(), playOptions) //return isFirstTrack
    suspend fun add(track: Track, playOptions: PlayOptions.() -> Unit = {}): Boolean { //return isFirstTrack
        if(playerQueue[this.player] == null) playerQueue[this.player] = this
        return if(this.player.playingTrack == null) {
            this.player.playTrack(track, playOptions)
            true
        } else {
            playerQueue[this.player]!!.add(QueueItem(track, playOptions))
            false
        }
    }
}

data class QueueItem(val track: Track, val option: PlayOptions.() -> Unit)
suspend fun QueueItem(track: PartialTrack, option: PlayOptions.() -> Unit): QueueItem {
    return QueueItem(track.toTrack(), option)
}