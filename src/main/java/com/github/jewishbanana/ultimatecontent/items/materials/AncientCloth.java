package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class AncientCloth extends CraftingMaterial {
	
	public static String REGISTERED_KEY = "ui:ancient_cloth";

	public AncientCloth(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.PAPER).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100008).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, AncientCloth.class);
	}
}
