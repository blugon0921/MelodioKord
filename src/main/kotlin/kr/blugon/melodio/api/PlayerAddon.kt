package kr.blugon.melodio.api

import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import kr.blugon.melodio.api.Queue.Companion.destroy
import kr.blugon.melodio.api.Queue.Companion.queue


object PlayerAddon {
    val playerVolume = HashMap<Player, Int>()
    var Player.varVolume: Int
        get() {
            if(playerVolume[this] == null) playerVolume[this] = 50
            return playerVolume[this]!!
        }
        set(value) {
            playerVolume[this] = value
        }

    val eventAdded = HashMap<Player, Boolean>()
    var Player.isEventAdded: Boolean
        get() {
            if(eventAdded[this] == null) eventAdded[this] = false
            return eventAdded[this]!!
        }
        set(value) {
            eventAdded[this] = value
        }

    val playerRepeatMode = HashMap<Player, RepeatMode>()
    var Player.repeatMode: RepeatMode
        get() {
            if(playerRepeatMode[this] == null) playerRepeatMode[this] = RepeatMode.OFF
            return playerRepeatMode[this]!!
        }
        set(value) {
            playerRepeatMode[this] = value
        }

    val playerIsRepeatShuffle = HashMap<Player, Boolean>()
    var Player.isRepeatedShuffle: Boolean
        get() {
            if(playerIsRepeatShuffle[this] == null) playerIsRepeatShuffle[this] = false
            return playerIsRepeatShuffle[this]!!
        }
        set(value) {
            playerIsRepeatShuffle[this] = value
        }
    val playerRepeatShuffleCount = HashMap<Player, Int>()
    var Player.repeatedShuffleCount: Int
        get() {
            if(playerRepeatShuffleCount[this] == null) playerRepeatShuffleCount[this] = 0
            return playerRepeatShuffleCount[this]!!
        }
        set(value) {
            playerRepeatShuffleCount[this] = value
        }

    suspend fun Player.destroy(link: Link) {
        this.queue.destroy()
        playerVolume.remove(this)
        link.destroy()
        link.disconnectAudio()
    }
}

enum class RepeatMode {
    OFF,
    TRACK,
    QUEUE
}