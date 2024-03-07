package kr.blugon.melodio

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.interaction.*
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.melodio.api.LinkAddon.voiceChannel
import kr.blugon.melodio.api.LogColor
import kr.blugon.melodio.api.LogColor.color
import mu.KLogger
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.time.Duration


object Modules {
    fun stringLimit(text: String, len: Int = 30, lastText: String = "..."): String {
        return if(text.length > len) text.substring(0..len)+lastText
        else text
    }

    fun nowDate(): String {
        val today = Date()
        val currentLocale = Locale("KOREAN", "KOREA")
        val pattern = "yyyy. MM. dd. HH:mm:ss" //hhmmss로 시간,분,초만 뽑기도 가능
        val formatter = SimpleDateFormat(
            pattern,
            currentLocale
        )
        return formatter.format(today)
    }


    //밀리초를 hh:mm:ss로 변환 0시간일경우 mm:ss로 변환
    fun timeFormat(time: Duration): String = timeFormat(time.inWholeMilliseconds)
    fun timeFormat(time: Long): String {
        val sec = time / 1000
        var hour = floor(sec / 3600.0)
        var minute = floor((sec - (hour * 3600)) / 60)
        var second = floor(sec - (hour * 3600) - (minute * 60))
        hour = floor(hour)
        minute = floor(minute)
        second = floor(second)
        var ltHour = "${hour.toInt()}"
        var ltminute = "${minute.toInt()}"
        var ltsecond = "${second.toInt()}"
        if (hour < 10) ltHour = "0${hour.toInt()}"
        if (minute < 10) ltminute = "0${minute.toInt()}"
        if (second < 10) ltsecond = "0${second.toInt()}"
        return if (ltHour == "00") {
            "${ltminute}:${ltsecond}"
        } else "${ltHour}:${ltminute}:${ltsecond}"
    }

    //hh:mm:ss를 초로 변환 hh가 0일경우 mm:ss를 초로 변환 mm이 0일경우 ss를 초로 변환 NumberFormat
    fun timeToSecond(time: String): Int {
        var hour = 0
        var minute = 0
        var second = 0
        if(time.contains(":")) {
            val timeArr = time.split(":")
            when (timeArr.size) {
                3 -> {
                    hour =  timeArr[0].toInt()
                    minute =  timeArr[1].toInt()
                    second =  timeArr[2].toInt()
                }
                2 -> {
                    minute =  timeArr[0].toInt()
                    second =  timeArr[1].toInt()
                }
                1 -> {
                    second =  timeArr[0].toInt()
                }
            }
        } else {
            second = time.toInt()
        }
        return (hour * 3600) + (minute * 60) + second
    }

    fun getThumbnail(track: Track): String {
        return "https://img.youtube.com/vi/${track.info.identifier}/mqdefault.jpg"
    }

    val buttons = ActionRowBuilder().apply {
        this.interactionButton(ButtonStyle.Success, "stopButton") {
            this.label = "정지"
            this.emoji = DiscordPartialEmoji(name = "⏹\uFE0F") //⏹️
        }
        this.interactionButton(ButtonStyle.Success, "pauseButton") {
            this.label = "일시정지/해제"
            this.emoji = DiscordPartialEmoji(name = "⏯\uFE0F") //⏯️
        }
        this.interactionButton(ButtonStyle.Success, "repeatQueueButton") {
            this.label = "대기열반복/해제"
            this.emoji = DiscordPartialEmoji(name = "\uD83D\uDD01") //🔁
        }
        this.interactionButton(ButtonStyle.Success, "skipButton") {
            this.label = "다음곡"
            this.emoji = DiscordPartialEmoji(name = "⏭\uFE0F") //⏭️
        }
    }

    val addThisButtons = buttons.copy().apply {
        this.interactionButton(ButtonStyle.Success, "addThisButton") {
            this.label = "해당트랙추가"
            this.emoji = DiscordPartialEmoji(id = Snowflake(1104057502120824912)) //<:plus:1104057502120824912>
        }
    }

    fun ActionRowBuilder.copy(): ActionRowBuilder {
        val copyObject = ActionRowBuilder()
        this.components.forEach { button ->
            copyObject.components.add(button)
        }
        return copyObject
    }

    fun KLogger.log(msg: String) {
        println("[${nowDate().color(LogColor.GREEN).color(LogColor.BOLD)}] $msg")
    }

    fun EmbedBuilder.usedUser(interaction: GuildComponentInteraction) {
        footer {
            this.text = interaction.user.globalName?: interaction.user.username
            this.icon = if(interaction.user.avatar == null) interaction.user.defaultAvatar.cdnUrl.toUrl()
            else interaction.user.avatar!!.cdnUrl.toUrl()
        }
    }

    suspend fun Link.isSameChannel(interaction: GuildApplicationCommandInteraction, voiceChannel: VoiceState): Boolean {
        val link = this
        if(link.state != Link.State.CONNECTED && link.state != Link.State.CONNECTING) {
            interaction.respondEphemeral {
                embed {
                    title = "**봇이 음성 채널에 접속해 있지 않습니다**"
                    color = Settings.COLOR_ERROR
                }
            }
            return false
        } else {
            if(link.voiceChannel == null) link.voiceChannel = voiceChannel.channelId
            if(voiceChannel.channelId != link.voiceChannel) {
                interaction.respondEphemeral {
                    embed {
                        title = "**봇과 같은 음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return false
            }
        }
        return true
    }
    suspend fun Link.isSameChannel(interaction: GuildButtonInteraction, voiceChannel: VoiceState): Boolean {
        val link = this
        if(link.state != Link.State.CONNECTED && link.state != Link.State.CONNECTING) {
            interaction.respondEphemeral {
                embed {
                    title = "**봇이 음성 채널에 접속해 있지 않습니다**"
                    color = Settings.COLOR_ERROR
                }
            }
            return false
        } else {
            if(link.voiceChannel == null) link.voiceChannel = voiceChannel.channelId
            if(voiceChannel.channelId != link.voiceChannel) {
                interaction.respondEphemeral {
                    embed {
                        title = "**봇과 같은 음성 채널에 접속해있지 않습니다**"
                        color = Settings.COLOR_ERROR
                    }
                }
                return false
            }
        }
        return true
    }
}