package kr.blugon.melodio.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.lavakordqueue.skip
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class SkipCmd(bot: Kord): Command(bot) {
    override val command = "skip"
    override val description = "노래를 건너뜁니다"
    override val options = listOf(
        IntegerOption("count", "건너뛸 개수를 입력해 주세요", 1, 100)
    )


    override suspend fun GuildChatInputCommandInteractionCreateEvent.onRun() {
        val (voiceChannel, link, player, current) = interaction.defaultCheck() ?: return

        var count: Int = (interaction.command.integers["count"]?: 1).toInt()

        //count가 대기열보다 크면서 대기열 반복중이 아니면 count를 대기열 크기로 만들기
        if(link.queue.size <= count && link.queue.repeatMode != RepeatMode.QUEUE) count = link.queue.size
        if(link.isRepeatedShuffle) link.repeatedShuffleCount-=count
        if(link.repeatedShuffleCount < 0) link.repeatedShuffleCount = 0

        val embed = EmbedBuilder().apply {
            title = ":track_next: 노래 ${count}개를 건너뛰었습니다"
            color = Settings.COLOR_NORMAL
        }

        //만약 대기열 크기만큼 스킵하면서 대기열 반복중이 아니면
        if(link.queue.size == count && link.repeatMode != RepeatMode.QUEUE) {
            //대기열 크기만큼 건너뛰는 메세지로 바꾸기
            embed.title = ":track_next: 노래 ${link.queue.size}개를 건너뛰었습니다"
        }

        val newTrack = link.skip(count)

        if(newTrack == null) { //스킵한 노래가 없으면
            interaction.respondPublic {
                embed {
                    title = ":track_next: 노래 ${count}개를 건너뛰었습니다".bold
                    description = """
                        ${current.info.displayTitle}
                        
                        곡 없음
                    """.trimIndent()
                    color = Settings.COLOR_NORMAL
                }
            }
            return
        }
        embed.description = """
            ${current.info.displayTitle}
            
            :arrow_forward: ${newTrack.info.displayTitle}
        """.trimIndent()

        interaction.respondPublic {
            embeds = mutableListOf(embed)
        }
    }
}