package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kr.blugon.kordmand.Command
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.defaultCheck

class ControllerCmd(bot: Kord): Command(bot) {
    override val command = "controller"
    override val description = "입력한 채널에 버튼 컨트롤러를 표시합니다"

    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck()?: return

        Buttons.resendController(link, interaction.channel)
        interaction.deferEphemeralResponse()
    }
}