package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class BloodPact extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:blood_pact";

	public BloodPact(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.NETHERITE_AXE).assembleLore().setCustomModelData(100017).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, BloodPact.class);
	}
	public Rarity getRarity() {
		return Rarity.LEGENDARY;
	}
}