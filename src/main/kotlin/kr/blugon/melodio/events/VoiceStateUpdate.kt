package kr.blugon.melodio.events

import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.audio.Link
import kotlinx.coroutines.*
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.*

@OptIn(DelicateCoroutinesApi::class)
class VoiceStateUpdate: Event {
    override val name = "voiceStateUpdate"

    companion object {
        val playerDestoryScopeRunning = HashMap<ULong, Boolean>()
        var Link.destoryScopeRunning: Boolean
            get() {
                if(playerDestoryScopeRunning[this.guildId] == null) playerDestoryScopeRunning[this.guildId] = false
                return playerDestoryScopeRunning[this.guildId]!!
            }
            set(value) {
                playerDestoryScopeRunning[this.guildId] = value
            }
    }

    override suspend fun register() {
        bot.on<VoiceStateUpdateEvent> {
            val link = bot.manager.getLink(state.guildId.value)

            if(old == null) return@on
            if(link.state != Link.State.CONNECTED) return@on

            when(bot.getUser(state.userId)?.isBot) {
                true, null -> return@on
                else -> {}
            }
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

            val oldMembers = ArrayList<Member>()
            old.getChannelOrNull()?.voiceStates?.collect {
                if(it.getMember().isBot) return@collect
                oldMembers.add(it.getMember())
            }
            when(stateChange.type) {
                VoiceUpdateType.JOIN -> {
                    if(oldMembers.size == stateChange.members.size) return@on
                    if(oldMembers.isEmpty() && stateChange.members.isNotEmpty() && link.player.paused) {
                        link.player.pause(false)
                        link.destoryScopeRunning = false
                    }
                }
                VoiceUpdateType.LEAVE -> {
                    if(stateChange.members.isEmpty() && link.queue.current != null) {
                        link.player.pause(true)
                        link.destoryScopeRunning = true
                        GlobalScope.launch {
                            var seconds = 0
                            while (link.destoryScopeRunning) {
                                if((10*60) <= seconds) { //10분
                                    link.destroyPlayer()
                                    break
                                }
                                seconds++
                                delay(1000)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

enum class VoiceUpdateType {
    JOIN,
    LEAVE,
    MOVE
}