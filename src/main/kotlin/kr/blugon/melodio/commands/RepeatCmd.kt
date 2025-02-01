package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.Buttons
import kr.blugon.melodio.modules.defaultCheck
import kr.blugon.melodio.modules.repeatMode

class RepeatCmd(bot: Kord): Command(bot) {
    override val command = "repeat"
    override val description = "반복 모드를 설정합니다"
    override val options = listOf(
        IntegerOption("mode", "반복 모드") {
            choice("현재 노래", 1)
            choice("대기열", 2)
            choice("해제", 3)
        }
    )


    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        val embed = EmbedBuilder()
        val mode = interaction.command.integers["mode"]
        embed.title = when(mode) {
            1L -> {
                link.repeatMode = RepeatMode.TRACK
                ":repeat_one: 현재 노래를 반복합니다"
            }
            2L -> {
                link.repeatMode = RepeatMode.QUEUE
                ":repeat: 대기열을 반복합니다"
            }
            3L -> {
                link.repeatMode = RepeatMode.OFF
                ":arrow_right_hook: 노래 반복을 해제했습니다"
            }
            else -> {
                if(link.repeatMode == RepeatMode.OFF) {
                    link.repeatMode = RepeatMode.TRACK
                    ":repeat_one: 현재 노래를 반복합니다"
                } else {
                    link.repeatMode = RepeatMode.OFF
                    ":arrow_right_hook: 노래 반복을 해제했습니다"
                }
            }
        }

        interaction.respondPublic {
            embeds = mutableListOf(embed.apply {
                color = Settings.COLOR_NORMAL
            })
        }
    }
}