package com.github.jewishbanana.ultimatecontent.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.uiframework.listeners.ItemListener;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe.AnvilSlot;

@SuppressWarnings("deprecation")
public class CraftingMaterial extends BaseItem {

	public CraftingMaterial(ItemStack item) {
		super(item);
	}
	protected static AnvilRecipe createAnvilIncrementEnchantRecipe(String enchant, List<ItemStack> ingredients, boolean exactIngredients) {
		UIEnchantment type = UIEnchantment.getEnchant(enchant);
		if (type == null)
			return null;
		AnvilRecipe recipe = new AnvilRecipe(ingredients, (event) -> {
			ItemStack item = event.getInventory().getItem(0).clone();
			int level = type.getEnchantLevel(item);
			if (level >= type.getMaxLevel() || item.getAmount() != event.getInventory().getItem(1).getAmount() || !type.canBeEnchanted(item))
				return new ItemStack(Material.AIR);
			ItemMeta meta = item.getItemMeta();
			ItemListener.attachRecipeMetaFix(meta);
			item.setItemMeta(meta);
			GenericItem base = GenericItem.createItemBase(item);
			if (level > 0)
				type.unloadEnchant(base);
			if (type.addEnchant(base, level+1, true)) {
				type.loadEnchant(base);
				base.getType().getBuilder().assembleLore(item, item.getItemMeta(), base.getType(), base);
				return item;
			}
			return new ItemStack(Material.AIR);
		}, exactIngredients);
		recipe.setSlot(AnvilSlot.SECOND);
		return recipe;
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.CRAFTING_INGREDIENTS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "materials";
	}
}
