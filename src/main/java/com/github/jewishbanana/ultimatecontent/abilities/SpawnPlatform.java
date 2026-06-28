package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class SpawnPlatform extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:spawn_platform";
	private static final List<SpawnPlatform> list = new ArrayList<>();
	
	private double range;
	private double particleMultiplier;
	
	private Map<Block, BlockState> blocks;

	public SpawnPlatform(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		entity.setFallDistance(0);
		activate(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(.5, .5, .5), base);
	}
	@Override
	public void interacted(PlayerInteractEvent event, GenericItem base) {
		Player player = event.getPlayer();
		if (!canActivateInRegion(player))
			return;
		// If on cooldown, surface UIFramework's cooldown message first (the silent placement checks below would otherwise
		// swallow it). This call only sends the message while on cooldown - it commits nothing.
		if (getType().isEntityOnCooldown(player.getUniqueId())) {
			use(player, doesSendCooldownMessages());
			return;
		}
		if (!shouldActivate())
			return;
		// Only deploy when actually airborne over open, non-liquid space and a platform could form here, so the cooldown
		// and item are only spent on a real activation.
		if (!isOverOpenAir(player) || !canPlaceBelow(player))
			return;
		if (use(player, doesSendCooldownMessages()))
			internalActivation(player, event, base, player);
	}
	/**
	 * Whether the entity is in the air over open space - the block directly below it is passable and not a liquid.
	 */
	private boolean isOverOpenAir(Entity entity) {
		Block below = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
		return below.isPassable() && !below.isLiquid();
	}
	/**
	 * Whether at least one block of a platform could be placed below the entity - mirrors the placement scan in
	 * {@link #activate(Location, GenericItem)} so the cooldown/consume is only spent when a platform will actually form.
	 */
	private boolean canPlaceBelow(Entity entity) {
		Location loc = entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(.5, .5, .5);
		World world = loc.getWorld();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		for (int x=(int) -range; x <= Math.ceil(range); x++)
			for (int z=(int) -range; z <= Math.ceil(range); z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				if (block.distance(position) > range)
					continue;
				Block b = world.getBlockAt(position.toLocation(world));
				if (b.isPassable() && canBlockBeDamaged(b))
					return true;
			}
		return false;
	}
	public void activate(Location loc, GenericItem base) {
		SpawnPlatform instance = UIAbilityType.createAbilityInstance(this.getClass());
		instance.blocks = new HashMap<>();
		World world = loc.getWorld();
		BlockData blackData = Material.BLACK_STAINED_GLASS.createBlockData();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		final int particleCount = (int) Math.ceil(3.0 * particleMultiplier);
		for (int x=(int) -range; x <= Math.ceil(range); x++)
			for (int z=(int) -range; z <= Math.ceil(range); z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				if (block.distance(position) > range)
					continue;
				Block b = world.getBlockAt(position.toLocation(world));
				if (b.isPassable() && canBlockBeDamaged(b)) {
					instance.blocks.put(b, b.getState());
					b.setType(Material.BLACK_STAINED_GLASS);
					if (particleCount > 0)
						world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5, 0, .5), (int) particleCount, .5, .1, .5, 1, blackData);
				}
			}
		if (instance.blocks.isEmpty())
			return;
		playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, 1.75f);
		list.add(instance);
		if (instance.blocks.containsKey(loc.getBlock()))
			loc.getBlock().setType(Material.SEA_LANTERN);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, 1.25f);
			instance.blocks.keySet().forEach(k -> {
				if (k.getType() == Material.BLACK_STAINED_GLASS)
					k.setType(Material.ORANGE_STAINED_GLASS);
				if (particleCount > 0)
					world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, blackData);
			});
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, .75f);
				BlockData orangeData = Material.ORANGE_STAINED_GLASS.createBlockData();
				instance.blocks.keySet().forEach(k -> {
					if (k.getType() == Material.ORANGE_STAINED_GLASS)
						k.setType(Material.RED_STAINED_GLASS);
					if (particleCount > 0)
						world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, orangeData);
				});
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
					playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, .5f);
					BlockData redData = Material.RED_STAINED_GLASS.createBlockData();
					instance.blocks.forEach((k, v) -> {
						if (k.getType() == Material.RED_STAINED_GLASS || k.getType() == Material.SEA_LANTERN)
							v.update(true);
						if (particleCount > 0)
							world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, redData);
						list.remove(instance);
					});
				}, 100);
			}, 100);
		}, 200);
	}
	@Override
	public int getMobProxyInterval() {
		return 2; // poll quickly so a fall is caught in time, regardless of the configured cooldown
	}
	@Override
	public void onMobHoldTick(Mob mob, GenericItem item) {
		if (mob.isOnGround() || mob.getFallDistance() < 6)
			return;
		// Look further down the faster it is falling so a quick fall isn't skipped over between polls.
		int checkDist = Math.max(5, (int) Math.ceil(Math.abs(mob.getVelocity().getY()) * getMobProxyInterval()) + 3);
		Block at = mob.getLocation().getBlock();
		boolean groundNear = false;
		for (int i=1; i <= checkDist; i++)
			if (!at.getRelative(BlockFace.DOWN, i).isPassable()) {
				groundNear = true;
				break;
			}
		if (!groundNear)
			return;
		// Must be falling into open, non-liquid space (don't deploy over water/lava) and have room for a platform.
		if (!isOverOpenAir(mob) || !canPlaceBelow(mob))
			return;
		mobActivate(mob, item);
	}
	private void sweep() {
		blocks.forEach((k, v) -> {
			Material type = k.getType();
			if (type == Material.BLACK_STAINED_GLASS || type == Material.ORANGE_STAINED_GLASS || type == Material.RED_STAINED_GLASS || type == Material.SEA_LANTERN)
				v.update(true);
		});
	}
	public void clean() {
		list.forEach(e -> e.sweep());
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, SpawnPlatform.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = registerSerializedDoubleField("range", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
}
