package com.github.jewishbanana.ultimatecontent.utils;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;

public class VersionUtils {
	
	private static double mcVersion;
	
	private static Enchantment sharpness;
	private static Enchantment smite;
	private static Enchantment athropods;
	private static Enchantment unbreaking;
	
	public static boolean displaysAllowed;
	public static boolean usingNewDamageEvent;
	
	private static Particle block_dust;
	private static Particle block_crack;
	private static Particle redstone_dust;
	private static Particle normal_smoke;
	private static Particle large_smoke;
	private static Particle snow_shovel;
	
	private static PotionEffectType jump_boost;
	private static PotionEffectType slowness;
	
	private static ItemFlag hide_effects;
	
	static {
		sharpness = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness"));
		smite = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("smite"));
		athropods = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("bane_of_arthropods"));
		unbreaking = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
		
		jump_boost = Registry.EFFECT.get(NamespacedKey.minecraft("jump_boost"));
		slowness = Registry.EFFECT.get(NamespacedKey.minecraft("slowness"));
		
		Integer[] version = Arrays.stream(Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-')).split("\\.")).map(e -> Integer.parseInt(e)).toArray(Integer[]::new);
		mcVersion = version[0] + (version[1] / 100);
		if (version[1] > 20 || (version[1] == 20 && version[2] >= 4))
			usingNewDamageEvent = true;
		if (version[1] >= 20 || (version[1] == 19 && version[2] >= 4))
			displaysAllowed = true;
		if (version[1] > 20 || (version[1] == 20 && version[2] >= 5)) {
			block_dust = Particle.DUST_PILLAR;
			block_crack = Particle.BLOCK;
			redstone_dust = Particle.DUST;
			normal_smoke = Particle.SMOKE;
			large_smoke = Particle.LARGE_SMOKE;
			snow_shovel = Particle.ITEM_SNOWBALL;
			
			hide_effects = ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
		} else {
			block_dust = Particle.valueOf("BLOCK_DUST");
			block_crack = Particle.valueOf("BLOCK_CRACK");
			redstone_dust = Particle.valueOf("REDSTONE");
			normal_smoke = Particle.valueOf("SMOKE_NORMAL");
			large_smoke = Particle.valueOf("SMOKE_LARGE");
			snow_shovel = Particle.valueOf("SNOW_SHOVEL");
			
			hide_effects = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
		}
	}
	public static double getMCVersion() {
		return mcVersion;
	}
	public static Enchantment getSharpness() {
		return sharpness;
	}
	public static Enchantment getSmite() {
		return smite;
	}
	public static Enchantment getAthropods() {
		return athropods;
	}
	public static Enchantment getUnbreaking() {
		return unbreaking;
	}
	public static Particle getBlockDust() {
		return block_dust;
	}
	public static Particle getBlockCrack() {
		return block_crack;
	}
	public static Particle getRedstoneDust() {
		return redstone_dust;
	}
	public static Particle getNormalSmoke() {
		return normal_smoke;
	}
	public static Particle getLargeSmoke() {
		return large_smoke;
	}
	public static Particle getSnowShovel() {
		return snow_shovel;
	}
	public static PotionEffectType getJumpBoost() {
		return jump_boost;
	}
	public static PotionEffectType getSlowness() {
		return slowness;
	}
	public static ItemFlag getHideEffects() {
		return hide_effects;
	}
}
