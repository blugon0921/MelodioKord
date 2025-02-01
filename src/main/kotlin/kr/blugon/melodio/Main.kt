package kr.blugon.melodio

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.exception.KordInitializationException
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.lavasrc.LavaSrc
import io.github.classgraph.ClassGraph
import kr.blugon.kordmand.Command
import kr.blugon.melodio.buttons.Button
import kr.blugon.melodio.exception.ConfigException
import kr.blugon.melodio.modules.*
import kotlin.time.Duration.Companion.seconds


object Main

//버전
const val version = "v1.2.0"

lateinit var bot: Kord
private lateinit var _lavalink: LavaKord
var Kord.manager: LavaKord
    get() = _lavalink
    set(value) { _lavalink = value }

private val _isReady = HashMap<Kord, Boolean>()
var Kord.isReady: Boolean
    get() {
        if(_isReady[this] == null) _isReady[this] = false
        return _isReady[this]!!
    }
    set(value) {
        _isReady[this] = value
    }


suspend fun main(args: Array<String>) {
    val isTest = args.contains("-test")
    if(args.contains("-registerCommand")) return registerCommands(isTest)
    Settings.checkExistConfig()
    bot = try {
        Kord(
            if (isTest) Settings.TEST_TOKEN?: throw ConfigException("testToken")
            else Settings.TOKEN
        )
    } catch (_: KordInitializationException) { throw ConfigException("The token is invalid") }

    bot.manager = bot.lavakord {
        link {
            autoReconnect = true
            retry = linear(2.seconds, 60.seconds, 10)
        }
        plugins {
            install(LavaSrc)
        }
    }.apply {
        addNode("ws://${Settings.LAVALINK_HOST}:${Settings.LAVALINK_PORT}", Settings.LAVALINK_PASSWORD)
    }

    val rootPackage = Main.javaClass.`package`

    //Commands
    rootPackage.botArgClasses<Command>("commands", bot).forEach {
        Logger.log("${LogColor.Cyan.inColor("✔")} ${LogColor.Cyan.inColor(it.command)} 커맨드 불러오기 성공")
        it.registerEvent()
    }

    //Events
    rootPackage.registrableClasses<NamedRegistrable>("events").forEach {
        Logger.log("${LogColor.Cyan.inColor("✔")} ${LogColor.Blue.inColor(it.name)} 이벤트 불러오기 성공")
        it.registerEvent()
    }

    //Buttons
    rootPackage.botArgClasses<Button>("buttons", bot).forEach {
        Logger.log("${LogColor.Cyan.inColor("✔")} ${LogColor.Yellow.inColor(it.name)} 버튼 불러오기 성공")
        it.registerEvent()
    }

    @OptIn(PrivilegedIntent::class)
    bot.login {
        intents += Intent.Guilds
        intents += Intent.GuildVoiceStates
        intents += Intent.GuildMembers
        intents += Intent.GuildMessages
        intents += Intent.MessageContent

        presence {
            status = PresenceStatus.Online
            playing("/play | $version")
//            playing("/play")
        }
    }
}


inline fun <reified T> Package.classes(more: String = ""): List<Class<T>> {
    val classes = ArrayList<Class<T>>()
    val packageName = if(more == "") name
                      else "$name.$more"

    val scanResult = ClassGraph().acceptPackages(packageName).scan()
    scanResult.allClasses.names.forEach {
        classes.add(ClassLoader.getSystemClassLoader().loadClass(it) as Class<T>)
    }
    return classes
}

inline fun <reified T> Package.registrableClasses(more: String = ""): List<T> {
    val registrable = ArrayList<T>()

    var name = "${this.name}.${more}"
    if(!name.startsWith("/")) name = "/$name"
    name.replace(".", "/")

    this.classes<T>(more).forEach {
        try {
            val instance = it.getConstructor().newInstance()
            registrable.add(instance as T)
        } catch (e: Exception) { return@forEach }
    }
    return registrable
}

inline fun <reified T> Package.botArgClasses(
    more: String = "",
    bot: Kord,
): List<T> {
    val registrable = ArrayList<T>()

    var name = "${this.name}.${more}"
    if(!name.startsWith("/")) name = "/$name"
    name.replace(".", "/")

    this.classes<T>(more).forEach {
        try {
            val instance = it.getConstructor(Kord::class.java).newInstance(bot)
            registrable.add(instance)
        } catch (e: Exception) { return@forEach }
    }
    return registrable
}