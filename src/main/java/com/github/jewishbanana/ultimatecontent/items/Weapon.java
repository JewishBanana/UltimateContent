package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemCategory;

public class Weapon extends BaseItem {
	
	public Weapon(ItemStack item) {
		super(item);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.WEAPONS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "weapons";
	}
}
