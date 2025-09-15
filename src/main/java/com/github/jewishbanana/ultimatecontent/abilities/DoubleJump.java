package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;

public class DoubleJump extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:double_jump";
	
	private double jumpHeightMultiplier;
	private double forwardVelocity;
	private double fallDamageNegation;
	private int activationPeriod;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;
	
	public DoubleJump(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		entity.setVelocity(entity.getVelocity().multiply(2.5).add(entity.getLocation().getDirection().multiply(forwardVelocity)).setY(0.4 * jumpHeightMultiplier));
        playSound(entity.getLocation(), Sound.ENTITY_RABBIT_JUMP, 20, .8);
        entity.setFallDistance(0f);
	}
	public void initFields() {
		this.jumpHeightMultiplier = getDoubleField("jumpHeightMultiplier", 1.0);
		this.forwardVelocity = getDoubleField("forwardVelocity", 0.0);
		this.fallDamageNegation = getDoubleField("fallDamageNegation", 0.0);
		this.activationPeriod = getIntegerField("activationPeriod", 0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, DoubleJump.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("jumpHeightMultiplier", jumpHeightMultiplier);
		map.put("forwardVelocity", forwardVelocity);
		map.put("fallDamageNegation", fallDamageNegation);
		map.put("activationPeriod", activationPeriod);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		jumpHeightMultiplier = (double) map.get("jumpHeightMultiplier");
		forwardVelocity = (double) map.get("forwardVelocity");
		fallDamageNegation = (double) map.get("fallDamageNegation");
		activationPeriod = (int) map.get("activationPeriod");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	public double getJumpHeightMultiplier() {
		return jumpHeightMultiplier;
	}
	public void setJumpHeightMultiplier(double jumpHeightMultiplier) {
		this.jumpHeightMultiplier = jumpHeightMultiplier;
	}
	public double getFallDamageNegation() {
		return fallDamageNegation;
	}
	public void setFallDamageNegation(double fallDamageNegation) {
		this.fallDamageNegation = fallDamageNegation;
	}
	public int getActivationPeriod() {
		return activationPeriod;
	}
	public void setActivationPeriod(int activationPeriod) {
		this.activationPeriod = activationPeriod;
	}
	public double getParticleMultiplier() {
		return particleMultiplier;
	}
	public void setParticleMultiplier(double particleMultiplier) {
		this.particleMultiplier = particleMultiplier;
	}
	public Ability.Action getCustomUsage() {
		return Ability.Action.UNBOUND;
	}
}
