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
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.repeatMode
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.RepeatMode
import kr.blugon.melodio.api.logger

class RepeatCmd: Command, OnCommand {
    override val command = "repeat"
    override val description = "대기열 혹은 노래를 반복합니다"
    override val options = listOf(
        IntegerOption("mode", "반복 모드를 선택해주세요").apply {
            choice("현재 노래", 1)
            choice("대기열", 2)
            choice("해제", 3)
        }
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

            val embed = EmbedBuilder()
            val mode = interaction.command.integers["mode"]
            when(mode) {
                1L -> {
                    link.repeatMode = RepeatMode.TRACK
                    embed.title = "**:repeat_one: 현재 노래를 반복합니다**"
                }
                2L -> {
                    link.repeatMode = RepeatMode.QUEUE
                    embed.title = "**:repeat: 대기열을 반복합니다**"
                }
                3L -> {
                    link.repeatMode = RepeatMode.OFF
                    embed.title = "**:arrow_right_hook: 노래 반복을 해제했습니다**"
                }
                else -> {
                    if(link.repeatMode == RepeatMode.OFF) {
                        link.repeatMode = RepeatMode.TRACK
                        embed.title = "**:repeat_one: 현재 노래를 반복합니다**"
                    } else {
                        link.repeatMode = RepeatMode.OFF
                        embed.title = "**:arrow_right_hook: 노래 반복을 해제했습니다**"
                    }
                }
            }

            interaction.respondPublic {
                embeds = mutableListOf(embed.apply {
                    color = Settings.COLOR_NORMAL
                })
                components = mutableListOf(buttons)
            }
        }
    }
}