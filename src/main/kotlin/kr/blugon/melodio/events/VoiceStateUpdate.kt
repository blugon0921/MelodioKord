package kr.blugon.melodio.events

import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.Member
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.schlaubi.lavakord.audio.Link
import kotlinx.coroutines.delay
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.api.LinkAddon.destroyPlayer
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue

class VoiceStateUpdate {
    val name = "voiceStateUpdate"

    companion object {
        val playerDestroyThread = HashMap<ULong, DestroyThread?>()
        var Link.destroyThread: DestroyThread?
            get() {
                if(playerDestroyThread[this.guildId] == null) playerDestroyThread[this.guildId] = null
                return playerDestroyThread[this.guildId]
            }
            set(value) {
                playerDestroyThread[this.guildId] = value
            }
    }

    suspend fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(name)} 이벤트 불러오기 성공")
        bot.on<VoiceStateUpdateEvent> {
            val guild = state.getGuild()
            val link = bot.manager.getLink(state.guildId.value)

            if(old == null) return@on
            if(link.state != Link.State.CONNECTED) return@on

            if(old?.channelId != null && state.channelId == null && state.userId == bot.selfId) {
                link.destroyPlayer()
                return@on
            }
            val old = old!!

            val stateChange = object {
                lateinit var type: VoiceUpdateType
                var channel: BaseVoiceChannelBehavior? = null
                val members = ArrayList<Member>()
            }
            if(old.getChannelOrNull() == null && state.getChannelOrNull() != null) stateChange.type = VoiceUpdateType.JOIN
            if(old.getChannelOrNull() != null && state.getChannelOrNull() == null) stateChange.type = VoiceUpdateType.LEAVE
            if(old.getChannelOrNull() != null && state.getChannelOrNull() != null) stateChange.type = VoiceUpdateType.MOVE
            if(old.getChannelOrNull() == null && state.getChannelOrNull() == null) return@on
//            if(state.data.mute && !old.data.mute) return@on link.player.pause(true)
//            if(!state.data.mute && old.data.mute) return@on link.player.pause(false)
            if(state.data.mute != old.data.mute) return@on
            if(state.data.selfVideo != old.data.selfVideo) return@on
            if(state.data.selfMute != old.data.selfMute) return@on
            if(state.data.selfStream.discordBoolean != old.data.selfStream.discordBoolean) return@on
            if(stateChange.type == VoiceUpdateType.MOVE) {
                link.voiceChannel = state.channelId
//                stateChange.type = VoiceUpdateType.LEAVE
            }
            if(stateChange.type == VoiceUpdateType.JOIN) stateChange.channel = state.getChannelOrNull()
            if(stateChange.type == VoiceUpdateType.LEAVE) stateChange.channel = old.getChannelOrNull()
            if(stateChange.type == VoiceUpdateType.MOVE) stateChange.channel = state.getChannelOrNull()

            if(stateChange.channel == null || stateChange.channel?.id != link.voiceChannel) return@on

            stateChange.channel!!.voiceStates.collect {
                if(it.getMember().isBot) return@collect
                stateChange.members.add(it.getMember())
            }
            if(stateChange.type == VoiceUpdateType.MOVE) {
                stateChange.type = VoiceUpdateType.LEAVE
                if(stateChange.members.isEmpty() && link.queue.current != null) { //Is Voice Channel Empty
                    stateChange.type = VoiceUpdateType.LEAVE
                }
                if(stateChange.members.isNotEmpty() && link.player.paused) { //Is Not Voice Channel Empty
                    stateChange.type = VoiceUpdateType.JOIN
                }
            }

            when(stateChange.type) {
                VoiceUpdateType.JOIN -> {
                    if(stateChange.members.isNotEmpty() && link.player.paused) {
                        link.player.pause(false)
                        if(link.destroyThread != null) {
                            link.destroyThread!!.stopFlag = true
                            link.destroyThread = null
                        }
                    }
                }
                VoiceUpdateType.LEAVE -> {
                    if(stateChange.members.isEmpty() && link.queue.current != null) {
                        link.player.pause(true)
                        link.destroyThread = DestroyThread(link)
                        link.destroyThread!!.start()
                    }
                }
                else -> {}
            }
        }
    }
}

class DestroyThread(val link: Link, var stopFlag: Boolean = false): Thread() {
    var second = 0

    override fun run() {
        suspend {
            while (!stopFlag) {
                if((10*60) <= second) { //10분
                    link.destroyPlayer()
                }
                second++
                delay(1000)
            }
        }
    }
}

enum class VoiceUpdateType {
    JOIN,
    LEAVE,
    MOVE
}