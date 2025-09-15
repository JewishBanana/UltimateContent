package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderBreakBlocks extends CustomPathfinder {
	
	private static Set<Material> bannedBlocks = Set.of(Material.AIR, Material.BEDROCK, Material.END_PORTAL_FRAME);
	
	private Queue<Block> blocks;
	private double maxDistance;
	private double damageMultiplier;
	private double speedMod;
	private double distanceToBreak;
	private float volume;
	private Block current;
	private Location goal;
	private int ticks;
	private boolean breaking;
	
	public Consumer<Block> endAction;

	public PathfinderBreakBlocks(@NotNull Mob m, @NotNull Queue<Block> queue, double maxDistance, double damageMultiplier, double speedMod, double distanceToBreak, float volume) {
		super(m);
		this.blocks = queue;
		this.maxDistance = maxDistance * maxDistance;
		this.damageMultiplier = damageMultiplier;
		this.speedMod = speedMod;
		this.distanceToBreak = distanceToBreak * distanceToBreak;
		this.volume = 1f * volume;
	}
	public PathfinderBreakBlocks(@NotNull Mob m, @NotNull Queue<Block> queue, double maxDistance) {
		this(m, queue, maxDistance, 1.0, 1.0, 1.0, 1f);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		if (blocks.isEmpty())
			return false;
		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block temp = it.next();
			if (bannedBlocks.contains(temp.getType())) {
				it.remove();
				continue;
			}
			if (!Utils.isLocationsWithinDistance(entity.getLocation(), temp.getLocation(), maxDistance))
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
		BukkitBrain.getBrain(entity).getController().moveTo(goal, speedMod);
	}
	@Override
	public void tick() {
		ticks--;
		if (breaking)
			return;
		if (Utils.isLocationsWithinDistance(entity.getLocation(), goal, distanceToBreak)) {
			final Location startPos = entity.getLocation();
			final SoundGroup soundGroup = current.getType().createBlockData().getSoundGroup();
			new BukkitRunnable() {
				final double damage = Utils.getDamageOnBlock(current, entity.getEquipment().getItemInMainHand()) * damageMultiplier;
				int blockTicks = (int) Math.ceil(1.0 / damage);
				float damageTrack;

				@Override
				public void run() {
					if (current.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(startPos.getWorld()) || entity.getLocation().distanceSquared(startPos) > distanceToBreak) {
						this.cancel();
						breaking = false;
						for (Player player : startPos.getWorld().getPlayers())
							player.sendBlockDamage(current.getLocation(), 0);
						return;
					}
					if (blockTicks-- <= 0) {
						current.breakNaturally(entity.getEquipment().getItemInMainHand());
						goal.getWorld().playSound(goal, soundGroup.getBreakSound(), volume, 1);
						blocks.remove(current);
						this.cancel();
						breaking = false;
						entity.swingMainHand();
						for (Player player : startPos.getWorld().getPlayers())
							player.sendBlockDamage(current.getLocation(), 0);
						if (endAction != null)
							endAction.accept(current);
						return;
					}
					damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
					for (Player player : startPos.getWorld().getPlayers())
						player.sendBlockDamage(current.getLocation(), damageTrack);
					if (blockTicks % 5 == 0)
						goal.getWorld().playSound(goal, soundGroup.getHitSound(), volume, 1);
				}
			}.runTaskTimer(Main.getInstance(), 0, 1);
			goal.getWorld().playSound(goal, soundGroup.getHitSound(), volume, 1);
			breaking = true;
			return;
		}
		BukkitBrain.getBrain(entity).getController().moveTo(goal, speedMod);
	}
	@Override
	public boolean canContinueToUse() {
		if (ticks == 0 || current.getType() == Material.AIR)
			return false;
		return true;
	}
}
