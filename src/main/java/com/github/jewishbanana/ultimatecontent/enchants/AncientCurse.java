package com.github.jewishbanana.ultimatecontent.enchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.CustomEnchant;

public class AncientCurse extends CustomEnchant {
	
	public static final String REGISTERED_KEY = "uc:ancient_curse";
	public static final List<Material> applicableTypes = new ArrayList<>(Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
	static {
		Material copperSword = Material.getMaterial("COPPER_SWORD");
		if (copperSword != null)
			applicableTypes.add(1, copperSword);
	}
	
	public AncientCurse() {
		this.setMaxLevel(3);
	}
	public EnchantRarity getRarity() {
		return EnchantRarity.VERY_RARE;
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, AncientCurse.class);
		
		enchant.setApplicableTypes(applicableTypes);
		enchant.addApplicableTypes(Arrays.asList(Material.ENCHANTED_BOOK));
	}
}
