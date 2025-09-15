package com.github.jewishbanana.ultimatecontent.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.CustomEnchant;

public class YetisBlessing extends CustomEnchant {
	
	public static final String REGISTERED_KEY = "uc:yetis_blessing";
	public static final List<Material> applicableTypes = Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);
	
	public YetisBlessing() {
		this.setMaxLevel(3);
	}
	public EnchantRarity getRarity() {
		return EnchantRarity.RARE;
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, YetisBlessing.class);
		
		enchant.setApplicableTypes(applicableTypes);
	}
}
