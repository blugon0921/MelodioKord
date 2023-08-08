package kr.blugon.melodio

import kr.blugon.melodio.api.CommandOption

interface Command {
    val command: String
    val description: String
    val options: List<CommandOption>?
}