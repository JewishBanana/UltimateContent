package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.enchants.YetisBlessing;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class YetiFur extends CraftingMaterial {
	
	public static final String REGISTERED_KEY = "uc:yeti_fur";

	public YetiFur(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.WHITE_DYE).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100011).build();
	}
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, YetiFur.class);
		
		type.registerRecipe(createAnvilIncrementEnchantRecipe(type, YetisBlessing.REGISTERED_KEY, "yeti_fur_anvil", new RecipeChoice.MaterialChoice(YetisBlessing.applicableTypes),
				base -> base.getEnchantLevel(YetisBlessing.REGISTERED_KEY) * 5));
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}
