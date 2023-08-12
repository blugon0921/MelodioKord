package kr.blugon.melodio.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.api.Queue.Companion.destroy
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.destroyThread
import kr.blugon.melodio.events.VoiceStateUpdate.Companion.playerDestroyThread


object LinkAddon {
    val linkVolume = HashMap<Link, Int>()
    var Link.varVolume: Int
        get() {
            if(linkVolume[this] == null) linkVolume[this] = 50
            return linkVolume[this]!!
        }
        set(value) {
            linkVolume[this] = value
        }

    val eventAdded = HashMap<Snowflake, Boolean>()
    suspend fun Link.isEventAdded(): Boolean {
        if(eventAdded[this.getGuild().id] == null) eventAdded[this.getGuild().id] = false
        return eventAdded[this.getGuild().id]!!
    }
    suspend fun Link.isEventAdded(value: Boolean) {
        eventAdded[this.getGuild().id] = value
    }

    val linkRepeatMode = HashMap<Link, RepeatMode>()
    var Link.repeatMode: RepeatMode
        get() {
            if(linkRepeatMode[this] == null) linkRepeatMode[this] = RepeatMode.OFF
            return linkRepeatMode[this]!!
        }
        set(value) {
            linkRepeatMode[this] = value
        }

    val linkIsRepeatShuffle = HashMap<Link, Boolean>()
    var Link.isRepeatedShuffle: Boolean
        get() {
            if(linkIsRepeatShuffle[this] == null) linkIsRepeatShuffle[this] = false
            return linkIsRepeatShuffle[this]!!
        }
        set(value) {
            linkIsRepeatShuffle[this] = value
        }
    val linkRepeatShuffleCount = HashMap<Link, Int>()
    var Link.repeatedShuffleCount: Int
        get() {
            if(linkRepeatShuffleCount[this] == null) linkRepeatShuffleCount[this] = 0
            return linkRepeatShuffleCount[this]!!
        }
        set(value) {
            linkRepeatShuffleCount[this] = value
        }

    val linkVoiceChannel = HashMap<ULong, Snowflake?>()
    var Link.voiceChannel: Snowflake?
        get() {
            if(linkVoiceChannel[this.guildId] == null) linkVoiceChannel[this.guildId] = null
            return linkVoiceChannel[this.guildId]
        }
        set(value) {
            linkVoiceChannel[this.guildId] = value
        }


    val LinkGuild = HashMap<Link, Snowflake>()
    suspend fun Link.getGuild(): Guild {
        return bot.getGuild(LinkGuild[this]!!)
    }
    fun Link.setGuild(value: Guild) {
        LinkGuild[this] = value.id
    }

    suspend fun Link.destroyPlayer() {
        this.queue.destroy()
        linkVolume.remove(this)
        linkRepeatMode.remove(this)
        linkIsRepeatShuffle.remove(this)
        linkRepeatShuffleCount.remove(this)
        LinkGuild.remove(this)
        linkVoiceChannel.remove(this.guildId)
        if(this.destroyThread != null) {
            this.destroyThread!!.stopFlag = true
        }
        playerDestroyThread.remove(this.guildId)
        this.destroy()
        this.disconnectAudio()
    }
}

enum class RepeatMode {
    OFF,
    TRACK,
    QUEUE
}