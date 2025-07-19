package kr.blugon.melodio.modules

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.request.KtorRequestException
import dev.schlaubi.lavakord.audio.Link
import kr.blugon.lavakordqueue.RepeatMode
import kr.blugon.lavakordqueue.queue
import kr.blugon.melodio.bot


object Buttons {

    var controllerMessages = mutableMapOf<Snowflake, Message>()
    suspend fun deleteController(guildId: Snowflake) = deleteController(guildId.value)
    suspend fun deleteController(guildId: ULong) {
        val id = Snowflake(guildId)
        val controllerMessage = controllerMessages[id]?: return
        try {
            controllerMessage.delete()
            controllerMessages.remove(id)
        } catch (_: KtorRequestException) { }
    }
    suspend fun reloadQueue(link: Link, guildId: ULong) {
        val id = Snowflake(guildId)
        val controllerMessage = controllerMessages[id]?: return
        try {
            val (embed, component) = QueueUtils.embed(link, controllerMessage)

            controllerMessage.edit {
                embeds = mutableListOf(embed)
                components = component
            }
        } catch (_: KtorRequestException) { }
    }
    suspend fun resendController(link: Link, channel: MessageChannelBehavior) {
        val guild = bot.getGuild(channel.asChannel().data.guildId.value?: return)
        deleteController(guild.id)
        val (embed, component) = QueueUtils.embed(link)

        controllerMessages[guild.id] = channel.createMessage {
            embeds = mutableListOf(embed)
            components = component
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
}