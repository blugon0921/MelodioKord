package kr.blugon.melodio.buttons

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import kr.blugon.melodio.modules.NamedRegistrable

abstract class Button(val bot: Kord): NamedRegistrable {
    open val checkType = ButtonCheckType.MATCH

    abstract suspend fun GuildButtonInteractionCreateEvent.onClick()

    override fun registerEvent() {
        bot.on<GuildButtonInteractionCreateEvent> {
            if(interaction.component.customId == null) return@on
            when(checkType) {
                ButtonCheckType.MATCH -> if(interaction.component.customId != name) return@on
                ButtonCheckType.CONTAINS -> if(!interaction.component.customId!!.contains(name)) return@on
                ButtonCheckType.STARTS_WITH -> if(!interaction.component.customId!!.startsWith(name)) return@on
                ButtonCheckType.ENDS_WITH -> if(!interaction.component.customId!!.endsWith(name)) return@on
            }
            onClick()
        }
    }
}

enum class ButtonCheckType {
    MATCH,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH
}