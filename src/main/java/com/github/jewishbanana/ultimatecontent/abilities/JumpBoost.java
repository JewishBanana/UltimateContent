package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;

public class JumpBoost extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:jump_boost";
	
	private double jumpHeight;
	private boolean slowFall;
	private boolean noFall;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;

	public JumpBoost(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location entityLoc = entity.getLocation();
		World world = entityLoc.getWorld();
		if (particleMultiplier > 0)
			world.spawnParticle(Particle.END_ROD, entityLoc, (int) Math.ceil(particleMultiplier * 10.0), 1.5, .5, 1.5, 0.1);
		playSound(entityLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, .5f);
		entity.setVelocity(entity.getVelocity().add(new Vector(0, jumpHeight / 10.0, 0)));
		if (slowFall && entity instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false));
		new BukkitRunnable() {
			final int particleCount = (int) Math.ceil(3.0 * particleMultiplier);
			
			@Override
			public void run() {
				if (entity == null || !entity.isValid() || entity.isOnGround()) {
					this.cancel();
					return;
				}
				if (particleCount > 0)
					world.spawnParticle(Particle.CLOUD, entity.getLocation(), particleCount, 2, 2, 2, 0.1);
				if (noFall)
					entity.setFallDistance(0);
			}
		}.runTaskTimer(plugin, 3, 1);
	}
	@Override
	public void onMobHoldTick(Mob mob, GenericItem item) {
		LivingEntity target = mob.getTarget();
		if (target == null || target.isDead())
			return;
		Location mobLoc = mob.getLocation();
		Location targetLoc = target.getLocation();
		double dx = targetLoc.getX() - mobLoc.getX();
		double dz = targetLoc.getZ() - mobLoc.getZ();
		// Must be within close horizontal proximity to bother boosting.
		if (dx * dx + dz * dz > 64) // 8 blocks horizontally
			return;
		double verticalGap = targetLoc.getY() - mobLoc.getY();
		// A mob can already step/jump up about a block, so only use the boost to reach a target that is clearly higher and
		// out of normal reach - otherwise it would just be a wasted jump to somewhere the mob could already get to.
		boolean tooHighToReach = verticalGap >= 2.0;
		// Or the target is somewhat above but the mob can't see a direct route to it (blocked), so a boosted hop up onto/over
		// the obstruction is worthwhile instead of taking the long way around.
		boolean blockedRoute = verticalGap >= 1.0 && !mob.hasLineOfSight(target);
		if (!tooHighToReach && !blockedRoute)
			return;
		// Only avoid blasting straight into a low ceiling - just need a couple blocks of clearance above the head, not the
		// whole path to the target (terrain like the cliff face right beside the mob would otherwise block it forever).
		if (!hasHeadroom(mob))
			return;
		// Only nudge horizontally if the boost actually fired (i.e. it wasn't on cooldown).
		if (!mobActivate(mob, item))
			return;
		// Mob-only: nudge it horizontally toward the target so the boosted jump actually carries it that way.
		Vector toward = new Vector(dx, 0, dz);
		if (toward.lengthSquared() > 0)
			mob.setVelocity(mob.getVelocity().add(toward.normalize().multiply(0.4)));
	}
	private boolean hasHeadroom(Mob mob) {
		Block feet = mob.getLocation().getBlock();
		for (int i = 2; i <= 4; i++) // a few blocks of clearance above the mob's head
			if (!feet.getRelative(BlockFace.UP, i).isPassable())
				return false;
		return true;
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, JumpBoost.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		jumpHeight = registerSerializedDoubleField("jumpHeight", map);
		slowFall = registerSerializedBooleanField("slowFall", map);
		noFall = registerSerializedBooleanField("noFall", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}
