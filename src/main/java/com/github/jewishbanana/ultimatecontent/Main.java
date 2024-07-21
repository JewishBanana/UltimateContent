package com.github.jewishbanana.ultimatecontent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.uiframework.UIFramework;
import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.Ability.Action;
import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes.Target;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomEnchant;
import com.github.jewishbanana.ultimatecontent.items.abilities.BlackRift;
import com.github.jewishbanana.ultimatecontent.items.abilities.Blinding;
import com.github.jewishbanana.ultimatecontent.items.abilities.CursedWinds;
import com.github.jewishbanana.ultimatecontent.items.abilities.DoubleJump;
import com.github.jewishbanana.ultimatecontent.items.abilities.JumpBoost;
import com.github.jewishbanana.ultimatecontent.items.abilities.PropulsionBlast;
import com.github.jewishbanana.ultimatecontent.items.abilities.RestoreHealth;
import com.github.jewishbanana.ultimatecontent.items.abilities.SaberParry;
import com.github.jewishbanana.ultimatecontent.items.abilities.SaberThrow;
import com.github.jewishbanana.ultimatecontent.items.abilities.SpawnPlatform;
import com.github.jewishbanana.ultimatecontent.items.abilities.StasisZone;
import com.github.jewishbanana.ultimatecontent.items.abilities.TeleportRay;
import com.github.jewishbanana.ultimatecontent.items.abilities.TidalWave;
import com.github.jewishbanana.ultimatecontent.items.abilities.YetiRoar;
import com.github.jewishbanana.ultimatecontent.items.books.AncientCurseBook;
import com.github.jewishbanana.ultimatecontent.items.easter.GoldenEgg;
import com.github.jewishbanana.ultimatecontent.items.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.items.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.items.enchants.YetisBlessing;
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
import com.github.jewishbanana.ultimatecontent.items.tools.AbyssalShield;
import com.github.jewishbanana.ultimatecontent.items.weapons.AncientBlade;
import com.github.jewishbanana.ultimatecontent.items.weapons.CallOfTheVoid;
import com.github.jewishbanana.ultimatecontent.items.weapons.GreenLightsaber;
import com.github.jewishbanana.ultimatecontent.items.weapons.StasisGun;
import com.github.jewishbanana.ultimatecontent.items.weapons.TritonsFang;
import com.github.jewishbanana.ultimatecontent.items.weapons.VoidsEdge;
import com.github.jewishbanana.ultimatecontent.listeners.ArmorListener;
import com.github.jewishbanana.ultimatecontent.listeners.DeathHandler;
import com.github.jewishbanana.ultimatecontent.listeners.DoubleJumpHandler;
import com.github.jewishbanana.ultimatecontent.listeners.RegionHandler;
import com.github.jewishbanana.ultimatecontent.utils.ConfigUpdater;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.mojang.datafixers.util.Pair;

public class Main extends JavaPlugin {

	private static Main instance;
	public static ConsoleCommandSender consoleSender;
	public FixedMetadataValue fixedData;
	
	private String UIFrameworkVersion = "2.2.2";
	
