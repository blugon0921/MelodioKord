package kr.blugon.melodio.buttons

import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.modules.Button
import kr.blugon.melodio.modules.completePlay
import kr.blugon.melodio.modules.playDefaultCheck

class AddThisBtn: Button {
    override val name = "addThisButton"

    override suspend fun register() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId != name) return@on
            val (voiceChannel, link, player) = interaction.playDefaultCheck()?: return@on

            var url = interaction.message.embeds[0].description!!
            var urlStart = 0
            for(i in 7 until url.length) {
                if(!url.substring(i).startsWith("https://")) continue
                urlStart = i
                break
            }


            url = when(interaction.message.embeds[0].title != ":musical_note: 현재 재생중인 노래") {
                true -> url.substring(urlStart, url.length-1) // by Play command
                false -> url.substring(urlStart, url.length-20) // by Now command
            }
            val response = interaction.deferPublicResponse()

            val item = link.loadItem(url)
            if(item.loadType != ResultStatus.NONE && item.loadType != ResultStatus.ERROR) { //통화방 안들어가있으면 통화방 연결
                link.connectAudio(voiceChannel.channelId!!)
            }

            response.completePlay(item, link, url)
        }
    }
}