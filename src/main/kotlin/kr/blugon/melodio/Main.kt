package kr.blugon.melodio

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import kr.blugon.kordmand.Command
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.Main.isTest
import kr.blugon.melodio.Main.manager
import kr.blugon.melodio.api.OnCommand
import java.io.File


object Main {
    lateinit var bot: Kord
    lateinit var lavalink: LavaKord
    var Kord.manager: LavaKord
        get() = lavalink
        set(value) { lavalink = value }

    val kordIsReady = HashMap<Kord, Boolean>()
    var Kord.isReady: Boolean
        get() {
            if(kordIsReady[this] == null) kordIsReady[this] = false
            return kordIsReady[this]!!
        }
        set(value) {
            kordIsReady[this] = value
        }


    val kordIsTestBot = HashMap<Kord, Boolean>()
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
    bot = if (args[0] == "test") Kord(Settings.TEST_TOKEN)
          else Kord(Settings.TOKEN)
    bot.isTest = args[0] == "test"
    bot.manager = bot.lavakord()

    bot.manager.addNode("ws://${Settings.LAVALINK_HOST}:${Settings.LAVALINK_PORT}", Settings.LAVALINK_PASSWORD)

    val rootPackage = Main.javaClass.`package`

    //Commands
    rootPackage.classesRunnable("commands").forEach {
        (it as OnCommand).on()
    }

    //Events
    rootPackage.classesRunnable("events")

    //Buttons
    rootPackage.classesRunnable("buttons")

    @OptIn(PrivilegedIntent::class)
    bot.login {
        intents += Intent.Guilds
        intents += Intent.GuildVoiceStates
        intents += Intent.GuildMembers
        intents += Intent.GuildMessages
        intents += Intent.MessageContent

        presence {
            status = PresenceStatus.Online
            playing("/play | Kord")
        }
    }
}


fun Package.classes(more: String = ""): ArrayList<Class<*>> {
    val classes = ArrayList<Class<*>>()
    var directory: File? = null
    val packageName = if(more == "") name
                      else "${name}.${more}"

    directory = try {
//        val classLoader = Thread.currentThread().getContextClassLoader()?: throw ClassNotFoundException("Can't get class loader.")
        val classLoader = Main.javaClass.classLoader?: throw ClassNotFoundException("Can't get class loader.")
        val path: String = packageName.replace('.', '/')
        val resource = classLoader.getResource(path) ?: throw ClassNotFoundException("No resource for $path")
//        println(resource.file.replace(".jar", "").replace("!", "").replace("file:/", "/"))
        File(resource.file.replace(".jar", "").replace("!", "").replace("file:/", "/"))
    } catch (_: NullPointerException) {
        throw ClassNotFoundException("$packageName ($directory) does not appear to be a valid package")
    }

    if (directory!!.exists()) {
        val files = directory.list()
        for (i in files!!.indices) {
            if (files[i].endsWith(".class")) {
                classes.add(Class.forName(("$packageName.").toString() + files[i].substring(0, files[i].length - 6)))
            }
        }
    } else {
        throw ClassNotFoundException("$packageName does not appear to be a valid package")
    }
    return classes
}

fun Package.classesRunnable(more: String = ""): ArrayList<Any> {
    val runnables = ArrayList<Any>()
    this.classes(more).forEach { clazz ->
        try {
            val instance = clazz.getDeclaredConstructor().newInstance()
            runnables.add(instance as Any)
        } catch (e: Exception) {
            return@forEach
        }
    }
    return runnables
}