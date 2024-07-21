package com.github.jewishbanana.ultimatecontent.items.easter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class GoldenEgg extends CraftingMaterial {
	
	public static String REGISTERED_KEY = "ui:golden_egg";

	public GoldenEgg(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.TURTLE_EGG).assembleLore().setCustomModelData(100028).build();
	}
	public static void register() {
		ItemType type = ItemType.registerItem(REGISTERED_KEY, GoldenEgg.class);
		
		type.registerRecipe(createAnvilIncrementEnchantRecipe(BunnyHop.REGISTERED_KEY, Utils.createIngredients(BunnyHop.applicableTypes), false));
	}
	public String getConfigItemSection() {
		return "easter_items";
	}
}
