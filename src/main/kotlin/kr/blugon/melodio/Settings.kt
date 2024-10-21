package kr.blugon.melodio

import dev.kord.common.Color
import kr.blugon.melodio.exception.ConfigException
import org.json.JSONObject
import java.io.File

fun ThrowConfigException(invalid: String): Nothing {
    throw ConfigException("'${invalid}' is null or invalid")
}

object Settings {
    private val configFile = File("config.json")
    private val settings = JSONObject(configFile.readText())

    private inline fun <reified T> JSONObject.getOrNull(key: String): T? {
        if(!settings.has(key)) return null
        val value = settings.get(key)
        if(value !is T) return null
        return value
    }

    val TOKEN = settings.getOrNull<String>("token")?: ThrowConfigException("token")
    val GUILD_ID = settings.getOrNull<Long>("guildId")?: ThrowConfigException("guildId")

    val TEST_TOKEN = settings.getOrNull<String>("testToken")
    val TEST_GUILD_ID = settings.getOrNull<Long>("testGuildId")

    private val COLOR_NORMAL_VALUE = settings.getOrNull<String>("colorNormal")?: ThrowConfigException("colorNormal")
    private val COLOR_LOADING_VALUE = settings.getOrNull<String>("colorLoading")?: ThrowConfigException("colorLoading")
    private val COLOR_ERROR_VALUE = settings.getOrNull<String>("colorError")?: ThrowConfigException("colorError")

    val COLOR_NORMAL = Color(Integer.parseInt(COLOR_NORMAL_VALUE, 16))
    val COLOR_LOADING = Color(Integer.parseInt(COLOR_LOADING_VALUE, 16))
    val COLOR_ERROR = Color(Integer.parseInt(COLOR_ERROR_VALUE, 16))

    val LAVALINK_HOST = settings.getOrNull<String>("lavalinkHost")?: ThrowConfigException("lavalinkHost")
    val LAVALINK_PORT = settings.getOrNull<Int>("lavalinkPort")?: ThrowConfigException("lavalinkPort")
    val LAVALINK_PASSWORD = settings.getOrNull<String>("lavalinkPassword")?: ThrowConfigException("lavalinkPassword")
}