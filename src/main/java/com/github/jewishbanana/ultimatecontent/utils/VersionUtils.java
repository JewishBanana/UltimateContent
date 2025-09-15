package com.github.jewishbanana.ultimatecontent.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;

public class VersionUtils {
	
	private static Integer[] mcVersion;
	
	private static final Enchantment sharpness;
	private static final Enchantment smite;
	private static final Enchantment athropods;
	private static final Enchantment unbreaking;
	private static final Enchantment efficiency;
	private static final Enchantment luckOfTheSea;
	
	public static boolean displaysAllowed;
	public static boolean usingNewDamageEvent;
	
	private static final Particle block_dust;
	private static final Particle block_crack;
	private static final Particle item_crack;
	private static final Particle redstone_dust;
	private static final Particle normal_smoke;
	private static final Particle large_smoke;
	private static final Particle snow_shovel;
	private static final Particle enchant;
	private static final Particle water_splash;
	
	private static final PotionEffectType jump_boost;
	private static final PotionEffectType slowness;
	private static final PotionEffectType resistance;
	
	private static final ItemFlag hide_effects;
	
	private static final Set<Biome> desertBiomes;
	private static final Set<Biome> swampBiomes;
	
	private static final Material short_grass;
	
	static {
		sharpness = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness"));
		smite = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("smite"));
		athropods = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("bane_of_arthropods"));
		unbreaking = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
		efficiency = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency"));
		luckOfTheSea = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("luck_of_the_sea"));
		
		jump_boost = Registry.EFFECT.get(NamespacedKey.minecraft("jump_boost"));
		slowness = Registry.EFFECT.get(NamespacedKey.minecraft("slowness"));
		resistance = Registry.EFFECT.get(NamespacedKey.minecraft("resistance"));
		
		mcVersion = Arrays.stream(Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-')).split("\\.")).map(e -> Integer.parseInt(e)).toArray(Integer[]::new);
		if (isMCVersionOrAbove("1.20.4")) {
			usingNewDamageEvent = true;
			short_grass = Material.SHORT_GRASS;
		} else {
			short_grass = Material.valueOf("GRASS");
		}
		if (isMCVersionOrAbove("1.19.4"))
			displaysAllowed = true;
		if (isMCVersionOrAbove("1.20.5")) {
			block_dust = Particle.DUST_PILLAR;
			block_crack = Particle.BLOCK;
			item_crack = Particle.ITEM;
			redstone_dust = Particle.DUST;
			normal_smoke = Particle.SMOKE;
			large_smoke = Particle.LARGE_SMOKE;
			snow_shovel = Particle.ITEM_SNOWBALL;
			enchant = Particle.ENCHANT;
			water_splash = Particle.SPLASH;
			
			hide_effects = ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
		} else {
			block_dust = Particle.valueOf("BLOCK_DUST");
			block_crack = Particle.valueOf("BLOCK_CRACK");
			item_crack = Particle.valueOf("ITEM_CRACK");
			redstone_dust = Particle.valueOf("REDSTONE");
			normal_smoke = Particle.valueOf("SMOKE_NORMAL");
			large_smoke = Particle.valueOf("SMOKE_LARGE");
			snow_shovel = Particle.valueOf("SNOW_SHOVEL");
			enchant = Particle.valueOf("ENCHANTMENT_TABLE");
			water_splash = Particle.valueOf("WATER_SPLASH");
			
			hide_effects = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
		}
		if (isMCVersionOrAbove("1.18"))
			desertBiomes = new HashSet<>(Arrays.asList(Biome.DESERT, Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS));
		else
			desertBiomes = new HashSet<>(Arrays.asList(Biome.DESERT, Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.valueOf("WOODED_BADLANDS_PLATEAU"), Biome.valueOf("MODIFIED_WOODED_BADLANDS_PLATEAU"), Biome.valueOf("MODIFIED_BADLANDS_PLATEAU"), Biome.valueOf("DESERT_LAKES"), Biome.valueOf("DESERT_HILLS"), Biome.valueOf("BADLANDS_PLATEAU")));
		if (isMCVersionOrAbove("1.19"))
			swampBiomes = new HashSet<>(Arrays.asList(Biome.SWAMP, Biome.MANGROVE_SWAMP));
		else
			swampBiomes = new HashSet<>(Arrays.asList(Biome.SWAMP));
	}
	public static boolean isMCVersionOrAbove(String version) {
		try {
			String[] test = version.split("\\.");
			for (int i = 0; i < test.length; i++) {
	            if (i >= mcVersion.length)
	                return false;
	            int testSegment = Integer.parseInt(test[i]);
	            if (mcVersion[i] > testSegment)
	                return true;
	            else if (mcVersion[i] < testSegment)
	                return false;
	        }
	        return true;
		} catch (NumberFormatException ex) {
			throw new NumberFormatException("The version string you supplied '"+version+"' is not a valid version string! Format must be as follows: '1.2.3' or '1.2' or '1'!");
		}
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
	public static Enchantment getEfficiency() {
		return efficiency;
	}
	public static Enchantment getLuckOfTheSea() {
		return luckOfTheSea;
	}
	public static Particle getBlockDust() {
		return block_dust;
	}
	public static Particle getBlockCrack() {
		return block_crack;
	}
	public static Particle getItemCrack() {
		return item_crack;
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
	public static Particle getEnchantParticle() {
		return enchant;
	}
	public static Particle getWaterSplash() {
		return water_splash;
	}
	public static PotionEffectType getJumpBoost() {
		return jump_boost;
	}
	public static PotionEffectType getSlowness() {
		return slowness;
	}
	public static PotionEffectType getResistance() {
		return resistance;
	}
	public static ItemFlag getHideEffects() {
		return hide_effects;
	}
	public static boolean isBiomeDesert(Biome biome) {
		return desertBiomes.contains(biome);
	}
	public static boolean isBiomeSwamp(Biome biome) {
		return swampBiomes.contains(biome);
	}
	public static Material getShortGrass() {
		return short_grass;
	}
}
