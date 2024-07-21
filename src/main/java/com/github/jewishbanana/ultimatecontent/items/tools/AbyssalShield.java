package com.github.jewishbanana.ultimatecontent.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Tool;

public class AbyssalShield extends Tool {
	
	public static String REGISTERED_KEY = "ui:abyssal_shield";

	public AbyssalShield(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.SHIELD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100003).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, AbyssalShield.class);
	}
}
