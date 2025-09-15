package com.github.jewishbanana.ultimatecontent.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
	
	public static com.github.jewishbanana.deadlydisasters.Main DDHook;
	private static boolean isDDPro;
	
	private static boolean worldGuard;
	private static boolean kingdomsAPI;
	
	private static com.palmergames.bukkit.towny.TownyAPI townyapi;
	private static me.ryanhamshire.GriefPrevention.DataStore grief;
	private static me.angeschossen.lands.api.LandsIntegration landsclaims;
	
	private static boolean affectEntities;
	private static boolean damageBlocks;
	
	private static Set<Material> blacklistedMaterials;
	private static Set<EntityType> blacklistedEntities;
	private static Set<Class<? extends CustomEntity<?>>> blacklistedCustomEntities;

	public static void init(Main plugin) {
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				PluginManager manager = plugin.getServer().getPluginManager();
				if (manager.isPluginEnabled("DeadlyDisasters")) {
					DDHook = com.github.jewishbanana.deadlydisasters.Main.getInstance();
					isDDPro = DDHook.isPluginPro();
				}
				worldGuard = manager.isPluginEnabled("WorldGuard");
				kingdomsAPI = manager.isPluginEnabled("Kingdoms");
				if (manager.isPluginEnabled("Towny"))
					townyapi = com.palmergames.bukkit.towny.TownyAPI.getInstance();
				if (manager.isPluginEnabled("GriefPrevention"))
					grief = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
				if (manager.isPluginEnabled("Lands"))
					landsclaims = me.angeschossen.lands.api.LandsIntegration.of(plugin);
			}
		}, 1);
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
	private static boolean isZoneProtected(Location loc) {
		return (((worldGuard && isWGRegion(loc)) || (townyapi != null && townyapi.getTownBlock(loc) != null)
				|| (grief != null && grief.getClaimAt(loc, true, null) != null) || (landsclaims != null && landsclaims.getArea(loc) != null) || (kingdomsAPI && org.kingdoms.constants.land.Land.getLand(loc) != null)));
	}
	public static boolean isLocationProtected(Location loc) {
		return isZoneProtected(loc);
	}
	public static boolean isEntityProtected(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		if (custom != null && blacklistedCustomEntities.contains(custom.getClass()))
			return true;
		return blacklistedEntities.contains(entity.getType()) || (!affectEntities && isZoneProtected(entity.getLocation()));
	}
	public static boolean isBlockProtected(Block block) {
		return blacklistedMaterials.contains(block.getType()) || (!damageBlocks && isZoneProtected(block.getLocation()));
	}
	private static boolean isWGRegion(Location location) {
		com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
		com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
		com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
		com.sk89q.worldguard.protection.ApplicableRegionSet set = query.getApplicableRegions(loc);
		return set.size() != 0;
	}
}
