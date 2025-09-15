package com.github.jewishbanana.ultimatecontent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.playerarmorchangeevent.PlayerArmorListener;
import com.github.jewishbanana.uiframework.UIFramework;
import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.Ability.Action;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes.Target;
import com.github.jewishbanana.ultimatecontent.abilities.BlackRift;
import com.github.jewishbanana.ultimatecontent.abilities.Blinding;
import com.github.jewishbanana.ultimatecontent.abilities.CursedWinds;
import com.github.jewishbanana.ultimatecontent.abilities.DoubleJump;
import com.github.jewishbanana.ultimatecontent.abilities.JumpBoost;
import com.github.jewishbanana.ultimatecontent.abilities.PropulsionBlast;
import com.github.jewishbanana.ultimatecontent.abilities.RestoreHealth;
import com.github.jewishbanana.ultimatecontent.abilities.SaberParry;
import com.github.jewishbanana.ultimatecontent.abilities.SaberThrow;
import com.github.jewishbanana.ultimatecontent.abilities.SpawnPlatform;
import com.github.jewishbanana.ultimatecontent.abilities.StasisZone;
import com.github.jewishbanana.ultimatecontent.abilities.TeleportRay;
import com.github.jewishbanana.ultimatecontent.abilities.TidalWave;
import com.github.jewishbanana.ultimatecontent.abilities.YetiRoar;
import com.github.jewishbanana.ultimatecontent.commands.MasterCommand;
import com.github.jewishbanana.ultimatecontent.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.enchants.YetisBlessing;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
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
import com.github.jewishbanana.ultimatecontent.entities.halloweenentities.Scarecrow;
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
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.BossSpawnItem;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.books.AncientCurseBook;
import com.github.jewishbanana.ultimatecontent.items.christmas.BrokenSnowGlobe;
import com.github.jewishbanana.ultimatecontent.items.christmas.CandyCane;
import com.github.jewishbanana.ultimatecontent.items.christmas.CursedCandyCane;
import com.github.jewishbanana.ultimatecontent.items.christmas.Ornament;
import com.github.jewishbanana.ultimatecontent.items.christmas.SantaHat;
import com.github.jewishbanana.ultimatecontent.items.christmas.SnowGlobe;
import com.github.jewishbanana.ultimatecontent.items.easter.BlueEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.EasterBasket;
import com.github.jewishbanana.ultimatecontent.items.easter.GoldenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.GreenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.OrangeEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.PurpleEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.RedEgg;
import com.github.jewishbanana.ultimatecontent.items.materials.AncientBone;
import com.github.jewishbanana.ultimatecontent.items.materials.AncientCloth;
import com.github.jewishbanana.ultimatecontent.items.materials.YetiFur;
import com.github.jewishbanana.ultimatecontent.items.misc.BlindingTrap;
import com.github.jewishbanana.ultimatecontent.items.misc.BoosterPack;
import com.github.jewishbanana.ultimatecontent.items.misc.EnergyDrink;
import com.github.jewishbanana.ultimatecontent.items.misc.HeartCrystal;
import com.github.jewishbanana.ultimatecontent.items.misc.MatterSwap;
import com.github.jewishbanana.ultimatecontent.items.misc.MedKit;
import com.github.jewishbanana.ultimatecontent.items.misc.PropulsionCannon;
import com.github.jewishbanana.ultimatecontent.items.misc.SafetyNet;
import com.github.jewishbanana.ultimatecontent.items.misc.VoidTear;
import com.github.jewishbanana.ultimatecontent.items.tools.AbyssalShield;
import com.github.jewishbanana.ultimatecontent.items.weapons.AncientBlade;
import com.github.jewishbanana.ultimatecontent.items.weapons.CallOfTheVoid;
import com.github.jewishbanana.ultimatecontent.items.weapons.DarkMageWand;
import com.github.jewishbanana.ultimatecontent.items.weapons.GreenLightsaber;
import com.github.jewishbanana.ultimatecontent.items.weapons.SoulRipper;
import com.github.jewishbanana.ultimatecontent.items.weapons.StasisGun;
import com.github.jewishbanana.ultimatecontent.items.weapons.TritonsFang;
import com.github.jewishbanana.ultimatecontent.items.weapons.VoidsEdge;
import com.github.jewishbanana.ultimatecontent.listeners.BossBlocksHandler;
import com.github.jewishbanana.ultimatecontent.listeners.DeathHandler;
import com.github.jewishbanana.ultimatecontent.listeners.DoubleJumpHandler;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.listeners.EntitySpawningHandler;
import com.github.jewishbanana.ultimatecontent.listeners.ItemMechanicsHandler;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.listeners.RegionHandler;
import com.github.jewishbanana.ultimatecontent.listeners.SantaHatHandler;
import com.github.jewishbanana.ultimatecontent.specialevents.SpecialEvent;
import com.github.jewishbanana.ultimatecontent.utils.ConfigUpdater;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.mojang.datafixers.util.Pair;

