package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class AncientBlade extends Weapon {
	
	public static String REGISTERED_KEY = "ui:ancient_blade";

	public AncientBlade(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.NETHERITE_SWORD).assembleLore().setCustomModelData(100005).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, AncientBlade.class);
	}
}
