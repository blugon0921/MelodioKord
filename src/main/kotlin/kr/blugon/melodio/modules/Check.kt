package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.GuildInteraction
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.kord.getLink
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager

suspend fun ActionInteraction.defaultCheck(): DefaultCheckResult? {
    if(this !is GuildInteraction) return null
    val voiceChannel = this.user.getVoiceStateOrNull()
    if(voiceChannel?.channelId == null) { //통화방 연결되어있는지 확인
        this.respondNotConnecedVoiceChannelMessage()
        return null
    }

    val link = kord.manager.getLink(this.guildId)
    if(!link.isSameChannel(this, voiceChannel)) return null

    val current = link.queue.current
    if(current == null) { //재생중인지 확인
        this.respondNotExistPlayingNow()
        return null
    }
    return DefaultCheckResult(voiceChannel, link, link.player, current)
}

suspend fun ActionInteraction.playDefaultCheck(): CheckResult? {
    if(this !is GuildInteraction) return null
    val voiceChannel = this.user.getVoiceStateOrNull()
    if(voiceChannel?.channelId == null) {
        this.respondNotConnecedVoiceChannelMessage()
        return null
    }
    val link = bot.manager.getLink(this.guildId)
    if(link.voiceChannel == null) {
        link.voiceChannel = voiceChannel.channelId
        link.addEvent()
    }
    if(link.state == Link.State.CONNECTED || link.state == Link.State.CONNECTING) { //이미 연결 되어 있으면
        if(voiceChannel.channelId != link.voiceChannel) {
            this.respondNotJoinedSameChannel()
            return null
        }
    }
    return CheckResult(voiceChannel, link, link.player)
}

suspend fun Link.isSameChannel(interaction: ActionInteraction, voiceChannel: VoiceState): Boolean {
    return if(this.state != Link.State.CONNECTED && this.state != Link.State.CONNECTING) {
        interaction.respondBotNotConnectedVoiceChannel()
        false
    } else {
        if(this.voiceChannel == null) this.voiceChannel = voiceChannel.channelId
        if(voiceChannel.channelId != this.voiceChannel) {
            interaction.respondNotJoinedSameChannel()
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