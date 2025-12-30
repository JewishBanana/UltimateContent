package com.github.jewishbanana.ultimatecontent.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.random.RandomGenerator;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.CustomLoadout;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Frosty;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Grinch;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Santa;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.DarkMage;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.PrimedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ShadowLeech;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SkeletonKnight;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SwampBeast;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.UndeadMiner;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ZombieKnight;
import com.github.jewishbanana.ultimatecontent.entities.desertentities.AncientMummy;
import com.github.jewishbanana.ultimatecontent.entities.desertentities.AncientSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.EasterBunny;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.KillerChicken;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.RampagingGoat;
import com.github.jewishbanana.ultimatecontent.entities.endentities.EndTotem;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidArcher;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidGuardian;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidStalker;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedEnderman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedHowler;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSpirit;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedTribesman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedZombie;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.FirePhantom;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.LostSoul;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.SoulReaper;
import com.github.jewishbanana.ultimatecontent.entities.snowentities.Yeti;
import com.github.jewishbanana.ultimatecontent.entities.waterentities.CursedDiver;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.SoundEffect;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public enum CustomEntityType {

	END_TOTEM(EndTotem.REGISTERED_KEY, Category.END_ENTITIES),
	VOID_WORM(VoidWorm.REGISTERED_KEY, Category.END_ENTITIES),
	VOID_GUARDIAN(VoidGuardian.REGISTERED_KEY, Category.END_ENTITIES),
	VOID_ARCHER(VoidArcher.REGISTERED_KEY, Category.END_ENTITIES),
	VOID_STALKER(VoidStalker.REGISTERED_KEY, Category.END_ENTITIES),
	
	ANCIENT_MUMMY(AncientMummy.REGISTERED_KEY, Category.DESERT_ENTITIES),
	ANCIENT_SKELETON(AncientSkeleton.REGISTERED_KEY, Category.DESERT_ENTITIES),
	
	PRIMED_CREEPER(PrimedCreeper.REGISTERED_KEY, Category.DARK_ENTITIES),
	DARK_MAGE(DarkMage.REGISTERED_KEY, Category.DARK_ENTITIES),
	SHADOW_LEECH(ShadowLeech.REGISTERED_KEY, Category.DARK_ENTITIES),
	SKELETON_KNIGHT(SkeletonKnight.REGISTERED_KEY, Category.DARK_ENTITIES),
	ZOMBIE_KNIGHT(ZombieKnight.REGISTERED_KEY, Category.DARK_ENTITIES),
	SWAMP_BEAST(SwampBeast.REGISTERED_KEY, Category.DARK_ENTITIES),
	UNDEAD_MINER(UndeadMiner.REGISTERED_KEY, Category.DARK_ENTITIES),
	
	CURSED_DIVER(CursedDiver.REGISTERED_KEY, Category.WATER_ENTITIES),
	
	YETI(Yeti.REGISTERED_KEY, Category.SNOW_ENTITIES),
	
	LOST_SOUL(LostSoul.REGISTERED_KEY, Category.NETHER_ENTITIES),
	SOUL_REAPER(SoulReaper.REGISTERED_KEY, Category.NETHER_ENTITIES),
	FIRE_PHANTOM(FirePhantom.REGISTERED_KEY, Category.NETHER_ENTITIES),
	
	INFESTED_ZOMBIE(InfestedZombie.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_SKELETON(InfestedSkeleton.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_CREEPER(InfestedCreeper.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_ENDERMAN(InfestedEnderman.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_SPIRIT(InfestedSpirit.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_TRIBESMAN(InfestedTribesman.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_DEVOURER(InfestedDevourer.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_HOWLER(InfestedHowler.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	INFESTED_WORM(InfestedWorm.REGISTERED_KEY, Category.INFESTED_ENTITIES),
	
	ELF(Elf.REGISTERED_KEY, Category.CHRISTMAS_ENTITIES),
	FROSTY(Frosty.REGISTERED_KEY, Category.CHRISTMAS_ENTITIES),
	GRINCH(Grinch.REGISTERED_KEY, Category.CHRISTMAS_ENTITIES),
	SANTA(Santa.REGISTERED_KEY, Category.CHRISTMAS_ENTITIES),
	
	KILLER_CHICKEN(KillerChicken.REGISTERED_KEY, Category.EASTER_ENTITIES),
	RAMPAGING_GOAT(RampagingGoat.REGISTERED_KEY, Category.EASTER_ENTITIES),
	EASTER_BUNNY(EasterBunny.REGISTERED_KEY, Category.EASTER_ENTITIES);
	
//	SCARECROW(Scarecrow.REGISTERED_KEY, Category.HALLOWEEN_ENTITIES),
//	GHOUL(Ghoul.REGISTERED_KEY, Category.HALLOWEEN_ENTITIES);
	
	public enum Category {
		END_ENTITIES,
		DESERT_ENTITIES,
		DARK_ENTITIES,
		WATER_ENTITIES,
		SNOW_ENTITIES,
		NETHER_ENTITIES,
		INFESTED_ENTITIES,
		CHRISTMAS_ENTITIES,
		EASTER_ENTITIES,
		HALLOWEEN_ENTITIES
	}
	
	private static RandomGenerator random = Utils.random();
	
	public final String registeredName;
	public final Category category;
	public final String configPath;
	public EntityVariant normalVariant;
	
	private Map<Double, Variant> variants = new HashMap<>();
	
	private CustomEntityType(String registeredName, Category category) {
		this.registeredName = registeredName;
		this.category = category;
		this.configPath = "entities."+category.toString().toLowerCase()+'.'+this.toString().toLowerCase()+'.';
	}
	private static void initDefaults(CustomEntityType type) {
		EntityVariant variant = new EntityVariant(type.configPath);
		type.normalVariant = variant;
		switch (type) {
		case END_TOTEM -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ENDERMITE_STEP, .8, .5) };
			variant.ambientSoundFrequency = 1;
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ENDERMAN_HURT, 1, .5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1, .5) };
			variant.movementSpeed = 0.32;
		}
		case VOID_GUARDIAN -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_DROWNED_AMBIENT, .8, .5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_DROWNED_DEATH, .8, .5) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 50, 50, 50, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.VOID_GUARD.getHead());
			variant.movementSpeed = 0.25;
		}
		case VOID_ARCHER -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 1, .8) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_DONKEY_DEATH, .3, .5) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 50, 50, 50, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.VOID_ARCHER.getHead());
			variant.movementSpeed = 0.05;
		}
		case VOID_WORM -> {
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_PIGLIN_BRUTE_DEATH, 1, .5) };
			variant.movementSpeed = 0.3;
		}
		case ELF -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_AMBIENT, 0.25, 2), new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_YES, 0.25, 2) };
			variant.ambientSoundFrequency = 5;
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_HURT, 1, 2) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_DEATH, 1, 2) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 30, 150, 0, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.CHRISTMAS_ELF1.getHead(), CustomHead.CHRISTMAS_ELF2.getHead());
			variant.movementSpeed = 0.25;
		}
		case GRINCH -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ZOMBIE_AMBIENT, 1, 0.5) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ZOMBIE_HURT, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ZOMBIE_DEATH, 1, 0.5) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 4, 94, 28, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.GRINCH.getHead());
			variant.movementSpeed = 0.35;
		}
		case SANTA -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_CELEBRATE, 2, 0.5) };
			variant.ambientSoundFrequency = 3;
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_HURT, 2, 0.6) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_DEATH, 2, 0.5) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 220, 0, 0, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SANTA.getHead());
			variant.movementSpeed = 0.3;
		}
		case ANCIENT_MUMMY -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_HUSK_AMBIENT, 1, 0.7) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_HUSK_HURT, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_HUSK_DEATH, 1, 0.6) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.MUMMY.getHead());
			variant.movementSpeed = 0.25;
		}
		case ANCIENT_SKELETON -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_SKELETON_AMBIENT, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_SKELETON_STEP, 1, 0.5) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_SKELETON_HURT, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_SKELETON_DEATH, 1, 0.5) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.ANCIENT_SKELETON.getHead());
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.MAIN_HAND, new ItemStack(Material.AIR));
			variant.movementSpeed = 0.3;
		}
		case SHADOW_LEECH -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ENDERMITE_AMBIENT, 1, 0.5) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_FOX_SCREECH, 1, 1.8) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ENDERMITE_DEATH, 0.5, 0.8) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SHADOW_LEECH.getHead());
			variant.movementSpeed = 0.15;
		}
		case SKELETON_KNIGHT -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SKELETON_KNIGHT.getHead());
		}
		case ZOMBIE_KNIGHT -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.ZOMBIE_KNIGHT.getHead());
		}
		case SWAMP_BEAST -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SWAMP_BEAST.getHead());
			variant.movementSpeed = 0.2;
		}
		case KILLER_CHICKEN -> {
			variant.knockback = 1.0;
			variant.movementSpeed = 0.25;
		}
		case EASTER_BUNNY -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_RABBIT_ATTACK, 1, 0.5) };
			variant.movementSpeed = 0.5;
		}
		case CURSED_DIVER -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.CURSED_DIVER.getHead());
			variant.movementSpeed = 0.3;
		}
		case LOST_SOUL -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VEX_AMBIENT, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_VEX_CHARGE, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VEX_DEATH, 1, .7) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.MAIN_HAND, new ItemStack(Material.AIR));
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.LOST_SOUL1.getHead(), CustomHead.LOST_SOUL2.getHead(), CustomHead.LOST_SOUL3.getHead(), CustomHead.LOST_SOUL4.getHead());
		}
		case SOUL_REAPER -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VEX_AMBIENT, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_VEX_CHARGE, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_CREEPER_DEATH, 1.5, .5) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SOUL_REAPER.getHead());
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.MAIN_HAND, new ItemStack(Material.AIR));
			variant.movementSpeed = 0.5;
		}
		case YETI -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_AMBIENT, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_ATTACK, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_CELEBRATE, 1, 0.5), new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_STUNNED, 1, 0.5) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_HURT, 1, 0.5) };
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_RAVAGER_DEATH, 2, 0.5) };
			variant.movementSpeed = 0.28;
			variant.knockback = 3.0;
		}
		case FIRE_PHANTOM -> {
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1, 0.5) };
		}
		case INFESTED_ZOMBIE -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_ZOMBIE.getHead());
			variant.movementSpeed = 0.25;
		}
		case INFESTED_SKELETON -> {
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_SKELETON.getHead());
			variant.movementSpeed = 0.3;
		}
		case INFESTED_CREEPER -> {
			variant.movementSpeed = 0.25;
		}
		case INFESTED_ENDERMAN -> {
			variant.movementSpeed = 0.35;
		}
		case INFESTED_SPIRIT -> {
			if (VersionUtils.isMCVersionOrAbove("1.19")) {
				variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ALLAY_DEATH, .8, .5), new SoundEffect(type.normalVariant, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .8, .5) };
				variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1, .7) };
			}
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.MAIN_HAND, new ItemStack(Material.AIR));
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_SPIRIT1.getHead(), CustomHead.INFESTED_SPIRIT2.getHead());
		}
		case INFESTED_TRIBESMAN -> {
			if (VersionUtils.isMCVersionOrAbove("1.19")) {
				variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1, () -> random.nextFloat() + 0.5F) };
				variant.ambientSoundFrequency = 3;
				variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.BLOCK_SCULK_SENSOR_BREAK, 1, 1.5f) };
			}
			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_VILLAGER_DEATH, 1, 2) };
			setLoadoutToLeatherArmor(variant.defaultLoadout, 30, 150, 0, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_TRIBESMAN1.getHead());
			variant.defaultLoadout.addEquipmentSlotItem(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_TRIBESMAN2.getHead(), 33.3f);
			variant.movementSpeed = 0.25;
		}
		case INFESTED_DEVOURER -> {
			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_FOX_AGGRO, 1, 0.5) };
			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_FOX_EAT, 1, .5) };
			if (VersionUtils.isMCVersionOrAbove("1.19"))
				variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.BLOCK_SCULK_SHRIEKER_BREAK, 2, .5) };
			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_DEVOURER.getHead());
			variant.movementSpeed = 0.3;
		}
		case INFESTED_HOWLER -> {
//			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_FOX_AGGRO, 1, 0.5) };
			if (VersionUtils.isMCVersionOrAbove("1.19")) {
				variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.BLOCK_SCULK_SHRIEKER_BREAK, 1, .8) };
				variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1, 2) };
			}
