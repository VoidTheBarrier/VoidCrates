package dev.voidcrates.data

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import dev.voidcrates.config.ConfigManager
import dev.voidcrates.config.CostOptions
import dev.voidcrates.config.FailureOptions
import dev.voidcrates.config.block.BlockOptions
import dev.voidcrates.config.item.GenericItem
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.userdata.UserData
import dev.voidcrates.utils.RandomCollection

class Crate(
    val enabled: Boolean = true,
    val name: String = "",
    val display: GenericItem = GenericItem(),
    val unique: Boolean = false,
    val preview: String = "",
    val animation: String = "",
    val permission: String = "",
    @SerializedName("inventory_space")
    val inventorySpace: Int = -1,
    val cost: CostOptions? = null,
    val cooldown: Long = -1,
    val failure: FailureOptions? = null,
    val keys: Map<String, Int> = emptyMap(),
    @SerializedName("hold_key")
    val holdKey: Boolean = false,
    val block: BlockOptions = BlockOptions(),
    @JsonAdapter(Reward.RewardMapAdapter::class)
    val rewards: MutableMap<String, Reward> = mutableMapOf(),
) {
    // Local variable that is filled in when creating the object
    @Transient
    lateinit var id: String

    fun parsePlaceholders(string: String): String {
        return string.replace("%crate_name%", name)
            .replace("%crate_id%", id)
            .replace("%crate_keys%", keys.entries.joinToString(", ") { (keyId, amount) ->
                "${ConfigManager.KEYS[keyId]?.name ?: keyId} x$amount"
            })
            .replace("%crate_inventory_space%", inventorySpace.toString())
    }

    fun generateRewardBag(data: UserData): RandomCollection<Reward> {
        val bag = RandomCollection<Reward>()
        for (reward in rewards.values) {
            if (!reward.canReceive(data, this)) continue
            bag.add(reward, reward.weight.toDouble())
        }

        return bag
    }

    override fun toString(): String {
        return "Crate(id='$id', enabled=$enabled, name='$name', display=$display, unique=$unique, preview='$preview', " +
                "animation='$animation', permission='$permission', cost=$cost, cooldown=$cooldown, keys=$keys, " +
                "block=$block, rewards=$rewards)"
    }
}