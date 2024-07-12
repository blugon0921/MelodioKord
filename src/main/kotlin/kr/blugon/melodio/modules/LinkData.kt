package kr.blugon.melodio.modules

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.modules.Queue.Companion.destroy
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.destoryScopeRunning
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.playerDestoryScopeRunning


private val linkRepeatMode = HashMap<ULong, RepeatMode>()
var Link.repeatMode: RepeatMode
    get() {
        if(linkRepeatMode[this.guildId] == null) linkRepeatMode[this.guildId] = RepeatMode.OFF
        return linkRepeatMode[this.guildId]!!
    }
    set(value) {
        linkRepeatMode[this.guildId] = value
    }

private val linkIsRepeatShuffle = HashMap<ULong, Boolean>()
var Link.isRepeatedShuffle: Boolean
    get() {
        if(linkIsRepeatShuffle[this.guildId] == null) linkIsRepeatShuffle[this.guildId] = false
        return linkIsRepeatShuffle[this.guildId]!!
    }
    set(value) {
        linkIsRepeatShuffle[this.guildId] = value
    }
private val linkRepeatShuffleCount = HashMap<ULong, Int>()
var Link.repeatedShuffleCount: Int
    get() {
        if(linkRepeatShuffleCount[this.guildId] == null) linkRepeatShuffleCount[this.guildId] = 0
        return linkRepeatShuffleCount[this.guildId]!!
    }
    set(value) {
        linkRepeatShuffleCount[this.guildId] = value
    }

private val linkVoiceChannel = HashMap<ULong, Snowflake?>()
var Link.voiceChannel: Snowflake?
    get() {
        if(linkVoiceChannel[this.guildId] == null) linkVoiceChannel[this.guildId] = null
        return linkVoiceChannel[this.guildId]
    }
    set(value) {
        linkVoiceChannel[this.guildId] = value
    }

val ULong.snowflake: Snowflake get() = Snowflake(this)

suspend fun Link.destroyPlayer() {
    this.queue.destroy()
    linkRepeatMode.remove(this.guildId)
    linkIsRepeatShuffle.remove(this.guildId)
    linkRepeatShuffleCount.remove(this.guildId)
    linkVoiceChannel.remove(this.guildId)
    if(this.destoryScopeRunning) {
        playerDestoryScopeRunning.remove(this.guildId)
    }
    this.destroy()
    this.disconnectAudio()
}

enum class RepeatMode {
    OFF,
    TRACK,
    QUEUE
}