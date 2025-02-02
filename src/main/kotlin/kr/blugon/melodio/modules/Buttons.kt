package kr.blugon.melodio.modules

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.GuildButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.request.KtorRequestException
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.Settings
import kr.blugon.melodio.bot
import kr.blugon.melodio.commands.queuePage
import kr.blugon.melodio.modules.Modules.timeFormat


object Buttons {

    var beforeControllMessage = mutableMapOf<Snowflake, MutableMap<Snowflake, Message>>()
    suspend fun deleteAllControllerInGuild(guildId: ULong) {
        val id = Snowflake(guildId)
        if(beforeControllMessage[id] != null) {
            beforeControllMessage[id]!!.forEach {
                beforeControllMessage[id]!!.forEach { (c, message) ->
                    try {
                        message.delete()
                    } catch (_: KtorRequestException) {}
                }
            }
            beforeControllMessage.remove(id)
        }
    }
    suspend fun deleteControllerInChannel(channel: MessageChannelBehavior) {
        val guildId = channel.asChannel().data.guildId.value?: return
        try {
            beforeControllMessage[guildId]?.get(channel.id)?.delete()
        } catch (_: KtorRequestException) {}
        beforeControllMessage[guildId]?.remove(channel.id)
    }
    suspend fun reloadControllerInChannel(link: Link, channel: MessageChannelBehavior) {
        val guild = bot.getGuild(channel.asChannel().data.guildId.value?: return)
        if(beforeControllMessage[guild.id] == null) beforeControllMessage[guild.id] = mutableMapOf()
        deleteControllerInChannel(channel)
        beforeControllMessage[guild.id]!![channel.id] = channel.createMessage {
            val pages = queuePage(link, link.queue.current!!)
            embed {
                title = ":clipboard: 대기열 [${timeFormat(link.queue.duration)}]"
                color = Settings.COLOR_NORMAL
                description = pages[0]
                footer {
                    text = "페이지 1/${pages.size}${
                        when(link.repeatMode) {
                            RepeatMode.TRACK -> "┃🔂 현재 곡 반복중"
                            RepeatMode.QUEUE -> "┃🔂 대기열 반복중"
                            else -> ""
                        }
                    }"
                }
            }
            val (beforePageButton, nextPageButton, reloadPageButton) = queue
            components = mutableListOf(ActionRowBuilder().apply {
                this.components.add(beforePageButton.apply { this.disabled = true })
                this.components.add(nextPageButton.apply { if(pages.size == 1) this.disabled = true })
                this.components.add(reloadPageButton)
            }, controlls(link))
//            components = mutableListOf(controlls(link))
        }
    }


    fun controlls(link: Link): ActionRowBuilder {
        return ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Secondary, "shuffle") {
                this.emoji = when(link.isRepeatedShuffle) {
                    true -> DiscordPartialEmoji(name = "\uD83D\uDD00") //🔀
                    false -> DiscordPartialEmoji(id = Snowflake(1335361471235756042)) //<:disabled_shuffle:1335361471235756042>
                }
            }
            interactionButton(ButtonStyle.Secondary, "pause") {
                this.emoji = when(link.player.paused) {
                    true -> DiscordPartialEmoji(name = "▶\uFE0F") //▶️
                    false -> DiscordPartialEmoji(name = "⏸\uFE0F") //⏸️
                }
            }
            interactionButton(ButtonStyle.Secondary, "repeat") {
                this.emoji = when(link.queue.repeatMode) {
                    RepeatMode.OFF -> DiscordPartialEmoji(id = Snowflake(1335324680327790684)) //<:disabled_repeat:1335324680327790684>
                    RepeatMode.TRACK -> DiscordPartialEmoji(name = "\uD83D\uDD02") //🔂
                    RepeatMode.QUEUE -> DiscordPartialEmoji(name = "\uD83D\uDD01") //🔁
                }
            }
            interactionButton(ButtonStyle.Secondary, "skip") {
                this.emoji = DiscordPartialEmoji(name = "⏭\uFE0F") //⏭️
            }
            interactionButton(ButtonStyle.Danger, "stop") {
                this.emoji = DiscordPartialEmoji(name = "⏹\uFE0F") //⏹️
            }
        }
    }

    val addTrack: ActionRowBuilder
        get() {
            return ActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, "add") {
                    this.label = "트랙 추가"
                    this.emoji = DiscordPartialEmoji(id = Snowflake(1104057502120824912)) //<:plus:1104057502120824912>
                }
            }
        }

    fun queue(interaction: GuildButtonInteraction, link: Link, appendPage: Int = 0): Pair<ActionRowBuilder, Int> {
        val (beforePageButton, nextPageButton, reloadPageButton) = queue

        val footerText = (if(interaction.message.embeds[0].footer == null) "undefined"
        else interaction.message.embeds[0].footer!!.text.replace(" ", "")).split("|").last().split("┃").first()
        var nowPage = footerText.split("/")[0].replace("페이지", "").toInt()+appendPage
        val pages = queuePage(link, link.queue.current!!)

        if(nowPage <= 1) {
            nowPage = 1
            beforePageButton.disabled = true
        }
        if(pages.size <= nowPage) {
            nowPage = pages.size
            nextPageButton.disabled = true
        }

        return ActionRowBuilder().apply {
            components.add(beforePageButton)
            components.add(nextPageButton)
            components.add(reloadPageButton)
        } to nowPage
    }
    private val queue: Triple<ButtonBuilder, ButtonBuilder, ButtonBuilder>
        get() {
            return Triple(
                ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "beforePage").apply {
                    this.label = "◀이전"
                },
                ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "nextPage").apply {
                    this.label = "다음▶"
                },
                ButtonBuilder.InteractionButtonBuilder(ButtonStyle.Primary, "reloadPage").apply {
                    this.label = "🔄️새로고침"
                }
            )
        }
}