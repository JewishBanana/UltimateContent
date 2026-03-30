package com.github.jewishbanana.ultimatecontent.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;

public class DependencyUtils {
	
//	public static com.github.jewishbanana.deadlydisasters.Main DDHook;
	private static boolean isDDPro;
	
	private static Predicate<Location> regionCheck;
	
	private static boolean affectEntities;
	private static boolean damageBlocks;
	
	private static Set<Material> blacklistedMaterials;
	private static Set<EntityType> blacklistedEntities;
	private static Set<Class<? extends CustomEntity<?>>> blacklistedCustomEntities;

	public static void init(Main plugin) {
		PluginManager pm = plugin.getServer().getPluginManager();
		if (pm.isPluginEnabled("DeadlyDisasters")) {
//			DDHook = com.github.jewishbanana.deadlydisasters.Main.getInstance();
//			isDDPro = DDHook.isPluginPro();
		}
		Predicate<Location> check = null;
		try {
			if (pm.isPluginEnabled("WorldGuard")) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.world_guard")) {
					check = (check == null) ? loc -> isWGRegion(loc) : check.and(loc -> isWGRegion(loc));
					plugin.getLogger().info("Successfully hooked into World Guard");
				} else
					plugin.getLogger().info("World Guard was detected, but region protection for this plugin is disabled in the config.yml file. World Guard regions will NOT be protected!");
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eWorld Guard &cregions from this plugin will NOT be protected!");
		}
		try {
			if (pm.isPluginEnabled("Towny")) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.towny")) {
					com.palmergames.bukkit.towny.TownyAPI townyHook = com.palmergames.bukkit.towny.TownyAPI.getInstance();
				    check = (check == null) ? 
				            loc -> townyHook.getTownBlock(loc) != null : 
				            check.and(loc -> townyHook.getTownBlock(loc) != null);
					plugin.getLogger().info("Successfully hooked into Towny");
				} else
					plugin.getLogger().info("Towny was detected, but region protection for this plugin is disabled in the config.yml file. Towny regions will NOT be protected!");
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eTowny &cregions from this plugin will NOT be protected!");
		}
		try {
			if (pm.isPluginEnabled("GriefPrevention")) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.grief_prevention")) {
					me.ryanhamshire.GriefPrevention.DataStore api = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
					check = (check == null) ? 
				            loc -> api.getClaimAt(loc, true, null) != null : 
				            check.and(loc -> api.getClaimAt(loc, true, null) != null);
					plugin.getLogger().info("Successfully hooked into Grief Prevention");
				} else
					plugin.getLogger().info("Grief Prevention was detected, but region protection for this plugin is disabled in the config.yml file. Grief Prevention regions will NOT be protected!");
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eGrief Prevention &cregions from this plugin will NOT be protected!");
		}
		try {
			if (pm.isPluginEnabled("Lands")) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.lands")) {
					me.angeschossen.lands.api.LandsIntegration api = me.angeschossen.lands.api.LandsIntegration.of(plugin);
				    check = (check == null) ? 
				            loc -> api.getArea(loc) != null : 
				            check.and(loc -> api.getArea(loc) != null);
					plugin.getLogger().info("Successfully hooked into Lands");
				} else
					plugin.getLogger().info("Lands was detected, but region protection for this plugin is disabled in the config.yml file. Lands regions will NOT be protected!");
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eLands &cregions from this plugin will NOT be protected!");
		}
		try {
			if (pm.isPluginEnabled("Kingdoms")) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.kingdoms")) {
					check = (check == null) ? 
				            loc -> org.kingdoms.constants.land.Land.getLand(loc) != null : 
				            check.and(loc -> org.kingdoms.constants.land.Land.getLand(loc) != null);
					plugin.getLogger().info("Successfully hooked into Kingdoms");
				} else
					plugin.getLogger().info("Kingdoms was detected, but region protection for this plugin is disabled in the config.yml file. Kingdoms regions will NOT be protected!");
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eKingdoms &cregions from this plugin will NOT be protected!");
		}
		regionCheck = (check == null) ? loc -> false : check;
	}
	public static void reload() {
		affectEntities = DataUtils.getConfigBoolean("general.protected_regions.affect_entities");
		damageBlocks = DataUtils.getConfigBoolean("general.protected_regions.damage_blocks");
		
		blacklistedMaterials = new HashSet<>();
		for (String s : DataUtils.getConfigStringList("general.blacklist.blocks")) {
			Material material = Material.matchMaterial(s);
			if (material == null) {
				Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+s+"' &cto global blacklist in config section &bgeneral.blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the global blocks blacklist!"));
				continue;
			}
			blacklistedMaterials.add(material);
		}
		blacklistedEntities = new HashSet<>();
		blacklistedCustomEntities = new HashSet<>();
		for (String s : DataUtils.getConfigStringList("general.blacklist.entities")) {
			try {
				UIEntityManager customType = UIEntityManager.getEntityType(s);
				if (customType != null) {
					blacklistedCustomEntities.add(customType.getEntityClass());
					continue;
				}
				EntityType type = EntityType.valueOf(s.toUpperCase());
				blacklistedEntities.add(type);
			} catch (IllegalArgumentException e) {
				Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+s+"' &cto global blacklist in config section &bgeneral.blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the global entities blacklist!"));
			}
		}
	}
	public static void awardAchievementProgress(UUID uuid, String achievement, int amount, int tier) {
//		if (isDDPro)
//			DDHook.achievementsHandler.awardProgress(uuid, achievement, amount, tier);
	}
	private static boolean isWGRegion(Location location) {
		com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
		com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
		com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
		com.sk89q.worldguard.protection.ApplicableRegionSet set = query.getApplicableRegions(loc);
		return set.size() != 0;
	}
	public static boolean isLocationProtected(Location loc) {
		return regionCheck.test(loc);
	}
	public static boolean isEntityProtected(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		if (custom != null && blacklistedCustomEntities.contains(custom.getClass()))
			return true;
		return blacklistedEntities.contains(entity.getType()) || (!affectEntities && isLocationProtected(entity.getLocation()));
	}
	public static boolean isBlockProtected(Block block) {
		return blacklistedMaterials.contains(block.getType()) || (!damageBlocks && isLocationProtected(block.getLocation()));
	}
}
