package kr.blugon.melodio.exception

class ConfigException(
    id: String,
    defaultMessage: Boolean = true
): Exception(when(defaultMessage) {
    true -> "'${id}' is null or invalid"
    false -> id
})