	public void onEnable() {
		instance = this;
		consoleSender = this.getServer().getConsoleSender();
		this.fixedData = new FixedMetadataValue(this, "protected");
		
		if (UIFramework.isVersionOrAbove(UIFrameworkVersion)) {
			consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR Cannot start plugin because UIFramework is out of date! Please update to at least &a"+UIFrameworkVersion+" &c(Current version installed is &b"+(getServer().getPluginManager().getPlugin("UIFramework").getDescription().getVersion())+"&c)."));
			this.setEnabled(false);
			return;
		}
		UIFramework.registerReloadRunnable(this, () -> reload(true, true, true));
		
		boolean firstSetup = false;
		if (!(new File(getDataFolder().getAbsolutePath(), "config.yml").exists()))
			firstSetup = true;
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		if (firstSetup)
			try {
				ConfigUpdater.update(this, getResource("config.yml"), new File(getDataFolder().getAbsolutePath(), "config.yml"), Arrays.asList(""));
				this.reloadConfig();
			} catch (IOException e) {
				e.printStackTrace();
				consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cUnable to initialize config! Please report the full error above to the discord."));
			}
		DependencyUtils.init(this);
		
		registerAbilities();
		registerEnchants();
		registerItems();
		init();
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
		
		reload(true, false, false);
	}
	private void registerEnchants() {
		AncientCurse.register();
		YetisBlessing.register();
		BunnyHop.register();
		
		reload(false, true, false);
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
//		DarkMageWand.register();
		TritonsFang.register();
		
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
		
		// Easter
		GoldenEgg.register();
		
		reload(false, false, true);
	}
	private void init() {
		reload(true, true, true);
		new DeathHandler(this);
		new RegionHandler(this);
		new ArmorListener(this);
		new DoubleJumpHandler(this);
	}
	public void onDisable() {
	}
	public static void reload(boolean abilities, boolean enchants, boolean items) {
		instance.reloadConfig();
		FileConfiguration config = instance.getConfig();
		DependencyUtils.reload();
		if (abilities)
			for (String s : config.getConfigurationSection("abilities").getKeys(false)) {
				AbilityType type = AbilityType.getAbilityType("ui:"+s);
				if (type == null) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'abilities."+s+"' &cno such ability exists!"));
					continue;
				}
				if (config.contains("abilities."+s+".name"))
					type.setDisplayName(Utils.convertString(DataUtils.getConfigString("abilities."+s+".name")));
				if (config.contains("abilities."+s+".description"))
					type.setDescription(Utils.convertString(DataUtils.getConfigString("abilities."+s+".description")));
			}
		if (enchants)
			for (String s : config.getConfigurationSection("enchants").getKeys(false)) {
				UIEnchantment type = UIEnchantment.getEnchant("ui:"+s);
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
		if (items)
			for (Entry<String, ItemType> entry : ItemType.getAllItems().entrySet()) {
				ItemType id = entry.getValue();
				try {
					if (!BaseItem.class.isAssignableFrom(id.getInstance()))
						continue;
					String tempPath = "items."+((BaseItem) id.getInstance().getDeclaredConstructor(ItemStack.class).newInstance(new ItemStack(Material.AIR))).getConfigItemSection()+'.'+entry.getKey().replaceFirst("ui:", "");
					id.getAbilities().clear();
					if (config.contains(tempPath+".allow_vanilla_crafts"))
						id.setAllowVanillaCrafts(DataUtils.getConfigBoolean(tempPath+".allow_vanilla_crafts"));
					else
						id.setAllowVanillaCrafts(false);
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
							UIEnchantment type = UIEnchantment.getEnchant("ui:"+enchant);
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}
	private static Pair<Ability, Queue<Ability.Action>> assembleAbility(String name, String path) {
		AbilityType type = AbilityType.getAbilityType("ui:"+name);
		if (type == null) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cno such ability exists!"));
			return null;
		}
		FileConfiguration config = instance.getConfig();
		Ability ability = type.createNewInstance();
		if (config.contains(path+".cooldown"))
			ability.setCooldownTicks((int) (DataUtils.getConfigDouble(path+".cooldown") * 20.0));
		if (ability instanceof AbilityAttributes) {
			AbilityAttributes attributes = (AbilityAttributes) ability;
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
			if (config.contains(path+".abilityDisplay")) {
				String display = DataUtils.getConfigString(path+".abilityDisplay");
				if (!config.contains("displays."+display))
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+path+".abilityDisplay' &ethere is no such ability display &a'"+display+"'"));
				else {
					attributes.setDisplayName(DataUtils.getConfigString("displays."+display+".name"));
					attributes.setDescription(DataUtils.getConfigString("displays."+display+".description"));
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
	public static Main getInstance() {
		return instance;
	}
}