package dev.voidcrates.storage.database.sql.providers

import dev.voidcrates.VoidCrates
import dev.voidcrates.config.VoidCratesConfig
import com.zaxxer.hikari.HikariConfig
import java.io.File

class H2Provider(config: VoidCratesConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:h2:%s;AUTO_SERVER=TRUE",
        File(VoidCrates.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.h2.Driver"
    override fun getDriverName(): String = "h2"
    override fun configure(config: HikariConfig) {}
}
