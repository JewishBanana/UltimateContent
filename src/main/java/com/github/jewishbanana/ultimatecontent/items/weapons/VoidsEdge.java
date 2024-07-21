package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class VoidsEdge extends Weapon {
	
	public static String REGISTERED_KEY = "ui:voids_edge";

	public VoidsEdge(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.IRON_SWORD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100002).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, VoidsEdge.class);
	}
}
