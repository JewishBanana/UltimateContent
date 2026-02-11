package com.github.jewishbanana.ultimatecontent.entities.pathfinders.target;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;

public class PathfinderHurtByEntity extends CustomPathfinder {
	
	private UUID owner;
	
	public LivingEntity signal;

	public PathfinderHurtByEntity(@NotNull Mob m, UUID owner) {
		super(m);
		this.owner = owner;
		PathfindersHandler.addPathfinder(m.getUniqueId(), this);
	}
	public PathfinderHurtByEntity(@NotNull Mob m) {
		this(m, null);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.TARGETING };
	}
	@Override
	public boolean canStart() {
		if (signal == null)
			return false;
		if (signal.getUniqueId().equals(owner) || EntityUtils.isEntityOwner(signal, owner)) {
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
