package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class AncientBlade extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:ancient_blade";

	public AncientBlade(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.NETHERITE_SWORD).assembleLore().setCustomModelData(100005).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, AncientBlade.class);
	}
	public Rarity getRarity() {
		return Rarity.LEGENDARY;
	}
}
