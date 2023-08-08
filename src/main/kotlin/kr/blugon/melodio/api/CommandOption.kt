package kr.blugon.melodio.api

import dev.kord.rest.builder.interaction.BooleanBuilder
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.NumberOptionBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

interface CommandOption {
    val name: String
    val description: String
}

data class IntegerOption(override val name: String, override val description: String, val builder: IntegerOptionBuilder.() -> Unit = {}): CommandOption
data class NumberOption(override val name: String, override val description: String, val builder: NumberOptionBuilder.() -> Unit = {}): CommandOption
data class StringOption(override val name: String, override val description: String, val builder: StringChoiceBuilder.() -> Unit = {}): CommandOption
data class BooleanOption(override val name: String, override val description: String, val builder: BooleanBuilder.() -> Unit = {}): CommandOption