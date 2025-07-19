package kr.blugon.melodio.modules

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.destoryScopeRunning
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.playerDestoryScopeRunning


var Link.repeatMode: RepeatMode
    get() = this.queue.repeatMode
    set(value) {
        this.queue.repeatMode = value
    }

private val _isRepeatedShuffle = HashMap<ULong, Boolean>()
var Link.isRepeatedShuffle: Boolean
    get() {
        if(_isRepeatedShuffle[this.guildId] == null) _isRepeatedShuffle[this.guildId] = false
        return _isRepeatedShuffle[this.guildId]!!
    }
    set(value) {
        _isRepeatedShuffle[this.guildId] = value
    }
private val _repeatedShuffleCount = HashMap<ULong, Int>()
var Link.repeatedShuffleCount: Int
    get() {
        if(_repeatedShuffleCount[this.guildId] == null) _repeatedShuffleCount[this.guildId] = 0
        return _repeatedShuffleCount[this.guildId]!!
    }
    set(value) {
        _repeatedShuffleCount[this.guildId] = value
    }

private val _voiceChannel = HashMap<ULong, Snowflake?>()
var Link.voiceChannel: Snowflake?
    get() {
        if(_voiceChannel[this.guildId] == null) _voiceChannel[this.guildId] = null
        return _voiceChannel[this.guildId]
    }
    set(value) {
        _voiceChannel[this.guildId] = value
    }

val ULong.snowflake: Snowflake get() = Snowflake(this)

suspend fun Link.destroyPlayer() = this.queue.destroy()
suspend fun Link._destroyPlayer() {
    _isRepeatedShuffle.remove(this.guildId)
    _repeatedShuffleCount.remove(this.guildId)
    _voiceChannel.remove(this.guildId)
    if(this.destoryScopeRunning) {
        playerDestoryScopeRunning.remove(this.guildId)
    }
    Buttons.deleteController(this.guildId)
    this.destroy()
    this.disconnectAudio()
}