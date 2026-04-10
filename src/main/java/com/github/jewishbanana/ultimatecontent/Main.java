package com.github.jewishbanana.ultimatecontent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import com.github.jewishbanana.ultimatecontent.AbilityAttributes.AbilityTypeBlacklists;
import com.github.jewishbanana.ultimatecontent.commands.MasterCommand;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.BossSpawnItem;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
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
	
	/**
	 * TODO:
	 * - make dark mage class
	 * - soul reaper scythe
	 * - infested worm
	 * - halloween entities and event
	 * - blood pact items and weapons
	 * - hook into dd achievements
	 * - fix saber throw
	 * - implement void tear
	 */
	
	public static ConsoleCommandSender consoleSender;

	private static JavaPlugin instance;
	private static FixedMetadataValue fixedData;
	private static SpecialEvent specialEvent;
	
	private static final String UIFrameworkVersion = "3.1.1";
	
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
		DataUtils.updateConfigChanges();
		
		DependencyUtils.init(this);
		CustomHead.init(this);
		
		BaseItem.createCustomCategories();
		UCRegistry.registerAbilities();
		UCRegistry.registerEnchants();
		UCRegistry.registerItems();
		
		CustomEntityType.initDefaults();
		UCRegistry.registerEntities();
		
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
				consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'abilities."+s+"' &cno such ability exists!"));
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
							consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+temp+"' &cto ability &e"+s+" &cblock blacklist in config section &babilities."+s+".blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the abilities block blacklist!"));
							continue;
						}
						types.add(material);
					}
					if (!types.isEmpty())
						AbilityAttributes.globalBlacklists.compute(type, (key, value) -> {
							AbilityTypeBlacklists blacklists = value == null ? new AbilityTypeBlacklists() : value;
							blacklists.materials = EnumSet.copyOf(types);
							return blacklists;
						});
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
							consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+temp+"' &cto ability &e"+s+" &centity blacklist in config section &babilities."+s+".blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the abilities entity blacklist!"));
						}
					if (!types.isEmpty() || !customTypes.isEmpty())
						AbilityAttributes.globalBlacklists.compute(type, (key, value) -> {
							AbilityTypeBlacklists blacklists = value == null ? new AbilityTypeBlacklists() : value;
							if (!types.isEmpty())
								blacklists.entityTypes = EnumSet.copyOf(types);
							if (!customTypes.isEmpty())
								blacklists.customEntityTypes = customTypes;
							return blacklists;
						});
				}
			}
		}
		for (String s : config.getConfigurationSection("enchants").getKeys(false)) {
			UIEnchantment type = UIEnchantment.getEnchant("uc:"+s);
			if (type == null) {
				consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+"' &cno such enchant exists!"));
				continue;
			}
			if (type instanceof CustomEnchant) {
				CustomEnchant enchant = (CustomEnchant) type;
				enchant.abilities.clear();
				if (!config.contains("enchants."+s+".name")) {
					consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+".name' &cno name is assigned, please set the name field!"));
					continue;
				}
				enchant.setDisplayName(Utils.convertString(DataUtils.getConfigString("enchants."+s+".name")));
				if (config.contains("enchants."+s+".levels"))
					for (String level : config.getConfigurationSection("enchants."+s+".levels").getKeys(false)) {
						int numeric = 0;
						try {
							numeric = Integer.parseInt(level);
						} catch (NumberFormatException ex) {
							consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'enchants."+s+".levels."+level+"' &cthe level &a'"+level+"' &cis not a valid integer!"));
							continue;
						}
						if (config.contains("enchants."+s+".levels."+level+".abilities"))
							for (String ability : config.getConfigurationSection("enchants."+s+".levels."+level+".abilities").getKeys(false)) {
								Pair<Ability, List<Ability.Action>> pair = assembleAbility(ability, "enchants."+s+".levels."+level+".abilities."+ability);
								if (pair != null) {
									if (enchant.abilities.containsKey(numeric))
										enchant.abilities.get(numeric).add(pair);
									else
										enchant.abilities.put(numeric, new ArrayList<>(Arrays.asList(pair)));
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
						Pair<Ability, List<Ability.Action>> ability = assembleAbility(s, tempPath+".abilities."+s);
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
							consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants' &cno such enchant &a'"+enchant+"' &cexists!"));
							continue;
						}
						int level = DataUtils.getConfigInt(tempPath+".enchants."+enchant);
						if (level < 1) {
							consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants."+enchant+"' &clevel cannot be "+level+"!"));
							continue;
						}
						if (level > type.getMaxLevel()) {
							consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading enchant from config path &e'"+tempPath+".enchants."+enchant+"' &clevel cannot be "+level+"! The max level this enchant supports is "+type.getMaxLevel()+"!"));
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
	private static Pair<Ability, List<Ability.Action>> assembleAbility(String name, String path) {
		UIAbilityType type = UIAbilityType.getAbilityType("uc:"+name);
		if (type == null) {
			consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cno such ability exists!"));
			return null;
		}
		FileConfiguration config = instance.getConfig();
		Ability ability = type.createNewInstance();
		if (config.contains(path+".cooldown", true))
			ability.setCooldownTicks((int) (DataUtils.getConfigDouble(path+".cooldown") * 20.0));
		if (config.contains(path+".abilityDisplay", true)) {
			String display = DataUtils.getConfigString(path+".abilityDisplay");
			if (!config.contains("displays."+display))
				consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+path+".abilityDisplay' &ethere is no such ability display &a'"+display+"'"));
			else {
				ability.setDisplayName(DataUtils.getConfigString("displays."+display+".name"));
				ability.setDescription(DataUtils.getConfigString("displays."+display+".description"));
			}
		}
		if (ability instanceof AbilityAttributes attributes) {
			attributes.setDefaultConfigPath("abilities."+name);
			attributes.setConfigPath(path);
//			if (config.contains(path+".cooldownMessages"))
//				attributes.setSendCooldownMessages(DataUtils.getConfigBoolean(path+".cooldownMessages"));
//			if (config.contains(path+".chance"))
//				attributes.setChance(DataUtils.getConfigDouble(path+".chance"));
//			if (config.contains(path+".volume"))
//				attributes.setVolume((float) DataUtils.getConfigDouble(path+".volume"));
//			if (config.contains(path+".target")) {
//				Target target = Target.forName(DataUtils.getConfigString(path+".target"));
//				if (target == null)
//					consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+path+".target' &ethere is no such target type &a'"+DataUtils.getConfigString(path+".target")+"'"));
//				else
//					attributes.setTarget(target);
//			}
//			if (config.contains(path+".blacklist")) {
//				if (config.contains(path+".blacklist.blocks")) {
//					Set<Material> types = new HashSet<>();
//					for (String s : DataUtils.getConfigStringList(path+".blacklist.blocks")) {
//						Material material = Material.matchMaterial(s);
//						if (material == null) {
//							consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+s+"' &cto ability &e"+name+" &cblock blacklist in config section &b"+path+".blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the abilities block blacklist!"));
//							continue;
//						}
//						types.add(material);
//					}
//					attributes.setImmuneMaterials(types);
//				}
//				if (config.contains(path+".blacklist.entities")) {
//					Set<EntityType> types = new HashSet<>();
//					Set<Class<? extends CustomEntity<?>>> customTypes = new HashSet<>();
//					for (String s : DataUtils.getConfigStringList(path+".blacklist.entities"))
//						try {
//							UIEntityManager customType = UIEntityManager.getEntityType(s);
//							if (customType != null) {
//								customTypes.add(customType.getEntityClass());
//								continue;
//							}
//							EntityType entityType = EntityType.valueOf(s.toUpperCase());
//							types.add(entityType);
//						} catch (IllegalArgumentException e) {
//							consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+s+"' &cto ability &e"+name+" &centity blacklist in config section &b"+path+".blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the abilities entity blacklist!"));
//						}
//					attributes.setImmuneEntities(types);
//					attributes.setImmuneCustomEntities(customTypes);
//				}
//			}
//			attributes.initFields();
		}
		ability.deserialize(config.getConfigurationSection(path).getValues(false));
		List<Action> actions = new ArrayList<>();
		if (!config.contains(path+".usage", true)) {
			if (ability instanceof AbilityAttributes attributes && attributes.getCustomUsage() != null)
				actions.add(attributes.getCustomUsage());
			else {
				consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthere is no &d'usage' &cprovided! Ability is unbound."));
				return null;
			}
		} else {
			for (String stringAction : DataUtils.getConfigStringList(path+".usage")) {
				Action action = Ability.Action.forName(stringAction);
				if (action == null) {
					consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthe usage &d'"+stringAction+"' &cdoes not exist!"));
					continue;
				}
				actions.add(action);
			}
		}
		if (actions.isEmpty()) {
			consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR while reading ability from config path &e'"+path+"' &cthere are no valid usages provided! Ability is unbound."));
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