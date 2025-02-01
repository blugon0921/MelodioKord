package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.StringOption
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.Modules.timeFormat
import kr.blugon.melodio.modules.Modules.timeToSecond
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.respondError

class MoveCmd(bot: Kord): Command(bot) {
    override val command = "move"
    override val description = "노래의 재생 위치를 이동합니다"
    override val options = listOf(
        StringOption("location", "이동할 위치를 적어주세요(00:00:00)").apply {
            this.required = true
        }
    )

    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        val time = interaction.command.strings["location"]!!

        val ms: Long
        try {
            ms = timeToSecond(time) *1000L
        } catch (e: Exception) { return interaction.respondError("시간 형식이 잘못되었습니다") }
        try {
            if(ms >= current.info.length) player.seekTo(current.info.length-100)
            else player.seekTo(ms)
        } catch (e: Exception) { return interaction.respondError("${timeFormat(ms/1000)} 위치로 이동할 수 없습니다") }

        interaction.respondPublic {
            embed {
                title = when(ms < current.info.length) {
                    true -> ":left_right_arrow: ${timeFormat(ms)} 위치로 이동했어요"
                    false -> ":left_right_arrow: ${timeFormat(current.info.length)} 위치로 이동했어요"
                }
                color = Settings.COLOR_NORMAL
            }
        }
        return
    }
}