package kr.blugon.melodio.modules

import kr.blugon.kordmand.RegistrableEvent

interface NamedRegistrable: RegistrableEvent {
    val name: String
}