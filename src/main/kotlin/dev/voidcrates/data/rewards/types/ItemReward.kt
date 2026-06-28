package dev.voidcrates.data.rewards.types

import dev.voidcrates.config.item.GenericItem
import dev.voidcrates.data.Crate
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.rewards.RewardLimits
import dev.voidcrates.data.rewards.RewardType
import net.minecraft.server.level.ServerPlayer

class ItemReward(
    name: String = "",
    description: List<String> = emptyList(),
    display: GenericItem? = null,
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    private val item: GenericItem = GenericItem()
) : Reward(RewardType.ITEM, name, description, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        player.inventory.placeItemBackInInventory(item.createItemStack(player))
    }

    override fun getGenericDisplay(): GenericItem {
        return display ?: item
    }

    override fun toString(): String {
        return "ItemReward(name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, item=$item)"
    }
}
