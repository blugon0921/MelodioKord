package kr.blugon.melodio

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.exception.KordInitializationException
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.isTest
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.Main.version
import kr.blugon.melodio.modules.*
import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds


object Main {
    val version = "1.1.4"

    lateinit var bot: Kord
    private lateinit var lavalink: LavaKord
    var Kord.manager: LavaKord
        get() = lavalink
        set(value) { lavalink = value }

    private val kordIsReady = HashMap<Kord, Boolean>()
    var Kord.isReady: Boolean
        get() {
            if(kordIsReady[this] == null) kordIsReady[this] = false
            return kordIsReady[this]!!
        }
        set(value) {
            kordIsReady[this] = value
        }


    private val kordIsTestBot = HashMap<Kord, Boolean>()
    var Kord.isTest: Boolean
        get() {
            if(kordIsTestBot[this] == null) kordIsTestBot[this] = false
            return kordIsTestBot[this]!!
        }
        set(value) {
            kordIsTestBot[this] = value
        }
}

suspend fun main(args: Array<String>) {
    //버전
    val settingsFile = File("config.json")
    if(!settingsFile.exists()) {
        val resource = ClassLoader.getSystemClassLoader().getResource("config.json")
            ?.readText() ?: throw FileNotFoundException("The settings file does not exist")
        withContext(Dispatchers.IO) {
            settingsFile.createNewFile()
            settingsFile.writeText(resource)
        }
        println("Please edit config.json")
        exitProcess(0)
    }
    val nullInspect = Settings.nullInspect(args.getOrNull(0) == "test")
    if(nullInspect != null) {
        println("The ${nullInspect.first} does not exist or is not a ${nullInspect.second}".color(LogColor.RED))
        exitProcess(0)
    }
    if(args.getOrNull(0) == "registerCommand" || (args.getOrNull(0) == "test" && args.getOrNull(1) == "registerCommand")) {
        deployCommand(args)
        return
    }
    bot = try {
        Kord(
            if (args.getOrNull(0) == "test") Settings.TEST_TOKEN!!
            else Settings.TOKEN!!
        )
    } catch (e: KordInitializationException) {
        println("Token is wrong".color(LogColor.RED))
        exitProcess(0)
    }
    bot.isTest = args.getOrNull(0) == "test"

    bot.manager = bot.lavakord {
        link {
            autoReconnect = true
            retry = linear(2.seconds, 60.seconds, 10)
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