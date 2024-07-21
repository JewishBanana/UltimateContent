package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class EnergyDrink extends BaseItem {
	
	public static String REGISTERED_KEY = "ui:energy_drink";
	
	public EnergyDrink(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.POTION).accessMeta(e -> {
			PotionMeta meta = (PotionMeta) e;
			meta.setColor(Color.fromARGB(150, 37, 242, 34));
			meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1, true, false), true);
			meta.addCustomEffect(new PotionEffect(VersionUtils.getJumpBoost(), 600, 1, true, false), true);
			meta.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 4, true, false), true);
		}).addItemFlags(VersionUtils.getHideEffects()).assembleLore().build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, EnergyDrink.class);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.CONSUMABLES.getItemCategory();
	}
}