package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemCategory;

public class Tool extends BaseItem {

	public Tool(ItemStack item) {
		super(item);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.TOOLS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "tools";
	}
}
