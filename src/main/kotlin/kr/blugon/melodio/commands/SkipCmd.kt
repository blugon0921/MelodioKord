package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.modules.*
import kr.blugon.melodio.modules.Modules.buttons

class SkipCmd: Command, Registable {
    override val command = "skip"
    override val description = "노래를 건너뜁니다"
    override val options = listOf(
        IntegerOption("count", "건너뛸 개수를 입력해 주세요", 1, 100)
    )

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return@onRun

            if(link.queue.isEmpty()) {
                interaction.respondPublic {
                    embed {
                        title = ":track_next: 노래 1개를 건너뛰었습니다".bold
                        description = """
                            ${current.info.displayTitle}
                            
                            
                            곡 없음
                        """.trimIndent()
                        color = Settings.COLOR_NORMAL
                    }
                }
                link.destroyPlayer()
                return@onRun
            }

            var count: Int = (interaction.command.integers["count"]?: 1).toInt()

            //count가 대기열보다 크면서 대기열 반복중이 아니면 count를 대기열 크기로 만들기
            if(link.queue.size <= count && link.repeatMode != RepeatMode.QUEUE) count = link.queue.size
            if(link.isRepeatedShuffle) link.repeatedShuffleCount-=count
            if(link.repeatedShuffleCount < 0) link.repeatedShuffleCount = 0

            //만약 대기열 반복중이면
            if(link.repeatMode == RepeatMode.QUEUE) {
                //스킵한 개수만큼 대기열에 다시 추가
                link.queue.add(current)
                if(1 < count) {
                    for(i in 0 until count-1) {
                        link.queue.add(link.queue[i].track) {
                            link.queue[i].option(this)
                        }
                    }
                }
            }

            val embed = EmbedBuilder()
            embed.title = ":track_next: 노래 ${count}개를 건너뛰었습니다"
            embed.color = Settings.COLOR_NORMAL

            //만약 대기열 크기만큼 스킵하면서 대기열 반복중이 아니면
            if(link.queue.size == count && link.repeatMode != RepeatMode.QUEUE) {
                //대기열 크기만큼 건너뛰는 메세지로 바꾸기
                embed.title = ":track_next: 노래 ${link.queue.size}개를 건너뛰었습니다"
            }

            //만약 스킵하는 개수가 대기열보다 작다면
            if(count <= link.queue.size) {
                //스킵
                embed.description = """
                    ${current.info.displayTitle}
                    
                    
                    :arrow_forward: ${link.queue[count-1].track.info.displayTitle}
                """.trimIndent()
            } else {
                //스킵하는 개수가 대기열보다 크면서 대기열을 반복중이면
                if(link.repeatMode == RepeatMode.QUEUE) {
                    embed.description = """
                        ${current.info.displayTitle}
                        
                        
                        :arrow_forward: ${link.queue[count-1].track.info.displayTitle}
                    """.trimIndent()
                }
            }

//            link.queue.add(0, QueueItem(current) {
//                this.volume = player.varVolume
//            })
            link.skip(count)

            interaction.respondPublic {
                embeds = mutableListOf(embed)
                components = mutableListOf(buttons)
            }
        }
    }
}