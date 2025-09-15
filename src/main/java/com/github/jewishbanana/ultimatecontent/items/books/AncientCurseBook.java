package com.github.jewishbanana.ultimatecontent.items.books;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.EnchantBook;

public class AncientCurseBook extends EnchantBook {
	
	public static final String REGISTERED_KEY = "uc:ancient_curse_book";

	public AncientCurseBook(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.ENCHANTED_BOOK).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, AncientCurseBook.class);
	}
	public String getDisplayName() {
		return displayHex+"Ancient Curse Book";
	}
}
