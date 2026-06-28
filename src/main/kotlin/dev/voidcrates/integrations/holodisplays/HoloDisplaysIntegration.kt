package dev.voidcrates.integrations.holodisplays

import dev.voidcrates.integrations.IntegratedMod
import dev.voidcrates.managers.HologramsManager
import dev.voidcrates.utils.Utils

class HoloDisplaysIntegration: IntegratedMod {
    override fun onServerStarted() {
        Utils.printInfo("The mod HoloDisplays was found, enabling integrations...")
        HologramsManager.load()
    }

    override fun onServerShutdown() {
        Utils.printInfo("Shutting down HoloDisplays integrations...")
        HologramsManager.unload()
    }
}