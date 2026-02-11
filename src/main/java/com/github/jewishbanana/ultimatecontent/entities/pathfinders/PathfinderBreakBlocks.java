package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderBreakBlocks extends CustomPathfinder {
	
	private static final Set<Material> bannedBlocks;
	static {
		bannedBlocks = EnumSet.of(Material.AIR, Material.BEDROCK, Material.END_PORTAL_FRAME);
	}
	
	private final List<Block> blocks;
	private final float maxDistanceSquared;
	private final double damageMultiplier;
	private final double speedMod;
	private final float distanceToBreakSquared;
	private final float volume;
	private final EntityController controller;
	
	private Block current;
	private Location goal;
	private int ticks;
	private boolean breaking;
	
	public Consumer<Block> endAction;

	public PathfinderBreakBlocks(@NotNull Mob m, @NotNull List<Block> blocks, float maxDistance, double damageMultiplier, double speedMod, float distanceToBreak, float volume) {
		super(m);
		this.blocks = blocks;
		this.maxDistanceSquared = maxDistance * maxDistance;
		this.damageMultiplier = damageMultiplier;
		this.speedMod = speedMod;
		this.distanceToBreakSquared = distanceToBreak * distanceToBreak;
		this.volume = volume;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	public PathfinderBreakBlocks(@NotNull Mob m, @NotNull List<Block> blocks, float maxDistance) {
		this(m, blocks, maxDistance, 1.0, 1.0, 1f, 1f);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		if (blocks.isEmpty())
			return false;
		Location entityLoc = entity.getLocation();
		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block temp = it.next();
			if (bannedBlocks.contains(temp.getType())) {
				it.remove();
				continue;
			}
			if (!Utils.isLocationsWithinDistance(entityLoc, temp.getLocation(), maxDistanceSquared))
				continue;
			current = temp;
			goal = temp.getLocation().add(.5, .5, .5);
			return true;
		}
		return false;
	}
	@Override
	public void start() {
		ticks = 100;
		breaking = false;
		controller.moveTo(goal, speedMod);
	}
	@Override
	public void tick() {
		ticks--;
		if (breaking)
			return;
		if (Utils.isLocationsWithinDistance(entity.getLocation(), goal, distanceToBreakSquared)) {
			final Location startPos = entity.getLocation();
			final World world = startPos.getWorld();
			final SoundGroup soundGroup = current.getType().createBlockData().getSoundGroup();
			final ItemStack tool = entity.getEquipment().getItemInMainHand();
			final Collection<Player> players = world.getPlayers();
			final Location blockLoc = current.getLocation();
			new BukkitRunnable() {
				final double damage = BlockUtils.getDamageOnBlock(current, tool) * damageMultiplier;
				int blockTicks = (int) Math.ceil(1.0 / damage);
				float damageTrack;

				@Override
				public void run() {
					if (current.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(world) || entity.getLocation().distanceSquared(startPos) > distanceToBreakSquared) {
						this.cancel();
						breaking = false;
						for (Player player : players)
							player.sendBlockDamage(blockLoc, 0);
						return;
					}
					if (blockTicks-- <= 0) {
						current.breakNaturally(tool);
						world.playSound(goal, soundGroup.getBreakSound(), volume, 1f);
						blocks.remove(current);
						this.cancel();
						breaking = false;
						entity.swingMainHand();
						for (Player player : players)
							player.sendBlockDamage(blockLoc, 0);
						if (endAction != null)
							endAction.accept(current);
						return;
					}
					damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
					for (Player player : players)
						player.sendBlockDamage(blockLoc, damageTrack);
					if (blockTicks % 5 == 0)
						world.playSound(goal, soundGroup.getHitSound(), volume, 1f);
				}
			}.runTaskTimer(Main.getInstance(), 0, 1);
			world.playSound(goal, soundGroup.getHitSound(), volume, 1f);
			breaking = true;
			return;
		}
		controller.moveTo(goal, speedMod);
	}
	@Override
	public boolean canContinueToUse() {
		if (ticks == 0 || current.getType() == Material.AIR)
			return false;
		return true;
	}
}