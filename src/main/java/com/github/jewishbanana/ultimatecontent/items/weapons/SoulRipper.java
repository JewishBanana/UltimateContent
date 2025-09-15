package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class SoulRipper extends Weapon {

	public static final String REGISTERED_KEY = "uc:soul_ripper";

	public SoulRipper(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.IRON_HOE).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100010).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, SoulRipper.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
