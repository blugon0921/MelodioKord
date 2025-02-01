package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.BooleanOption
import kr.blugon.kordmand.Command
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class ShuffleCmd(bot: Kord): Command(bot) {
    override val command = "shuffle"
    override val description = "현재 대기열 순서를 섞습니다"
    override val options = listOf(
        BooleanOption("repeat", "대기열을 반복 중일 때 대기열을 모두 재생하면 자동으로 대기열을 섞습니다")
    )


    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        if(link.queue.size < 2) return interaction.respondError("대기열에 노래가 2개 이상이어야 합니다")

        val isRepeat = interaction.command.booleans["repeat"]
        link.queue.shuffle()
        if(isRepeat != null) {
//            if(link.repeatMode != RepeatMode.QUEUE) return interaction.respondError("대기열을 반복 중이어야 합니다")
            link.repeatedShuffleCount = 0
            link.isRepeatedShuffle = isRepeat
        }

        interaction.respondPublic {
            embed {
                title = when(isRepeat) {
                    null -> ":twisted_rightwards_arrows: 대기열 순서를 섞었습니다"
                    false -> "<:disabled_shuffle:1335361471235756042> 대기열 순서 섞기 반복을 해제했습니다"
                    true -> ":twisted_rightwards_arrows: 대기열 순서 섞기를 반복합니다"
                }
                color = Settings.COLOR_NORMAL
            }
        }
    }
}