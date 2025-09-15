package com.github.jewishbanana.ultimatecontent.entities.pathfinders.target;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;

public class PathfinderAllyHurtByEntity extends CustomPathfinder {

	private UUID owner;
	
	public LivingEntity signal;

	public PathfinderAllyHurtByEntity(@NotNull Mob m, TameableEntity tameable) {
		super(m);
		this.owner = tameable.getOwner();
		PathfindersHandler.addPathfinder(tameable, this);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.TARGETING };
	}
	@Override
	public boolean canStart() {
		if (signal == null)
			return false;
		if (signal.getUniqueId().equals(owner)
				|| (signal instanceof Tameable tameable && tameable.getOwner() != null && tameable.getOwner().getUniqueId().equals(owner))
				|| (UIEntityManager.getEntity(signal) instanceof TameableEntity tameable && tameable.getOwner() != null && tameable.getOwner().equals(owner))) {
			signal = null;
			return false;
		}
		return true;
	}
	@Override
	public void start() {
		entity.setTarget(signal);
		signal = null;
	}
	@Override
	public void tick() {
		if (EntityUtils.isEntityImmunePlayer(entity.getTarget()))
			entity.setTarget(null);
	}
	@Override
	public boolean canContinueToUse() {
		LivingEntity target = entity.getTarget();
		if (target == null || target.isDead())
			return false;
		if (!target.getWorld().equals(entity.getWorld()) || target.getLocation().distanceSquared(entity.getLocation()) > 400) {
			Entity leader = Bukkit.getEntity(owner);
			if (leader == null || !leader.getWorld().equals(target.getWorld()) || target.getLocation().distanceSquared(leader.getLocation()) > 225)
				return false;
		}
		return true;
	}
}
