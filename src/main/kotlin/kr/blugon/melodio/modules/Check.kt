package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.GuildInteraction
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.kord.getLink
import kotlinx.coroutines.delay
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.lavakordqueue.volume
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.manager

suspend fun ActionInteraction.defaultCheck(): DefaultCheckResult? {
    if(this !is GuildInteraction) return null
    val voiceChannel = this.user.getVoiceStateOrNull()
    if(voiceChannel?.channelId == null) { //통화방 연결되어있는지 확인
        respondError(Messages.NOT_CONNECTED_VOICE_CHANNEL)
        return null
    }

    val link = kord.manager.getLink(this.guildId)
    if(!link.isSameChannel(this, voiceChannel)) return null

    var current = link.queue.current
    if(current == null) { //재생중인지 확인
        current = link.player.playingTrack
        if(current == null) {
            respondError(Messages.NOT_EXIST_PLAYING_NOW)
            return null
        } else link.queue.current = link.player.playingTrack
    }
    return DefaultCheckResult(voiceChannel, link, link.player, current)
}

suspend fun ActionInteraction.playDefaultCheck(): CheckResult? {
    if(this !is GuildInteraction) return null
    val voiceChannel = this.user.getVoiceStateOrNull()
    if(voiceChannel?.channelId == null) {
        respondError(Messages.NOT_CONNECTED_VOICE_CHANNEL)
        return null
    }
    val link = bot.manager.getLink(this.guildId)
    if(link.voiceChannel == null) {
        link.voiceChannel = voiceChannel.channelId
        link.queue.onQueueEnd {
            link._destroyPlayer()
        }
        link.player.on<TrackStartEvent> {
            if(link.isRepeatedShuffle) {
                if(link.repeatMode != RepeatMode.QUEUE) {
                    link.repeatedShuffleCount = 0
                    return@on
                }
                if(link.queue.size <= link.repeatedShuffleCount) {
                    link.repeatedShuffleCount = 0
                    link.queue.shuffle()
                } else link.repeatedShuffleCount++
            }
            delay(1000)
            Buttons.reloadQueue(link, this.guildId)
        }
        link.volume = Settings.VOLUME
    }
    if(link.state == Link.State.CONNECTED || link.state == Link.State.CONNECTING) { //이미 연결 되어 있으면서
        if(voiceChannel.channelId != link.voiceChannel) { //같은 채널에 없을 때
            respondError(Messages.NOT_JOINED_SAME_CHANNEL)
            return null
        }
    }
    return CheckResult(voiceChannel, link, link.player)
}

suspend fun Link.isSameChannel(interaction: ActionInteraction, voiceChannel: VoiceState): Boolean {
    return if(this.state != Link.State.CONNECTED && this.state != Link.State.CONNECTING) {
        interaction.respondError(Messages.BOT_NOT_CONNECED_VOICE_CHANNEL)
        false
    } else {
        if(this.voiceChannel == null) this.voiceChannel = voiceChannel.channelId
        if(voiceChannel.channelId != this.voiceChannel) {
            interaction.respondError(Messages.NOT_JOINED_SAME_CHANNEL)
            false
        } else true
    }
}


open class CheckResult(
    open val voiceChannel: VoiceState,
    open val link: Link,
    open val player: Player
) {
    operator fun component1(): VoiceState = voiceChannel
    operator fun component2(): Link = link
    operator fun component3(): Player = player
}

class DefaultCheckResult(
    override val voiceChannel: VoiceState,
    override val link: Link,
    override val player: Player,
    val current: Track,
): CheckResult(voiceChannel, link, player) {
    operator fun component4(): Track = current
}