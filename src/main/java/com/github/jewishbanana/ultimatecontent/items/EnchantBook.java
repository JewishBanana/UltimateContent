package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class EnchantBook extends BaseItem {
	
	protected static String displayHex = Utils.convertString("(hex:#a1a1a1)");
	
	public EnchantBook(ItemStack item) {
		super(item);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.ENCHANTED_BOOKS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "enchant_books";
	}
}
