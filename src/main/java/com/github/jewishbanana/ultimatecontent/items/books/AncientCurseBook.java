package com.github.jewishbanana.ultimatecontent.items.books;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.EnchantBook;
import com.github.jewishbanana.ultimatecontent.items.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class AncientCurseBook extends EnchantBook {
	
	public static String REGISTERED_KEY = "ui:ancient_curse_book";

	public AncientCurseBook(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.ENCHANTED_BOOK).build();
	}
	public static void register() {
		ItemType type = ItemType.registerItem(REGISTERED_KEY, AncientCurseBook.class);
		
		type.registerRecipe(createAnvilBookRecipe(AncientCurse.REGISTERED_KEY, Utils.createIngredients(AncientCurse.applicableTypes)));
	}
	public String getDisplayName() {
		return displayHex+"Ancient Curse Book";
	}
}