public class Main extends JavaPlugin {
	
	public static ConsoleCommandSender consoleSender;

	private static JavaPlugin instance;
	private static FixedMetadataValue fixedData;
	private static SpecialEvent specialEvent;
	
	private static final String UIFrameworkVersion = "3.0.0";
	
	public void onEnable() {
		instance = this;
		fixedData = new FixedMetadataValue(this, "protected");
		consoleSender = this.getServer().getConsoleSender();
		
		if (!UIFramework.isVersionOrAbove(UIFrameworkVersion)) {
			consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR Cannot start plugin because UIFramework is out of date! Please update to at least &a"+UIFrameworkVersion+" &c(Current version installed is &b"+(getServer().getPluginManager().getPlugin("UIFramework").getDescription().getVersion())+"&c). https://www.spigotmc.org/resources/uiframework.110768/"));
			this.setEnabled(false);
			return;
		}
		UIFramework.registerReloadRunnable(this, () -> reload());
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		try {
			ConfigUpdater.update(this, "config.yml", new File(getDataFolder().getAbsolutePath(), "config.yml"), null);
			this.reloadConfig();
		} catch (IOException e) {
			e.printStackTrace();
			consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cUnable to initialize config! Please report the full error above to the discord."));
		}
		
		DependencyUtils.init(this);
		CustomHead.init(this);
		
		BaseItem.createCustomCategories();
		registerAbilities();
		registerEnchants();
		registerItems();
		CustomEntityType.initDefaults();
		registerEntities();
		
		init();
		
		specialEvent = SpecialEvent.checkForEvent(this);
		if (specialEvent != null)
			specialEvent.reload();
	}
	public void onDisable() {
		if (specialEvent != null)
			specialEvent.saveData();
		BossSpawnItem.unloadBossBlocks();
		SantaHatHandler.removeEntities();
		BaseEntity.cleanAllEntities();
		
		DataUtils.saveDataFile();
		
		instance = null;
		fixedData = null;
	}
	private void registerAbilities() {
		SaberParry.register();
		SaberThrow.register();
		StasisZone.register();
		JumpBoost.register();
		SpawnPlatform.register();
		RestoreHealth.register();
		PropulsionBlast.register();
		TeleportRay.register();
		Blinding.register();
		CursedWinds.register();
		BlackRift.register();
		TidalWave.register();
		YetiRoar.register();
		DoubleJump.register();
	}
	private void registerEnchants() {
		AncientCurse.register();
		YetisBlessing.register();
		BunnyHop.register();
	}
	private void registerItems() {
		// Crafting Materials
		AncientBone.register();
		AncientCloth.register();
		YetiFur.register();
		
		// Weapons
		GreenLightsaber.register();
		StasisGun.register();
		AncientBlade.register();
		VoidsEdge.register();
		AbyssalShield.register();
		CallOfTheVoid.register();
		DarkMageWand.register();
		TritonsFang.register();
		SoulRipper.register();
		
		// Enchant Books
		AncientCurseBook.register();
		
		// Miscellaneous
		BoosterPack.register();
		SafetyNet.register();
		HeartCrystal.register();
		MedKit.register();
		EnergyDrink.register();
		PropulsionCannon.register();
		MatterSwap.register();
		BlindingTrap.register();
		VoidTear.register();
		
		// Easter
		GreenEgg.register();
		BlueEgg.register();
		RedEgg.register();
		OrangeEgg.register();
		PurpleEgg.register();
		GoldenEgg.register();
		EasterBasket.register();
		
		// Christmas
		CandyCane.register();
		CursedCandyCane.register();
		Ornament.register();
		BrokenSnowGlobe.register();
		SantaHat.register();
		SnowGlobe.register();
	}
	private void registerEntities() {
		// End Entities
		EndTotem.register();
		VoidWorm.register();
		VoidArcher.register();
		VoidGuardian.register();
		VoidStalker.register();
		
		// Dark Entities
		PrimedCreeper.register();
		DarkMage.register();
		ShadowLeech.register();
		SkeletonKnight.register();
		ZombieKnight.register();
		SwampBeast.register();
		UndeadMiner.register();
		
		// Desert Entities
		AncientMummy.register();
		AncientSkeleton.register();
		
		// Water Entities
		CursedDiver.register();
		
		// Snow Entities
		Yeti.register();
		
		// Infested Entities
		InfestedZombie.register();
		InfestedSkeleton.register();
		InfestedCreeper.register();
		InfestedEnderman.register();
		InfestedSpirit.register();
		InfestedTribesman.register();
		InfestedDevourer.register();
		InfestedHowler.register();
		InfestedWorm.register();
		
		// Nether Entities
		LostSoul.register();
		SoulReaper.register();
		FirePhantom.register();
		
		// Christmas Entities
		Elf.register();
		Frosty.register();
		Grinch.register();
		Santa.register();
		
		// Easter Entities
		KillerChicken.register();
		RampagingGoat.register();
		EasterBunny.register();
		
		// Halloween Entities
		Scarecrow.register();
	}
	private void init() {
		reload();
		new MasterCommand(this);
		
		new DeathHandler(this);
		new RegionHandler(this);
		new PlayerArmorListener(this);
		new BossBlocksHandler(this);
		new EntitiesHandler(this);
		new PathfindersHandler(this);
		new ItemMechanicsHandler(this);
		new EntitySpawningHandler(this);
		
		new DoubleJumpHandler(this);
		new SantaHatHandler(this);
	}
	public static void reload() {
		instance.reloadConfig();
		FileConfiguration config = instance.getConfig();
		DataUtils.reload();
		DependencyUtils.reload();
		CustomEntityType.reload(instance);
		AbilityAttributes.reload();
		Rarity.reload();
		if (specialEvent != null)
			specialEvent.reload();
		for (String s : config.getConfigurationSection("abilities").getKeys(false)) {
			UIAbilityType type = UIAbilityType.getAbilityType("uc:"+s);
			if (type == null) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'abilities."+s+"' &cno such ability exists!"));
				continue;
			}
			if (config.contains("abilities."+s+".name"))
				type.setDisplayName(Utils.convertString(DataUtils.getConfigString("abilities."+s+".name")));
			if (config.contains("abilities."+s+".description"))
				type.setDescription(Utils.convertString(DataUtils.getConfigString("abilities."+s+".description")));
			if (config.contains("abilities."+s+".blacklist")) {
				if (config.contains("abilities."+s+".blacklist.blocks")) {
					Set<Material> types = new HashSet<>();
					for (String temp : DataUtils.getConfigStringList("abilities."+s+".blacklist.blocks")) {
						Material material = Material.matchMaterial(temp);
						if (material == null) {
							Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+temp+"' &cto ability &e"+s+" &cblock blacklist in config section &babilities."+s+".blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the abilities block blacklist!"));
							continue;
						}
						types.add(material);
					}
					AbilityAttributes.globalMaterialBlacklist.put(type, types);
				}
				if (config.contains("abilities."+s+".blacklist.entities")) {
					Set<EntityType> types = new HashSet<>();
					Set<Class<? extends CustomEntity<?>>> customTypes = new HashSet<>();
					for (String temp : DataUtils.getConfigStringList("abilities."+s+".blacklist.entities"))
						try {
							UIEntityManager customType = UIEntityManager.getEntityType(temp);
							if (customType != null) {
								customTypes.add(customType.getEntityClass());
								continue;
							}
							EntityType entityType = EntityType.valueOf(temp.toUpperCase());
							types.add(entityType);
						} catch (IllegalArgumentException e) {
							Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+temp+"' &cto ability &e"+s+" &centity blacklist in config section &babilities."+s+".blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the abilities entity blacklist!"));
						}
					AbilityAttributes.globalEntityBlacklist.put(type, types);
					AbilityAttributes.globalCustomEntityBlacklist.put(type, customTypes);
				}
			}
		}
		for (String s : config.getConfigurationSection("enchants").getKeys(false)) {
			UIEnchantment type = UIEnchantment.getEnchant("uc:"+s);
			if (type == null) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+"' &cno such enchant exists!"));
				continue;
			}
			if (type instanceof CustomEnchant) {
				CustomEnchant enchant = (CustomEnchant) type;
				enchant.abilities.clear();
				if (!config.contains("enchants."+s+".name")) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+".name' &cno name is assigned, please set the name field!"));
					continue;
				}
				enchant.setDisplayName(Utils.convertString(DataUtils.getConfigString("enchants."+s+".name")));
				if (config.contains("enchants."+s+".levels"))
					for (String level : config.getConfigurationSection("enchants."+s+".levels").getKeys(false)) {
						int numeric = 0;
						try {
							numeric = Integer.parseInt(level);
						} catch (NumberFormatException ex) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+".levels."+level+"' &cthe level &a'"+level+"' &cis not a valid integer!"));
							continue;
						}
						if (config.contains("enchants."+s+".levels."+level+".abilities"))
							for (String ability : config.getConfigurationSection("enchants."+s+".levels."+level+".abilities").getKeys(false)) {
								Pair<Ability, Queue<Ability.Action>> pair = assembleAbility(ability, "enchants."+s+".levels."+level+".abilities."+ability);
								if (pair != null) {
									if (enchant.abilities.containsKey(numeric))
										enchant.abilities.get(numeric).add(pair);
									else
										enchant.abilities.put(numeric, new ArrayDeque<>(Arrays.asList(pair)));
								}
							}
					}
			}
		}
		for (Entry<String, UIItemType> entry : UIItemType.getRegistry().entrySet()) {
			UIItemType id = entry.getValue();
			try {
				if (!BaseItem.class.isAssignableFrom(id.getInstance()))
					continue;
				String tempPath = "items."+((BaseItem) id.getInstance().getDeclaredConstructor(ItemStack.class).newInstance(new ItemStack(Material.AIR))).getConfigItemSection()+'.'+entry.getKey().replaceFirst("uc:", "");
				id.getAbilities().clear();
				if (config.contains(tempPath+".allow_vanilla_crafts"))
					id.setAllowVanillaCrafts(DataUtils.getConfigBoolean(tempPath+".allow_vanilla_crafts"));
				else
					id.setAllowVanillaCrafts(false);
				if (config.contains(tempPath+".allow_vanilla_enchanting"))
					id.setAllowVanillaEnchanting(DataUtils.getConfigBoolean(tempPath+".allow_vanilla_enchanting"));
				else
					id.setAllowVanillaEnchanting(true);
				if (config.contains(tempPath+".abilities"))
					for (String s : config.getConfigurationSection(tempPath+".abilities").getKeys(false)) {
						Pair<Ability, Queue<Ability.Action>> ability = assembleAbility(s, tempPath+".abilities."+s);
						if (ability != null)
							id.addAbility(ability.getSecond(), ability.getFirst());
					}
				BaseItem.setConfigPath(id.getInstance(), tempPath);
				if (config.contains(tempPath+".damage"))
					id.setDamage(DataUtils.getConfigDouble(tempPath+".damage"));
				if (config.contains(tempPath+".attackSpeed"))
					id.setAttackSpeed(DataUtils.getConfigDouble(tempPath+".attackSpeed"));
				if (config.contains(tempPath+".projectileDamage"))
					id.setProjectileDamage(DataUtils.getConfigDouble(tempPath+".projectileDamage"));
				if (config.contains(tempPath+".projectileDamageMultiplier"))
					id.setProjectileDamageMultiplier(DataUtils.getConfigDouble(tempPath+".projectileDamageMultiplier"));
				if (config.contains(tempPath+".durability"))
					id.setDurability(DataUtils.getConfigDouble(tempPath+".durability"));
				if (config.contains(tempPath+".lore"))
					id.setLore(DataUtils.getConfigStringList(tempPath+".lore").stream().map(s -> Utils.convertString(s)).collect(Collectors.toList()));
				if (config.contains(tempPath+".enchants"))
					for (String enchant : config.getConfigurationSection(tempPath+".enchants").getKeys(false)) {
						UIEnchantment type = UIEnchantment.getEnchant("uc:"+enchant);
						if (type == null) {
							Enchantment vanilla = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchant));
							if (vanilla != null) {
								id.getBuilder().accessMeta(e -> e.addEnchant(vanilla, DataUtils.getConfigInt(tempPath+".enchants."+enchant), true));
								continue;
							}
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants' &cno such enchant &a'"+enchant+"' &cexists!"));
							continue;
						}
						int level = DataUtils.getConfigInt(tempPath+".enchants."+enchant);
						if (level < 1) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants."+enchant+"' &clevel cannot be "+level+"!"));
							continue;
						}
						if (level > type.getMaxLevel()) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants."+enchant+"' &clevel cannot be "+level+"! The max level this enchant supports is "+type.getMaxLevel()+"!"));
							continue;
						}
						id.addEnchant(type, level);
					}
				id.setDisplayName(config.contains(tempPath+".name") ? Utils.convertString(DataUtils.getConfigString(tempPath+".name")) : null);

				id.refreshItemLore();
				
				id.updateRecipeResults();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private static Pair<Ability, Queue<Ability.Action>> assembleAbility(String name, String path) {
		UIAbilityType type = UIAbilityType.getAbilityType("uc:"+name);
		if (type == null) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cno such ability exists!"));
			return null;
		}
		FileConfiguration config = instance.getConfig();
		Ability ability = type.createNewInstance();
		if (config.contains(path+".cooldown"))
			ability.setCooldownTicks((int) (DataUtils.getConfigDouble(path+".cooldown") * 20.0));
		if (config.contains(path+".abilityDisplay")) {
			String display = DataUtils.getConfigString(path+".abilityDisplay");
			if (!config.contains("displays."+display))
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+path+".abilityDisplay' &ethere is no such ability display &a'"+display+"'"));
			else {
				ability.setDisplayName(DataUtils.getConfigString("displays."+display+".name"));
				ability.setDescription(DataUtils.getConfigString("displays."+display+".description"));
			}
		}
		if (ability instanceof AbilityAttributes attributes) {
			attributes.setConfigPath(path);
			if (config.contains(path+".cooldownMessages"))
				attributes.setSendCooldownMessages(DataUtils.getConfigBoolean(path+".cooldownMessages"));
			if (config.contains(path+".chance"))
				attributes.setChance(DataUtils.getConfigDouble(path+".chance"));
			if (config.contains(path+".volume"))
				attributes.setVolume((float) DataUtils.getConfigDouble(path+".volume"));
			if (config.contains(path+".target")) {
				Target target = Target.forName(DataUtils.getConfigString(path+".target"));
				if (target == null)
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+path+".target' &ethere is no such target type &a'"+DataUtils.getConfigString(path+".target")+"'"));
				else
					attributes.setTarget(target);
			}
			if (config.contains(path+".blacklist")) {
				if (config.contains(path+".blacklist.blocks")) {
					Set<Material> types = new HashSet<>();
					for (String s : DataUtils.getConfigStringList(path+".blacklist.blocks")) {
						Material material = Material.matchMaterial(s);
						if (material == null) {
							Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+s+"' &cto ability &e"+name+" &cblock blacklist in config section &b"+path+".blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the abilities block blacklist!"));
							continue;
						}
						types.add(material);
					}
					attributes.setImmuneMaterials(types);
				}
				if (config.contains(path+".blacklist.entities")) {
					Set<EntityType> types = new HashSet<>();
					Set<Class<? extends CustomEntity<?>>> customTypes = new HashSet<>();
					for (String s : DataUtils.getConfigStringList(path+".blacklist.entities"))
						try {
							UIEntityManager customType = UIEntityManager.getEntityType(s);
							if (customType != null) {
								customTypes.add(customType.getEntityClass());
								continue;
							}
							EntityType entityType = EntityType.valueOf(s.toUpperCase());
							types.add(entityType);
						} catch (IllegalArgumentException e) {
							Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+s+"' &cto ability &e"+name+" &centity blacklist in config section &b"+path+".blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the abilities entity blacklist!"));
						}
					attributes.setImmuneEntities(types);
					attributes.setImmuneCustomEntities(customTypes);
				}
			}
			attributes.initFields();
		}
		Queue<Action> actions = new ArrayDeque<>();
		if (!config.contains(path+".usage")) {
			if (ability instanceof AbilityAttributes && ((AbilityAttributes) ability).getCustomUsage() != null)
				actions.add(((AbilityAttributes) ability).getCustomUsage());
			else {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthere is no &d'usage' &cprovided! Ability is unbound."));
				return null;
			}
		} else {
			for (String stringAction : DataUtils.getConfigStringList(path+".usage")) {
				Action action = Ability.Action.forName(stringAction);
				if (action == null) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthe usage &d'"+stringAction+"' &cdoes not exist!"));
					continue;
				}
				actions.add(action);
			}
		}
		if (actions.isEmpty()) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthere are no valid usages provided! Ability is unbound."));
			return null;
		}
		return Pair.of(ability, actions);
	}
	public static JavaPlugin getInstance() {
		if (instance == null)
			instance = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("UltimateContent");
		return instance;
	}
	public static FixedMetadataValue getFixedMetadata() {
		if (fixedData == null)
			fixedData = new FixedMetadataValue(getInstance(), "protected");
		return fixedData;	
	}
	public static SpecialEvent getSpecialEvent() {
		return specialEvent;
	}
}