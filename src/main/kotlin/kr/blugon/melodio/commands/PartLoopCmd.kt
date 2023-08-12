package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.*
import kr.blugon.melodio.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Modules.log
import kr.blugon.melodio.Modules.timeFormat
import kr.blugon.melodio.Modules.timeToSecond
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.varVolume
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.inColor
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.StringOption
import kotlin.concurrent.thread

class PartLoopCmd: Command {
    override val command = "partloop"
    override val description = "노래의 특정 부분을 반복합니다"
    override val options = listOf(
        StringOption("start", "반복을 시작할 위치를 적어주세요(00:00:00)") {
            this.required = true
        },
        StringOption("end", "반복을 끝낼 위치를 적어주세요(00:00:00)")
    )

    companion object {
        val playerPartLoopThread = HashMap<ULong, PartLoopThread?>()
        var Link.partloopThread: PartLoopThread?
            get() {
                if(playerPartLoopThread[this.guildId] == null) playerPartLoopThread[this.guildId] = null
                return playerPartLoopThread[this.guildId]
            }
            set(value) {
                playerPartLoopThread[this.guildId] = value
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun execute() {
        kordLogger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
        bot.on<GuildChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName != command) return@on
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@on

            val player = link.player

            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "**재생중인 노래가 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }
            val start = interaction.command.strings["start"]!!
            val end = interaction.command.strings["end"]

            val startMs: Long
            var endMs = current.length.inWholeMilliseconds-1000L
            try {
                startMs = timeToSecond(start)*1000L
                if(end != null) endMs = timeToSecond(end)*1000L
            } catch (e: Exception) {
                interaction.respondEphemeral {
                    embed {
                        title = "**시간 형식이 잘못되었습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }
            if(endMs <= startMs) {
                interaction.respondEphemeral {
                    embed {
                        title = "**시작 위치는 종료 위치보다 전이어야 합니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            try {
                link.player.seekTo(startMs)
            } catch (e: Exception) {
                interaction.respondEphemeral {
                    embed {
                        title = "**${timeFormat(startMs)} 위치로 이동할 수 없습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return@on
            }

            interaction.respondPublic {
                embed {
                    title = "**:arrow_right_hook: ${timeFormat(startMs)}~${timeFormat(endMs)}를 반복합니다**"
                    color = Settings.COLOR_NORMAL
                }
                components.add(buttons)
            }

            if(link.partloopThread != null) {
                link.partloopThread!!.stopFlag = true
                link.partloopThread = null
            }
            link.partloopThread = PartLoopThread(link, startMs, endMs, current)
            link.partloopThread!!.start()
        }
    }
}

class PartLoopThread(val link: Link, val startMs: Long, val endMs: Long, val track: Track, var stopFlag: Boolean = false): Thread() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun run() {
        super.run()

        GlobalScope.launch {
            while (!stopFlag) {
                if(link.player.position < startMs) {
                    loop@while (true) {
                        try {
                            if(link.queue.current != null) {
                                link.player.seekTo(startMs)
                                break@loop
                            }
                        } catch (_: IllegalStateException) {
                            continue@loop
                        }
                    }
                }
                if(endMs <= link.player.position) {
                    link.player.playTrack(track) {
                        this.volume = link.varVolume
                    }
                    loop@while (true) {
                        try {
                            if(link.queue.current != null) {
                                link.player.seekTo(startMs)
                                break@loop
                            }
                        } catch (e: IllegalStateException) {
                            link.player.playTrack(track) {
                                this.volume = link.varVolume
                            }
                            continue@loop
                        }
                    }
                }
                delay(10)
            }
        }
    }
}