package com.github.jewishbanana.ultimatecontent.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Tool;

public class AbyssalShield extends Tool {
	
	public static final String REGISTERED_KEY = "uc:abyssal_shield";

	public AbyssalShield(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.SHIELD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100003).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, AbyssalShield.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
