package dev.voidcrates.placeholders

interface ServerPlaceholder {
    fun handle(args: List<String>): GenericResult
    fun id(): String
}
