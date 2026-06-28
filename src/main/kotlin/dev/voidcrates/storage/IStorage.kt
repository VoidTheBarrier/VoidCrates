package dev.voidcrates.storage

import dev.voidcrates.config.VoidCratesConfig
import dev.voidcrates.data.userdata.UsedKeyData
import dev.voidcrates.data.userdata.UserData
import dev.voidcrates.storage.database.MongoStorage
import dev.voidcrates.storage.database.sql.SQLStorage
import dev.voidcrates.storage.file.FileStorage
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.concurrent.CompletableFuture

interface IStorage {
    companion object {
        fun load(config: VoidCratesConfig.Storage): IStorage {
            return when (config.type) {
                StorageType.JSON -> FileStorage()
                StorageType.MONGO -> MongoStorage(config)
                StorageType.MYSQL, StorageType.SQLITE -> SQLStorage(config)
            }
        }
    }

    fun getUser(uuid: UUID): UserData
    fun getUser(player: ServerPlayer): UserData = getUser(player.uuid)
    fun saveUser(userData: UserData): Boolean
    fun getUsedKey(uuid: UUID): UsedKeyData?
    fun saveUsedKey(usedKeyData: UsedKeyData): Boolean

    fun getUserAsync(uuid: UUID): CompletableFuture<UserData>
    fun getUserAsync(player: ServerPlayer): CompletableFuture<UserData> = getUserAsync(player.uuid)
    fun saveUserAsync(userData: UserData): CompletableFuture<Boolean>
    fun getUsedKeyAsync(uuid: UUID): CompletableFuture<UsedKeyData?>
    fun saveUsedKeyAsync(usedKeyData: UsedKeyData): CompletableFuture<Boolean>

    fun close() {}
}
