package kr.blugon.melodio.modules

import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kr.blugon.melodio.modules.Modules.stringLimit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.time.Duration

object Modules {
    const val DEFAULT_VOLUME = 50

    fun stringLimit(text: String, len: Int = 30, lastText: String = "..."): String {
        return if(text.length > len) text.substring(0..len)+lastText
        else text
    }

    fun nowDate(): String {
        val today = Date()
        val currentLocale = Locale.KOREA
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

    //hh:mm:ss를 초로 변환 NumberFormat
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

    val buttons: ActionRowBuilder
        get() {
            return ActionRowBuilder().apply {
                this.interactionButton(ButtonStyle.Secondary, "stopButton") {
                    this.label = "정지"
                    this.emoji = DiscordPartialEmoji(name = "⏹\uFE0F") //⏹️
                }
                this.interactionButton(ButtonStyle.Secondary, "pauseButton") {
                    this.label = "일시정지"
                    this.emoji = DiscordPartialEmoji(name = "⏯\uFE0F") //⏯️
                }
                this.interactionButton(ButtonStyle.Secondary, "repeatQueueButton") {
                    this.label = "대기열반복"
                    this.emoji = DiscordPartialEmoji(name = "\uD83D\uDD01") //🔁
                }
                this.interactionButton(ButtonStyle.Secondary, "skipButton") {
                    this.label = "다음곡"
                    this.emoji = DiscordPartialEmoji(name = "⏭\uFE0F") //⏭️
                }
            }
        }

    val addThisButtons = buttons.apply {
        this.interactionButton(ButtonStyle.Secondary, "addThisButton") {
            this.label = "해당트랙추가"
            this.emoji = DiscordPartialEmoji(id = Snowflake(1104057502120824912)) //<:plus:1104057502120824912>
        }
    }
}

fun String.hyperlink(url: String, isBold: Boolean = true, stringLimit: Boolean = true): String {
    var default = this.replace("[", "［").replace("]", "］")
    if(stringLimit) default = stringLimit(default)
    default = "[${default}](${url})"
    if(isBold) default = default.bold
    return default
}

val TrackInfo.titleWithArtist: String
    get() {
        return "${
            if(this.sourceType == TrackSourceType.Spotify) "${this.author} - "
            else ""
        }${this.title}"
    }

val TrackInfo.displayTitle: String get() = this.displayTitle()
fun TrackInfo.displayTitle(isHyperlinked: Boolean = true, appendArtist: Boolean = true): String {
    val title = stringLimit(if(appendArtist) this.titleWithArtist else this.title)
    return if(isHyperlinked) title.hyperlink("${this.uri}")
    else title
}

val String.bold: String get() = "**$this**"
val String.strikethrough: String get() = "~~$this~~"
val String.underline: String get() = "__${this}__"
val String.italic: String get() = "_${this}_"
val String.box: String get() = "`$this`"



fun EmbedBuilder.interactedUser(interaction: GuildComponentInteraction) {
    footer {
        this.text = interaction.user.globalName?: interaction.user.username
        this.icon = if(interaction.user.avatar == null) interaction.user.defaultAvatar.cdnUrl.toUrl()
        else interaction.user.avatar!!.cdnUrl.toUrl()
    }
}