package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/**
 * Drives a breach undead miner up to a target sitting above it (a skybase / pillar): it builds a climbable spiral
 * staircase and physically walks/jumps up its own steps (via the entity controller — <b>no teleporting</b>, which looked
 * buggy and fought the mob's own pathing). Added at top priority when the miner enters breach mode; it suppresses the
 * normal player-chase ({@code setTarget(null)}) while it works so the miner doesn't oscillate between running at the
 * player and climbing. Once the build reaches the target's height it stops and the miner's normal bridge logic carries it
 * across to the platform.
 *
 * <p>Coordination so miners don't pile up and clobber one staircase:</p>
 * <ul>
 *   <li>{@link #staircaseTops} maps each active builder to the live top of its staircase. On {@link #start()} a miner that
 *       finds another builder's staircase within {@link #SHARE_RADIUS_SQ} becomes a <b>follower</b> — it climbs that
 *       staircase (behind the builder) instead of starting its own, so squads converge into a few shared stairways spread
 *       around the area rather than all building over the same spot.</li>
 *   <li>{@link #stairBlocks} holds every placed step; no miner will break a block in it while clearing headroom, so one
 *       miner can never destroy another's (or its own) staircase out from under the climbing horde.</li>
 * </ul>
 */
public class PathfinderBuildStaircase extends CustomPathfinder {

	private static final BlockFace[] SPIN = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final double SHARE_RADIUS_SQ = 49.0; // within 7 blocks of another builder's stairway -> share it
	private static final double OPERATE_RANGE_SQ = 3600.0; // 60 blocks; beyond this the miner approaches via normal AI first

	/** Builder UUID -> the live top (standing surface) of its staircase. Followers climb toward it; new miners share it. */
	public static final Map<UUID, Location> staircaseTops = new ConcurrentHashMap<>();
	/** Every still-standing breach step. Protected so no miner breaks another's (or its own) staircase clearing headroom. */
	public static final Set<Block> stairBlocks = ConcurrentHashMap.newKeySet();

	private final EntityController controller;
	private final LivingEntity target;
	private final Material material;
	private final List<Block> placed;

	private boolean builder;
	private UUID follow;
	private Location stand; // the standing surface (feet block) at the top of the staircase we're building/climbing
	private int stuck;

	public PathfinderBuildStaircase(@NotNull Mob m, @NotNull LivingEntity target, Material material, @NotNull List<Block> placed) {
		super(m);
		this.target = target;
		this.material = material;
		this.placed = placed;
		this.controller = BukkitBrain.getBrain(m).getController();
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING, PathfinderFlag.JUMPING };
	}
	@Override
	public boolean canStart() {
		return target != null && !target.isDead() && target.getWorld().equals(entity.getWorld())
				&& target.getEyeLocation().getY() - entity.getLocation().getY() > 2.0
				&& entity.getLocation().distanceSquared(target.getLocation()) < OPERATE_RANGE_SQ;
	}
	@Override
	public boolean canContinueToUse() {
		return target != null && !target.isDead() && target.getWorld().equals(entity.getWorld())
				&& target.getEyeLocation().getY() - entity.getLocation().getY() > 1.5
				&& entity.getLocation().distanceSquared(target.getLocation()) < OPERATE_RANGE_SQ;
	}
	@Override
	public void start() {
		entity.setCollidable(false); // so the climbing horde can't shove the builder off its steps
		entity.setTarget(null);
		Location me = entity.getLocation();
		follow = null;
		builder = false;
		for (Map.Entry<UUID, Location> e : staircaseTops.entrySet()) {
			if (e.getKey().equals(entity.getUniqueId()))
				continue;
			Location top = e.getValue();
			if (top != null && top.getWorld().equals(me.getWorld()) && horizSq(top, me) <= SHARE_RADIUS_SQ) {
				follow = e.getKey();
				break;
			}
		}
		if (follow == null) {
			builder = true;
			stand = footing(me);
			staircaseTops.put(entity.getUniqueId(), stand);
		}
	}
	@Override
	public void tick() {
		entity.setTarget(null);
		if (builder)
			buildTick();
		else
			followTick();
	}
	private void buildTick() {
		Location loc = entity.getLocation();
		if (stand == null || !stand.getWorld().equals(loc.getWorld()) || loc.getY() < stand.getY() - 3.0)
			stand = footing(loc);
		controller.lookAt(target.getEyeLocation());
		boolean onStand = horizSq(loc, stand) < 1.6 && Math.abs(loc.getY() - stand.getY()) < 1.3;
		if (onStand) {
			Block standBlock = stand.getBlock();
			BlockFace dir = SPIN[Math.floorMod(standBlock.getY(), SPIN.length)];
			Block step = standBlock.getRelative(dir);
			clear(step.getRelative(BlockFace.UP));
			clear(step.getRelative(BlockFace.UP, 2));
			if (step.isPassable() && !DependencyUtils.isBlockProtected(step)) {
				step.setType(material);
				placed.add(step);
				stairBlocks.add(step);
				SoundGroup sg = material.createBlockData().getSoundGroup();
				step.getWorld().playSound(BlockUtils.getCenterOfBlock(step), sg.getPlaceSound(), 1f, 1f);
				entity.swingOffHand();
			}
			stand = new Location(loc.getWorld(), step.getX() + 0.5, step.getY() + 1, step.getZ() + 0.5);
			staircaseTops.put(entity.getUniqueId(), stand);
			stuck = 0;
		} else {
			controller.moveTo(stand);
			if (loc.getY() < stand.getY() - 0.1 && entity.isOnGround())
				controller.jump();
			if (++stuck > 60) { // wandered/blocked too long -> re-anchor the spiral to where we actually are
				stand = footing(loc);
				stuck = 0;
			}
		}
	}
	private void followTick() {
		Location top = staircaseTops.get(follow);
		if (top == null || !top.getWorld().equals(entity.getWorld())) {
			// the builder we were following is gone -> take over and build from where we stand
			builder = true;
			stand = footing(entity.getLocation());
			staircaseTops.put(entity.getUniqueId(), stand);
			return;
		}
		controller.lookAt(top);
		controller.moveTo(top);
		if (entity.getLocation().getY() < top.getY() - 0.1 && entity.isOnGround())
			controller.jump();
	}
	/** Breaks a headroom obstacle, but never a protected block or another miner's staircase step. */
	private void clear(Block b) {
		if (b == null || b.isPassable() || stairBlocks.contains(b) || DependencyUtils.isBlockProtected(b))
			return;
		b.breakNaturally();
	}
	@Override
	public void stop() {
		staircaseTops.remove(entity.getUniqueId());
		if (entity.isValid())
			entity.setCollidable(true);
	}
	private static double horizSq(Location a, Location b) {
		double dx = a.getX() - b.getX(), dz = a.getZ() - b.getZ();
		return dx * dx + dz * dz;
	}
	private static Location footing(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5);
	}
}
