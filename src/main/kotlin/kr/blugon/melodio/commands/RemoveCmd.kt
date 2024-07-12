package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class RemoveCmd: Command, Registable {
    override val command = "remove"
    override val description = "대기열에 있는 노래를 삭제합니다"
    override val options = listOf(
        IntegerOption("number", "삭제할 노래의 번호를 적어주세요", 1, Int.MAX_VALUE.toLong()).apply {
            required = true
        }
    )

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            val number = interaction.command.integers["number"]!!.toInt()
            if(link.queue.isEmpty()) {
                interaction.respondError("대기열이 비어있습니다")
                return@onRun
            }
            if(link.queue.size < number) {
                interaction.respondError("${link.queue.size}이하의 숫자를 입력해주세요")
                return@onRun
            }

            val rmTrack = link.queue[number-1]
            interaction.respondPublic {
                embed {
                    title = "<:minus:1104057498727632906> ${number}번 노래를 삭제했어요"
                    color = Settings.COLOR_NORMAL
                    description = rmTrack.track.info.displayTitle
                }
                components = mutableListOf(buttons)
            }
            link.queue.removeAt(number-1)
        }
    }
}