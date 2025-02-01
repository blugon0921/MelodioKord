package kr.blugon.melodio

import dev.kord.common.Color
import kr.blugon.melodio.exception.ConfigException
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

object Settings {
    private val configFile = File("config.json")
    private val settings = JSONObject(configFile.readText())

    private inline fun <reified T> JSONObject.getOrNull(key: String): T? {
        if(!this.has(key)) return null
        val value = this.get(key)
        if(value !is T) return null
        return value
    }

    val TOKEN = settings.getOrNull<String>("token")?: throw ConfigException("token")
    val GUILD_ID = settings.getOrNull<Long>("guildId")?: throw ConfigException("guildId")

    val TEST_TOKEN = settings.getOrNull<String>("testToken")
    val TEST_GUILD_ID = settings.getOrNull<Long>("testGuildId")

    private val COLOR_NORMAL_VALUE = settings.getOrNull<String>("colorNormal")?: throw ConfigException("colorNormal")
    private val COLOR_LOADING_VALUE = settings.getOrNull<String>("colorLoading")?: throw ConfigException("colorLoading")
    private val COLOR_ERROR_VALUE = settings.getOrNull<String>("colorError")?: throw ConfigException("colorError")

    val COLOR_NORMAL = Color(Integer.parseInt(COLOR_NORMAL_VALUE, 16))
    val COLOR_LOADING = Color(Integer.parseInt(COLOR_LOADING_VALUE, 16))
    val COLOR_ERROR = Color(Integer.parseInt(COLOR_ERROR_VALUE, 16))

    val LAVALINK_HOST = settings.getOrNull<String>("lavalinkHost")?: throw ConfigException("lavalinkHost")
    val LAVALINK_PORT = settings.getOrNull<Int>("lavalinkPort")?: throw ConfigException("lavalinkPort")
    val LAVALINK_PASSWORD = settings.getOrNull<String>("lavalinkPassword")?: throw ConfigException("lavalinkPassword")

    fun checkExistConfig(fileName: String = "config.json") {
        val settingsFile = File(fileName)
        if(!settingsFile.exists()) { //config.json is not exist
            val resource = ClassLoader.getSystemClassLoader().getResource("config.json")?.readText()
                ?: throw FileNotFoundException("Failed to load config.json file")
            settingsFile.createNewFile()
            settingsFile.writeText(resource)
            return println("Please edit config.json")
        }
    }
}