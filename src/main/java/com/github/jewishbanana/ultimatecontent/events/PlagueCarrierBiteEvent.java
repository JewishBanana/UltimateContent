package com.github.jewishbanana.ultimatecontent.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a plague helper mob completes a bite attempt against a living target.
 */
public final class PlagueCarrierBiteEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final LivingEntity carrier;
	private final LivingEntity victim;

	public PlagueCarrierBiteEvent(LivingEntity carrier, LivingEntity victim) {
		this.carrier = carrier;
		this.victim = victim;
	}

	public LivingEntity getCarrier() {
		return carrier;
	}

	public LivingEntity getVictim() {
		return victim;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
