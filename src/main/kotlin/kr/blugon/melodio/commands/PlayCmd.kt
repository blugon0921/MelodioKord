package kr.blugon.melodio.commands

import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import kr.blugon.kordmand.BooleanOption
import kr.blugon.kordmand.Command
import kr.blugon.kordmand.IntegerOption
import kr.blugon.kordmand.StringOption
import kr.blugon.melodio.Main.bot
import kr.blugon.melodio.modules.Registable
import kr.blugon.melodio.modules.completePlay
import kr.blugon.melodio.modules.playDefaultCheck
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URLEncoder

class PlayCmd: Command, Registable {
    override val command = "play"
    override val description = "대기열에 노래를 추가합니다"
    override val options = listOf(
        StringOption("song", "노래 제목이나 링크을 적어주세요(유튜브, 사운드클라우드)").apply {
            this.required = true
            this.autoComplete = true
        },
        BooleanOption("shuffle", "셔플 여부를 적어주세요(기본값 false)"),
        IntegerOption("index", "노래를 추가할 위치를 적어주세요(0이 바로 다음)", 0)
    )

    override suspend fun register() {
        onRun(bot) {
            val (voiceChannel, link, player) = interaction.playDefaultCheck()?: return@onRun

            var url = interaction.command.strings["song"]!!
            val isShuffle = interaction.command.booleans["shuffle"]?: false
            val index = interaction.command.integers["index"]?.toInt() ?: -1
            val response = interaction.deferPublicResponse()

            if(!url.startsWith("http")) url = "ytsearch:$url"
            val item = link.loadItem(url)
            if(item.loadType != ResultStatus.NONE && item.loadType != ResultStatus.ERROR) { //통화방 안들어가있으면 통화방 연결
                link.connectAudio(voiceChannel.channelId!!)
            }

            response.completePlay(item, link, url, index, isShuffle)
        }

        onAutoComplete(bot) {
            val focusedValue = interaction.focusedOption.value
            if(focusedValue.startsWith("http") && focusedValue.contains(":")) {
                interaction.suggestString {
                    choice(focusedValue, focusedValue)
                }
                return@onAutoComplete
            }
            val response = getAutoCompletes(focusedValue)
            if(focusedValue.replace(" ", "") == "" || response.isEmpty()) {
                interaction.suggestString {
                    choice("🔍URL 또는 검색어 입력", "🔍URL 또는 검색어 입력")
                }
                return@onAutoComplete
            }
            interaction.suggestString {
                response.forEach {
                    this.choice(it, it)
                }
            }
        }
    }

    fun request(url: String): JSONArray {
        val connection = URI(url).toURL().openConnection()
        BufferedReader(InputStreamReader(connection.getInputStream())).use {
            return JSONArray(it.readText())
        }
    }

    fun getAutoCompletes(search: String): List<String> {
        if(100 < URLEncoder.encode(search, Charsets.UTF_8).length) return listOf()
        val url = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=${URLEncoder.encode(search, Charsets.UTF_8)}"
        return arrayListOf<String>().apply {
            val data = request(url)
            data.getJSONArray(1).forEach {
                this.add(it.toString())
            }
        }
    }
}