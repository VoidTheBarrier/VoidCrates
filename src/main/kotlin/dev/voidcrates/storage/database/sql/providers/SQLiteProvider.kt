package dev.voidcrates.storage.database.sql.providers

import dev.voidcrates.VoidCrates
import dev.voidcrates.config.VoidCratesConfig
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: VoidCratesConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File(VoidCrates.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
