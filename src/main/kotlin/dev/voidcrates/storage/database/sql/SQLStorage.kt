package dev.voidcrates.storage.database.sql

import com.google.gson.reflect.TypeToken
import dev.voidcrates.VoidCrates
import dev.voidcrates.config.VoidCratesConfig
import dev.voidcrates.data.userdata.CrateData
import dev.voidcrates.data.userdata.UsedKeyData
import dev.voidcrates.data.userdata.UserData
import dev.voidcrates.storage.IStorage
import dev.voidcrates.storage.StorageType
import dev.voidcrates.storage.database.sql.providers.MySQLProvider
import dev.voidcrates.storage.database.sql.providers.SQLiteProvider
import java.lang.reflect.Type
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: VoidCratesConfig.Storage) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
    private val cratesType: Type = object : TypeToken<HashMap<String, CrateData>>() {}.type
    private val keysType: Type = object : TypeToken<HashMap<String, Int>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    userData.crates = VoidCrates.INSTANCE.gson.fromJson(result.getString("crates"), cratesType)
                    userData.keys = VoidCrates.INSTANCE.gson.fromJson(result.getString("keys"), keysType)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(userData: UserData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid, crates, `keys`) VALUES ('%s', '%s', '%s')",
                    userData.uuid.toString(),
                    VoidCrates.INSTANCE.gson.toJson(userData.crates),
                    VoidCrates.INSTANCE.gson.toJson(userData.keys)
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUsedKey(uuid: UUID): UsedKeyData? {
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}used_keys WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    return UsedKeyData(
                        uuid,
                        result.getString("keyId"),
                        result.getLong("timeUsed"),
                        UUID.fromString(result.getString("player"))
                    )
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun saveUsedKey(usedKeyData: UsedKeyData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}used_keys (uuid, keyId, timeUsed, player) VALUES ('%s', '%s', %d, '%s')",
                    usedKeyData.uuid.toString(),
                    usedKeyData.keyId,
                    usedKeyData.timeUsed,
                    usedKeyData.player.toString()
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                getUser(uuid)
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, VoidCrates.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(userData)
        }, VoidCrates.INSTANCE.asyncExecutor)
    }

    override fun getUsedKeyAsync(uuid: UUID): CompletableFuture<UsedKeyData?> {
        return CompletableFuture.supplyAsync({
            getUsedKey(uuid)
        }, VoidCrates.INSTANCE.asyncExecutor)
    }

    override fun saveUsedKeyAsync(usedKeyData: UsedKeyData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUsedKey(usedKeyData)
        }, VoidCrates.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
