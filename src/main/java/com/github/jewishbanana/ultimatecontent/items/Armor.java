package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemCategory;

public class Armor extends BaseItem {

	public Armor(ItemStack item) {
		super(item);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.ARMOR.getItemCategory();
	}
	public String getConfigItemSection() {
		return "armor";
	}
}
