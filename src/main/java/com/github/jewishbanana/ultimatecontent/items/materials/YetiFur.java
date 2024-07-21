package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.enchants.YetisBlessing;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class YetiFur extends CraftingMaterial {
	
	public static String REGISTERED_KEY = "ui:yeti_fur";

	public YetiFur(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.WHITE_DYE).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100011).build();
	}
	public static void register() {
		ItemType type = ItemType.registerItem(REGISTERED_KEY, YetiFur.class);
		
		type.registerRecipe(createAnvilIncrementEnchantRecipe(YetisBlessing.REGISTERED_KEY, Utils.createIngredients(YetisBlessing.applicableTypes), false));
	}
}
