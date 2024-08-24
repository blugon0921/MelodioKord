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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.blugon.kordmand.Command
import kr.blugon.melodio.modules.*
import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds


object Main

//버전
const val version = "v1.1.11"

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

private val _isTestBot = HashMap<Kord, Boolean>()
var Kord.isTest: Boolean
    get() {
        if(_isTestBot[this] == null) _isTestBot[this] = false
        return _isTestBot[this]!!
    }
    set(value) {
        _isTestBot[this] = value
    }


suspend fun main(args: Array<String>) {
    val settingsFile = File("config.json")
    if(!settingsFile.exists()) { //config.json is not exist
        val resource = ClassLoader.getSystemClassLoader().getResource("config.json")?.readText() ?: throw FileNotFoundException("Failed to load config.json file")
        withContext(Dispatchers.IO) {
            settingsFile.createNewFile()
            settingsFile.writeText(resource)
        }
        return println("Please edit config.json")
    }
    val isTest = args.contains("-test")

    if(args.contains("-registerCommand")) return registerCommands(isTest)
    bot = try {
        Kord(
            if (isTest) Settings.TEST_TOKEN?: ThrowConfigException("testToken")
            else Settings.TOKEN
        )
    } catch (_: KordInitializationException) { return println("The token is invalid".color(LogColor.RED)) }
    bot.isTest = isTest

    bot.manager = bot.lavakord {
        link {
            autoReconnect = true
            retry = linear(2.seconds, 60.seconds, 10)
        }
        plugins {
            install(LavaSrc)
//            install(Lyrics)
        }
    }
    bot.manager.addNode("ws://${Settings.LAVALINK_HOST}:${Settings.LAVALINK_PORT}", Settings.LAVALINK_PASSWORD!!)

    val rootPackage = Main.javaClass.`package`

    //Commands
    rootPackage.classesRegistable<Registable>("commands").forEach {
        if(it is Command) logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.CYAN.inColor(it.command)} 커맨드 불러오기 성공")
        it.register()
    }

    //Events
    rootPackage.classesRegistable<Event>("events").forEach {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.BLUE.inColor(it.name)} 이벤트 불러오기 성공")
        it.register()
    }

    //Buttons
    rootPackage.classesRegistable<Button>("buttons").forEach {
        logger.log("${LogColor.CYAN.inColor("✔")} ${LogColor.YELLOW.inColor(it.name)} 버튼 불러오기 성공")
        it.register()
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

inline fun <reified T> Package.classesRegistable(more: String = ""): List<T> {
    val registable = ArrayList<T>()

    var name = "${this.name}.${more}"
    if(!name.startsWith("/")) name = "/$name"
    name.replace(".", "/")

    this.classes<T>(more).forEach { clazz ->
        try {
            val instance = clazz.getDeclaredConstructor().newInstance()
            registable.add(instance as T)
        } catch (e: Exception) {
            return@forEach
        }
    }
    return registable
}