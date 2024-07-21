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
import com.github.jewishbanana.ultimatecontent.utils.Utils;

@SuppressWarnings("deprecation")
public class EnchantBook extends BaseItem {
	
	protected static String displayHex = Utils.convertString("(hex:#a1a1a1)");
	
	public EnchantBook(ItemStack item) {
		super(item);
	}
	protected static AnvilRecipe createAnvilBookRecipe(String enchant, List<ItemStack> ingredients) {
		UIEnchantment type = UIEnchantment.getEnchant(enchant);
		if (type == null)
			return null;
		AnvilRecipe recipe = new AnvilRecipe(ingredients, (event) -> {
			ItemStack item = event.getInventory().getItem(0).clone();
			int level = type.getEnchantLevel(item);
			int bookLevel = type.getEnchantLevel(event.getInventory().getItem(1));
			if (level >= type.getMaxLevel() || item.getAmount() != 1 || !type.canBeEnchanted(item) || level > bookLevel)
				return new ItemStack(Material.AIR);
			ItemMeta meta = item.getItemMeta();
			ItemListener.attachRecipeMetaFix(meta);
			item.setItemMeta(meta);
			GenericItem base = GenericItem.createItemBase(item);
			if (level > 0)
				type.unloadEnchant(base);
			if (type.addEnchant(base, bookLevel > level ? bookLevel : level+1, true)) {
				type.loadEnchant(base);
				base.getType().getBuilder().assembleLore(item, item.getItemMeta(), base.getType(), base);
				return item;
			}
			return new ItemStack(Material.AIR);
		}, false);
		recipe.setSlot(AnvilSlot.SECOND);
		return recipe;
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.ENCHANTED_BOOKS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "enchant_books";
	}
}
