package kr.blugon.melodio.api

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.*
import kr.blugon.melodio.Loadable
import kr.blugon.melodio.Settings

interface Command: Loadable {
    val command: String
    val description: String
    val options: List<CommandOption>?

    suspend fun deploy(bot: Kord) {
        bot.createGuildChatInputCommand(Snowflake(Settings.GUILD_ID), command, description) {
            this.setOptions()
        }
        bot.createGlobalChatInputCommand(command, description) {
            this.setOptions()
        }
    }

    private fun ChatInputCreateBuilder.setOptions() {
        this@Command.options?.forEach { option ->
            //Mentionable
            if(option.type == OptionType.MENTIONABLE) {
                this.mentionable(option.name, option.description) { option as MentionableOption
                    this.required = option.required
                }
            }

            //Channel
            if(option.type == OptionType.CHANNEL) {
                this.channel(option.name, option.description) { option as ChannelOption
                    this.required = option.required
                    this.channelTypes = option.channelTypes
                }
            }

            //User
            if(option.type == OptionType.USER) {
                this.user(option.name, option.description) { option as UserOption
                    this.required = option.required
                }
            }

            //Role
            if(option.type == OptionType.ROLE) {
                this.user(option.name, option.description) { option as RoleOption
                    this.required = option.required
                }
            }

            //Attachment
            if(option.type == OptionType.ATTACHMENT) {
                this.attachment(option.name, option.description) { option as AttachmentOption
                    this.required = option.required
                }
            }

            //Number
            if(option.type == OptionType.NUMBER) {
                this.number(option.name, option.description) { option as NumberOption
                    this.required = option.required
                    this.minValue = option.minValue
                    this.maxValue = option.maxValue
                }
            }

            //String
            if(option.type == OptionType.STRING) {
                this.string(option.name, option.description) { option as StringOption
                    this.required = option.required
                    this.minLength = option.minLength
                    this.maxLength = option.maxLength
                    this.autocomplete = option.autoComplete
                    option.choices.forEach {
                        this.choice(it.name, it.value)
                    }
                }
            }

            //Integer
            if(option.type == OptionType.INTEGER) {
                this.integer(option.name, option.description) { option as IntegerOption
                    this.required = option.required
                    this.minValue = option.minValue
                    this.maxValue = option.maxValue
                    option.choices.forEach {
                        this.choice(it.name, it.value)
                    }
                }
            }

            //Boolean
            if(option.type == OptionType.BOOLEAN) {
                this.boolean(option.name, option.description) { option as BooleanOption
                    this.required = option.required
                }
            }

            //SubCommand
            if(option.type == OptionType.SUB_COMMAND) {
                this.subCommand(option.name, option.description) { option as SubCommandOption
                    this.required = option.required
                    option.options?.forEach { option2 ->
                        this.setOptions(option2)
                    }
                }
            }

            //Group
            if(option.type == OptionType.GROUP) {
                this.group(option.name, option.description) { option as GroupOption
                    this.required = option.required
                    option.subCommands?.forEach { subCommandOption ->
                        this.subCommand(subCommandOption.name, subCommandOption.description) {
                            this.required = subCommandOption.required
                            subCommandOption.options?.forEach { option2 ->
                                this.setOptions(option2)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun SubCommandBuilder.setOptions(option: CommandOption) {
        //Mentionable
        if(option.type == OptionType.MENTIONABLE) {
            this.mentionable(option.name, option.description) { option as MentionableOption
                this.required = option.required
            }
        }

        //Channel
        if(option.type == OptionType.CHANNEL) {
            this.channel(option.name, option.description) { option as ChannelOption
                this.required = option.required
                this.channelTypes = option.channelTypes
            }
        }

        //User
        if(option.type == OptionType.USER) {
            this.user(option.name, option.description) { option as UserOption
                this.required = option.required
            }
        }

        //Role
        if(option.type == OptionType.ROLE) {
            this.user(option.name, option.description) { option as RoleOption
                this.required = option.required
            }
        }

        //Attachment
        if(option.type == OptionType.ATTACHMENT) {
            this.attachment(option.name, option.description) { option as AttachmentOption
                this.required = option.required
            }
        }

        //Number
        if(option.type == OptionType.INTEGER) {
            this.number(option.name, option.description) { option as NumberOption
                this.required = option.required
                this.minValue = option.minValue
                this.maxValue = option.maxValue
            }
        }

        //String
        if(option.type == OptionType.STRING) {
            this.string(option.name, option.description) { option as StringOption
                this.required = option.required
                this.minLength = option.minLength
                this.maxLength = option.maxLength
                option.choices.forEach {
                    this.choice(it.name, it.value)
                }
            }
        }

        //Integer
        if(option.type == OptionType.INTEGER) {
            this.integer(option.name, option.description) { option as IntegerOption
                this.required = option.required
                this.minValue = option.minValue
                this.maxValue = option.maxValue
                option.choices.forEach {
                    this.choice(it.name, it.value)
                }
            }
        }

        //Boolean
        if(option.type == OptionType.BOOLEAN) {
            this.boolean(option.name, option.description) { option as BooleanOption
                this.required = option.required
            }
        }
    }
}

interface AutoComplete {
    fun autocomplete()
}