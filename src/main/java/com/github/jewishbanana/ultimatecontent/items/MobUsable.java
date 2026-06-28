package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIItemType;

public interface MobUsable {
	int getMobUseRange();
	int getMobUseIntervalTicks();
	void onMobHoldTick(LivingEntity mob);

	/**
	 * Consumes a single count of the given item from the mob's hand. Used by mob-side ability activation so consumable items
	 * (config {@code shouldConsume: true}) decrement just like a player using them. Decrements the stack, or sets the slot to
	 * AIR when it was the last item. Matches by the custom item type rather than {@code isSimilar} so a refreshed/normalized
	 * item meta does not cause a miss.
	 *
	 * @param mob The mob holding the item
	 * @param item The item instance to consume one of
	 */
	static void consumeFromMob(LivingEntity mob, GenericItem item) {
		EntityEquipment eq = mob.getEquipment();
		if (eq == null || item == null)
			return;
		UIItemType type = item.getType();
		ItemStack hand = eq.getItemInMainHand();
		GenericItem handBase = hand == null ? null : GenericItem.getItemBase(hand);
		if (handBase != null && handBase.getType().equals(type)) {
			if (hand.getAmount() <= 1)
				eq.setItemInMainHand(new ItemStack(Material.AIR));
			else {
				hand.setAmount(hand.getAmount() - 1);
				eq.setItemInMainHand(hand);
			}
			return;
		}
		ItemStack offHand = eq.getItemInOffHand();
		GenericItem offBase = offHand == null ? null : GenericItem.getItemBase(offHand);
		if (offBase != null && offBase.getType().equals(type)) {
			if (offHand.getAmount() <= 1)
				eq.setItemInOffHand(new ItemStack(Material.AIR));
			else {
				offHand.setAmount(offHand.getAmount() - 1);
				eq.setItemInOffHand(offHand);
			}
		}
	}
}
