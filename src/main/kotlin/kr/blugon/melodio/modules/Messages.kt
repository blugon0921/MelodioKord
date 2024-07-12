package kr.blugon.melodio.modules

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import kr.blugon.melodio.Settings

object Messages {
    const val NOT_CONNECTED_VOICE_CHANNEL = "음성 채널에 접속해있지 않습니다"
    const val NOT_EXIST_PLAYING_NOW = "재생중인 노래가 없습니다"
    const val NOT_JOINED_SAME_CHANNEL = "봇과 같은 음성 채널에 접속해있지 않습니다"
    const val BOT_NOT_CONNECED_VOICE_CHANNEL = "봇이 음성 채널에 접속해있지 않습니다"

    const val NO_SEARCH_RESULT = "결과를 찾을 수 없습니다"
    const val SEARCH_EXCEPTION = "결과를 찾는중 오류가 발생했습니다"
}

fun errorEmbed(title: String): EmbedBuilder {
    return EmbedBuilder().apply {
        this.title = title
        this.color = Settings.COLOR_ERROR
    }
}
suspend fun ActionInteraction.respondError(title: String) {
    respondEphemeral { embeds = mutableListOf(errorEmbed(title)) }
}

suspend fun ActionInteraction.respondNotConnecedVoiceChannelMessage() {
    respondError(Messages.NOT_CONNECTED_VOICE_CHANNEL)
}
suspend fun ActionInteraction.respondNotExistPlayingNow() {
    respondError(Messages.NOT_EXIST_PLAYING_NOW)
}
suspend fun ActionInteraction.respondNotJoinedSameChannel() {
    respondError(Messages.NOT_JOINED_SAME_CHANNEL)
}
suspend fun ActionInteraction.respondBotNotConnectedVoiceChannel() {
    respondError(Messages.BOT_NOT_CONNECED_VOICE_CHANNEL)
}