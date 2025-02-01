package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.displayTitle
import kr.blugon.melodio.modules.respondError

class ControllerCmd(bot: Kord): Command(bot) {
    override val command = "controller"
    override val description = "입력한 채널에 버튼 컨트롤러를 표시합니다"

    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck()?: return

        Buttons.reloadControllerInChannel(link, interaction.channel)
        interaction.deferEphemeralResponse()
    }
}