//			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_HOWLER.getHead());
			variant.movementSpeed = 0.15;
		}
		case INFESTED_WORM -> {}
//		case SCARECROW -> {
//			variant.ambientSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_WITHER_AMBIENT, 2, .5) };
//			variant.hurtSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_ZOMBIE_VILLAGER_HURT, 1, .5) };
//			variant.deathSounds = new SoundEffect[] { new SoundEffect(type.normalVariant, Sound.ENTITY_WITHER_SKELETON_DEATH, 2, .5) };
//			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.MAIN_HAND, new ItemStack(Material.JACK_O_LANTERN));
//			setLoadoutToLeatherArmor(variant.defaultLoadout, 105, 68, 31, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
//			variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.SCARECROW.getHead());
//			variant.movementSpeed = 0.5;
//		}
//		case GHOUL -> {}
		default -> {}
		}
	}
	private static void setLoadoutToLeatherArmor(CustomLoadout loadout, int red, int green, int blue, LoadoutEquipmentSlot... slots) {
		for (LoadoutEquipmentSlot slot : slots) {
			ItemStack item = switch (slot) {
			case FEET -> new ItemStack(Material.LEATHER_BOOTS);
			case LEGS -> new ItemStack(Material.LEATHER_LEGGINGS);
			case CHEST -> new ItemStack(Material.LEATHER_CHESTPLATE);
			case HEAD -> new ItemStack(Material.LEATHER_HELMET);
			default -> null;
			};
			if (item == null)
				continue;
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(Color.fromRGB(red, green, blue));
			item.setItemMeta(meta);
			loadout.addEquipmentSlotDefaults(slot, item);
		}
	}
	public Variant determineVariant(Entity entity) {
		if (variants.isEmpty())
			return null;
		if (entity.getPersistentDataContainer().has(Variant.variantKey, PersistentDataType.STRING)) {
			String key = entity.getPersistentDataContainer().get(Variant.variantKey, PersistentDataType.STRING);
			if (key.equals("NORMAL"))
				return null;
			for (Variant variant : variants.values())
				if (variant.getKey().equals(key))
					return variant;
			return null;
		}
		for (Entry<Double, Variant> entry : variants.entrySet())
			if (random.nextDouble() < entry.getKey()) {
				Variant variant = entry.getValue();
				entity.getPersistentDataContainer().set(Variant.variantKey, PersistentDataType.STRING, variant.getKey());
				return variant;
			}
		entity.getPersistentDataContainer().set(Variant.variantKey, PersistentDataType.STRING, "NORMAL");
		return null;
	}
	public static void initDefaults() {
		for (CustomEntityType type : values())
			initDefaults(type);
	}
	private static void setup(CustomEntityType type, JavaPlugin plugin) {
		UIEntityManager entityType = UIEntityManager.getEntityType(type.registeredName);
		if (entityType != null)
			entityType.setSpawnRate(DataUtils.getConfigDouble(type.configPath+"spawnRate", 0.0) / 100.0);
		type.variants.clear();
		Queue<Variant> typeVariants = Variant.variants.get(type);
		if (typeVariants != null)
			typeVariants.forEach(var -> {
				ConfigurationSection section = plugin.getConfig().getConfigurationSection(type.configPath+"variants."+var.getKey());
				if (section == null)
					return;
				var.getEntityVariant().overwriteFromPath(type.configPath+"variants."+var.getKey());
				if (section.contains("conversionChance"))
					type.variants.put(DataUtils.getConfigDouble(type.configPath+"variants."+var.getKey()+".conversionChance", 0.0) / 100.0, var);
			});
	}
	public int getSectionInteger(String value, int defaultValue) {
		return DataUtils.getConfigInt(normalVariant.configPath+value, defaultValue);
	}
	public double getSectionDouble(String value, double defaultValue) {
		return DataUtils.getConfigDouble(normalVariant.configPath+value, defaultValue);
	}
	public boolean getSectionBoolean(String value, boolean defaultValue) {
		return DataUtils.getConfigBoolean(normalVariant.configPath+value, defaultValue);
	}
	public String getSectionString(String value, String defaultValue) {
		return DataUtils.getConfigString(normalVariant.configPath+value, defaultValue);
	}
	public static void reload(JavaPlugin plugin) {
		for (CustomEntityType type : values())
			try {
				setup(type, plugin);
			} catch (Exception e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in reading config data for entity &f"+type.normalVariant.displayName+" &cthe entities section has a syntax error. Please look over the instructions in the config above the entities section to see how to properly set up custom equipment load outs. This entity will use its default settings!"));
			}
	}
}
