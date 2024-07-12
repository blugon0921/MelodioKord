package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.BooleanOption
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons

class ShuffleCmd: Command, Registable {
    override val command = "shuffle"
    override val description = "현재 대기열 순서를 섞습니다"
    override val options = listOf(
        BooleanOption("repeat", "대기열을 반복중일때 대기열을 모두 재생하면 자동으로 대기열을 섞습니다")
    )

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            if(link.queue.size < 2) {
                interaction.respondError("대기열에 노래가 2개 이상이어야 합니다")
                return@onRun
            }

            val isRepeat = interaction.command.booleans["repeat"]
            if(isRepeat == null) {
                link.queue.shuffle()
                interaction.respondPublic {
                    embed {
                        title = ":twisted_rightwards_arrows: 대기열 순서를 섞었습니다"
                        color = Settings.COLOR_NORMAL
                    }
                    components = mutableListOf(buttons)
                }
            } else {
                if(!isRepeat) {
                    link.isRepeatedShuffle = false
                    link.repeatedShuffleCount = 0
                    interaction.respondPublic {
                        embed {
                            title = ":arrow_right: 대기열 순서 섞기 반복을 해제했습니다"
                            color = Settings.COLOR_NORMAL
                        }
                        components = mutableListOf(buttons)
                    }
                    return@onRun
                }
                if(link.repeatMode != RepeatMode.QUEUE) {
                    interaction.respondError("대기열을 반복 중이어야 합니다")
                    return@onRun
                }
                link.queue.shuffle()
                link.isRepeatedShuffle = true
                link.repeatedShuffleCount = 0
                interaction.respondPublic {
                    embed {
                        title = ":twisted_rightwards_arrows: 대기열 순서 섞기를 반복합니다".bold
                        color = Settings.COLOR_NORMAL
                    }
                    components = mutableListOf(buttons)
                }
            }
        }
    }
}