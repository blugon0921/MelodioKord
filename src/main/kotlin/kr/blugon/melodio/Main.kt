package kr.blugon.melodio

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.buttons.*
import kr.blugon.melodio.commands.*
import kr.blugon.melodio.events.ClientReady
import kr.blugon.melodio.events.JoinGuild
import kr.blugon.melodio.events.VoiceStateUpdate


object Main {
    lateinit var bot: Kord
    lateinit var lavalink: LavaKord
    var Kord.manager: LavaKord
        get() = lavalink
        set(value) {
            lavalink = value
        }
}

suspend fun main(args: Array<String>) {
//    bot = Kord(Settings.TEST_TOKEN)
    bot = Kord(Settings.TOKEN)
    bot.manager = bot.lavakord()
    bot.manager.addNode("ws://${Settings.LAVALINK_HOST}:${Settings.LAVALINK_PORT}", Settings.LAVALINK_PASSWORD)
//    bot.manager.addNode("ws://127.0.0.1:${Settings.LAVALINK_PORT}", Settings.LAVALINK_PASSWORD)

    //Commands
    PlayCmd().execute()
    MoveCmd().execute()
    StopCmd().execute()
    QueueCmd().execute()
    NowCmd().execute()
    PauseCmd().execute()
    ResumeCmd().execute()
    ShuffleCmd().execute()
    RepeatCmd().execute()
    SkipCmd().execute()
    PartLoopCmd().execute()
    RemoveCmd().execute()
    VolumeCmd().execute()
    SpeedCmd().execute()

    //Events
    ClientReady().execute()
    VoiceStateUpdate().execute()
    JoinGuild().execute()

    //Buttons
    StopBtn().execute()
    PauseBtn().execute()
    AddThisBtn().execute()
    RepeatBtn().execute()
    SkipBtn().execute()

    BeforePageBtn().execute()
    NextPageBtn().execute()
    ReloadPageBtn().execute()

    bot.login {
        intents += Intent.Guilds
        intents += Intent.GuildVoiceStates
        @OptIn(PrivilegedIntent::class)
        intents += Intent.GuildMembers
        intents += Intent.GuildMessages
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent

        presence {
            status = PresenceStatus.Offline
            playing("/play | 3.0.0")
        }
    }
}