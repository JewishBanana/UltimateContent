package com.github.jewishbanana.ultimatecontent.entities.pathfinders.target;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;

public class PathfinderOwnerHurtEntity extends CustomPathfinder {

	private UUID owner;
	
	public LivingEntity signal;

	public PathfinderOwnerHurtEntity(@NotNull Mob m, TameableEntity tameable) {
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
		return true;
	}
}
