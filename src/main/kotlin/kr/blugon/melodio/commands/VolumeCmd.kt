package kr.blugon.melodio.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.embed
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Modules.buttons
import kr.blugon.melodio.Modules.isSameChannel
import kr.blugon.melodio.Settings
import kr.blugon.melodio.api.LinkAddon.isVolumePlay
import kr.blugon.melodio.api.LinkAddon.volume
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.OnCommand
import kr.blugon.melodio.api.Queue.Companion.queue
import kr.blugon.melodio.api.logger
import kotlin.time.Duration.Companion.seconds

//class VolumeCmd: Command, OnCommand {
//    override val command = "volume"
//    override val description = "노래의 볼륨을 설정합니다"
//    override val options = listOf(
//        IntegerOption("volume", "조절할 볼륨을 입력해주세요(기본 50)", 0, 100).apply {
//            required = true
//        }
//    )
//
//    override fun on() {
//        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(command)} 커맨드 불러오기 성공")
//        onRun(bot) {
//            if(interaction.command.rootName != command) return@onRun
//            val voiceChannel = interaction.user.getVoiceStateOrNull()
//            if(voiceChannel?.channelId == null) {
//                interaction.respondEphemeral {
//                    embed {
//                        title = "**음성 채널에 접속해있지 않습니다**"
//                        color = Settings.COLOR_ERROR
//                    }
//                }
//                return@onRun
//            }
//
//            val link = kord.manager.getLink(interaction.guildId.value)
//            if(!link.isSameChannel(interaction, voiceChannel)) return@onRun
//
//            val player = link.player
//
//            val current = link.queue.current
//            if(current == null) {
//                interaction.respondEphemeral {
//                    embed {
//                        title = "**재생중인 노래가 없습니다**"
//                        color = Settings.COLOR_ERROR
//                    }
//                }
//                return@onRun
//            }
//
//            val volume = interaction.command.integers["volume"]!!.toInt()
//            var icon = ":loud_sound:"
//            if (volume <= 50) icon = ":sound:"
//            if (volume == 0) icon = ":mute:"
//
//            val position = player.positionDuration
//            link.volume = volume
//            link.isVolumePlay = true
//            player.playTrack(current) {
//                this.volume = volume
//                this.position = position.plus(0.3.seconds)
//            }
////            player.applyFilters {
////                this.volume = volume.toFloat()/50
////            }
//
//            interaction.respondPublic {
//                embed {
//                    title = "**${icon} 볼륨을 ${volume}%로 설정했습니다**"
//                    color = Settings.COLOR_NORMAL
//                }
//                components = mutableListOf(buttons)
//            }
//        }
//    }
//}