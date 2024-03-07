package kr.blugon.melodio.api

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional

interface CommandOption {
    val name: String
    val description: String
    val type: OptionType
    var required: Boolean
}


data class MentionableOption( //Mentionable
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.MENTIONABLE
    override var required = false
}

data class ChannelOption( //Channel
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.CHANNEL
    override var required = false
    val channelTypes = ArrayList<ChannelType>()
}

data class UserOption( //User
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.USER
    override var required = false
}

data class RoleOption( //Role
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.ROLE
    override var required = false
}

data class AttachmentOption( //Attachment
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.ATTACHMENT
    override var required = false
}

data class NumberOption( //Number
    override var name: String,
    override var description: String,
    var minValue: Double? = null,
    var maxValue: Double? = null,
): CommandOption {
    override val type: OptionType = OptionType.NUMBER
    override var required = false
    val choices = ArrayList<Choice.NumberChoice>()
}

data class StringOption( //String
    override var name: String,
    override var description: String,
    var minLength: Int? = null,
    var maxLength: Int? = null,
    var autoComplete: Boolean = false,
): CommandOption {
    override val type: OptionType = OptionType.STRING
    override var required = false
    val choices = ArrayList<Choice.StringChoice>()

    fun choice(name: String, value: String) {
        choices.add(Choice.StringChoice(name, Optional.Missing(), value))
    }
}

data class IntegerOption( //Integer
    override var name: String,
    override var description: String,
    var minValue: Long? = null,
    var maxValue: Long? = null,
): CommandOption {
    override val type: OptionType = OptionType.INTEGER
    override var required = false
    val choices = ArrayList<Choice.IntegerChoice>()

    fun choice(name: String, value: Long) {
        choices.add(Choice.IntegerChoice(name, Optional.Missing(), value))
    }
}

data class BooleanOption( //Boolean
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.BOOLEAN
    override var required = false
}

data class SubCommandOption( //Boolean
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.SUB_COMMAND
    override var required = false
    var options: MutableList<CommandOption>? = null
}

data class GroupOption( //Boolean
    override var name: String,
    override var description: String,
): CommandOption {
    override val type: OptionType = OptionType.GROUP
    override var required = false
    var subCommands: MutableList<SubCommandOption>? = null
}

enum class OptionType {
    MENTIONABLE,
    CHANNEL,
    USER,
    ROLE,
    ATTACHMENT,
    NUMBER,
    STRING,
    INTEGER,
    BOOLEAN,
    SUB_COMMAND,
    GROUP,
}