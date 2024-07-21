package com.github.jewishbanana.ultimatecontent.items.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.items.CustomEnchant;

public class YetisBlessing extends CustomEnchant {
	
	public static String REGISTERED_KEY = "ui:yetis_blessing";
	public static List<Material> applicableTypes = Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);
	
	public YetisBlessing(String registeredName, int id) {
		super(registeredName, id);
		this.setMaxLevel(3);
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, YetisBlessing.class);
		
		enchant.setApplicableTypes(applicableTypes);
	}
}
