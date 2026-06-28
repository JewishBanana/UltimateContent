package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.github.jewishbanana.uiframework.events.AbilityTriggerEvent;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class RegionHandler implements Listener {
	
	public static Set<Block> fluidChangeBlocks = new HashSet<>();
	private final Map<UUID, Long> abilityBlockedMessageCooldown = new HashMap<>();

	public RegionHandler(UltimateContent plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAbilityTrigger(AbilityTriggerEvent event) {
		Entity activator = getAbilityActivator(event.getTriggeringEntity());
		Entity check = activator != null ? activator : event.getTriggeringEntity();
		if (DependencyUtils.canActivateAbilities(check))
			return;
		event.setCancelled(true);
		if (activator instanceof Player player)
			sendAbilityBlockedMessage(player);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBlockForm(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof FallingBlock && event.getEntity().hasMetadata("uc-fb"))
				event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onFlow(FluidLevelChangeEvent e) {
		if (fluidChangeBlocks.contains(e.getBlock()))
			e.setCancelled(true);
	}
	private Entity getAbilityActivator(Entity trigger) {
		if (trigger instanceof Projectile projectile) {
			ProjectileSource source = projectile.getShooter();
			if (source instanceof Entity entity)
				return entity;
		}
		return trigger;
	}
	private void sendAbilityBlockedMessage(Player player) {
		long now = System.currentTimeMillis();
		Long last = abilityBlockedMessageCooldown.get(player.getUniqueId());
		if (last != null && now - last < 1000)
			return;
		abilityBlockedMessageCooldown.put(player.getUniqueId(), now);
		player.sendMessage(Utils.convertString(DataUtils.getConfigString("language.abilities.regionActivationBlocked", "&cYou cannot activate abilities in this region!")));
	}
}
