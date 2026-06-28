package dev.voidcrates.integrations

interface IntegratedMod {
    fun onInit() {}
    fun onServerStarted() {}
    fun onServerStarting() {}
    fun onServerShutdown() {}
}