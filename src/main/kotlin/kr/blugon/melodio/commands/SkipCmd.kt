package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.stringLimit
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.destroyPlayer
import kr.blugon.melodio.api.LinkAddon.isRepeatedShuffle
import kr.blugon.melodio.api.LinkAddon.repeatMode
import kr.blugon.melodio.api.LinkAddon.repeatedShuffleCount
import kr.blugon.melodio.api.LinkAddon.volume
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.Queue.Companion.skip
import kr.blugon.melodio.api.RepeatMode
import kr.blugon.melodio.api.logger

class SkipCmd: Command, OnCommand {
    override val command = "skip"
    override val description = "노래를 건너뜁니다"
    override val options = listOf(
        IntegerOption("count", "건너뛸 개수를 입력해 주세요", 1, 50)
    )

    override fun on() {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        onRun(bot) {
            if(interaction.command.rootName != command) return@onRun
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            if(link.queue.isEmpty()) {
                interaction.respondPublic {
                    embed {
                        title = "**:track_next: 노래 1개를 건너뛰었습니다**"
                        description = """
                            [**${stringLimit(current.info.title.replace("[", "［").replace("]", "［"))}**](${current.info.uri})
                            
                            
                            **곡 없음**
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
                link.queue.add(current) { volume = link.volume }
                if(1 < count) {
                    for(i in 0 until count-1) {
                        link.queue.add(link.queue[i].track) {
                            link.queue[i].option(this)
                        }
                    }
                }
            }

            val embed = EmbedBuilder()
            embed.title = "**:track_next: 노래 ${count}개를 건너뛰었습니다**"
            embed.color = Settings.COLOR_NORMAL

            //만약 대기열 크기만큼 스킵하면서 대기열 반복중이 아니면
            if(link.queue.size == count && link.repeatMode != RepeatMode.QUEUE) {
                //대기열 크기만큼 건너뛰는 메세지로 바꾸기
                embed.title = "**:track_next: 노래 ${link.queue.size}개를 건너뛰었습니다**"
            }

            //만약 스킵하는 개수가 대기열보다 작다면
            if(count <= link.queue.size) {
                //스킵
                embed.description = """
                    [**${stringLimit(current.info.title.replace("[", "［").replace("]", "［"))}**](${current.info.uri})
                    
                    
                    :arrow_forward: [**${stringLimit(link.queue[count-1].track.info.title.replace("[", "［").replace("]", "［"))}**](${link.queue[count-1].track.info.uri})
                """.trimIndent()
            } else {
                //스킵하는 개수가 대기열보다 크면서 대기열을 반복중이면
                if(link.repeatMode == RepeatMode.QUEUE) {
                    embed.description = """
                        [**${stringLimit(current.info.title.replace("[", "［").replace("]", "［"))}**](${current.info.uri})
                        
                        
                        :arrow_forward: [**${stringLimit(link.queue[count-1].track.info.title.replace("[", "［").replace("]", "［"))}**](${link.queue[count-1].track.info.uri})
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