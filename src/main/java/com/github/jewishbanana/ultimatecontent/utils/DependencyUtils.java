package com.github.jewishbanana.ultimatecontent.utils;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;

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

	public static void init(Main plugin) {
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				PluginManager manager = plugin.getServer().getPluginManager();
				if (manager.isPluginEnabled("DeadlyDisasters")) {
					DDHook = com.github.jewishbanana.deadlydisasters.Main.getInstance();
					isDDPro = DDHook.isPro;
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
	}
	public static void awardAchievementProgress(UUID uuid, String achievement, int amount, int tier) {
		if (isDDPro)
			DDHook.achievementsHandler.awardProgress(uuid, achievement, amount, tier);
	}
	public static boolean isZoneProtected(Location loc) {
		return (((worldGuard && isWGRegion(loc)) || (townyapi != null && townyapi.getTownBlock(loc) != null)
				|| (grief != null && grief.getClaimAt(loc, true, null) != null) || (landsclaims != null && landsclaims.getArea(loc) != null) || (kingdomsAPI && org.kingdoms.constants.land.Land.getLand(loc) != null)));
	}
	public static boolean isLocationProtected(Location loc) {
		return isZoneProtected(loc);
	}
	public static boolean isEntityProtected(Entity entity) {
		return !affectEntities && isZoneProtected(entity.getLocation());
	}
	public static boolean isBlockProtected(Block block) {
		return !damageBlocks && isZoneProtected(block.getLocation());
	}
	private static boolean isWGRegion(Location location) {
		com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
		com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
		com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
		com.sk89q.worldguard.protection.ApplicableRegionSet set = query.getApplicableRegions(loc);
		return set.size() != 0;
	}
}
