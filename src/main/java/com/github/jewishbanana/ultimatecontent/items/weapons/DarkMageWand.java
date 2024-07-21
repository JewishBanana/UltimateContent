package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class DarkMageWand extends Weapon {
	
	public static String REGISTERED_KEY = "ui:dark_mage_wand";

	public DarkMageWand(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.BLAZE_ROD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100009).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, DarkMageWand.class);
	}
}
