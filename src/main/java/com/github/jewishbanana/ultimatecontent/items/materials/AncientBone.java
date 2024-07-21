package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class AncientBone extends CraftingMaterial {
	
	public static String REGISTERED_KEY = "ui:ancient_bone";

	public AncientBone(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.BONE).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100007).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, AncientBone.class);
	}
}
