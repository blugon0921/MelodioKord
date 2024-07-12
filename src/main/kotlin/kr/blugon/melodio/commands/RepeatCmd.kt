package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.modules.Modules.bold
import kr.blugon.melodio.modules.Modules.buttons
import kr.blugon.melodio.modules.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.modules.*

class RepeatCmd: Command, Registable {
    override val command = "repeat"
    override val description = "대기열 혹은 노래를 반복합니다"
    override val options = listOf(
        IntegerOption("mode", "반복 모드를 선택해주세요").apply {
            choice("현재 노래", 1)
            choice("대기열", 2)
            choice("해제", 3)
        }
    )

    override suspend fun register() {
        onRun(bot) {
            if(interaction.command.rootName != command) return@onRun
            val voiceChannel = interaction.user.getVoiceStateOrNull()
            if(voiceChannel?.channelId == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "음성 채널에 접속해있지 않습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val link = kord.manager.getLink(interaction.guildId.value)
            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun


            val current = link.queue.current
            if(current == null) {
                interaction.respondEphemeral {
                    embed {
                        title = "재생중인 노래가 없습니다".bold
                        color = Settings.COLOR_ERROR
                    }
                }
                return@onRun
            }

            val embed = EmbedBuilder()
            val mode = interaction.command.integers["mode"]
            embed.title = when(mode) {
                1L -> {
                    link.repeatMode = RepeatMode.TRACK
                    ":repeat_one: 현재 노래를 반복합니다".bold
                }
                2L -> {
                    link.repeatMode = RepeatMode.QUEUE
                    ":repeat: 대기열을 반복합니다".bold
                }
                3L -> {
                    link.repeatMode = RepeatMode.OFF
                    ":arrow_right_hook: 노래 반복을 해제했습니다".bold
                }
                else -> {
                    if(link.repeatMode == RepeatMode.OFF) {
                        link.repeatMode = RepeatMode.TRACK
                        ":repeat_one: 현재 노래를 반복합니다".bold
                    } else {
                        link.repeatMode = RepeatMode.OFF
                        ":arrow_right_hook: 노래 반복을 해제했습니다".bold
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