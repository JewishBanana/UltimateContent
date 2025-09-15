package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.github.jewishbanana.playerarmorchangeevent.PlayerArmorChangeEvent;
import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemField;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.abilities.DoubleJump;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.mojang.datafixers.util.Pair;

public class DoubleJumpHandler implements Listener {
	
	private Main plugin;
	private RandomGenerator random = RandomGenerator.of("SplittableRandom");
	private Set<UUID> doubleJumpEnabledPlayers = new HashSet<>();
	private Set<UUID> doubleJumpingPlayers = new HashSet<>();
	private Map<UUID, Float> doubleJumpFall = new HashMap<>();
	private Set<UUID> doubleJumpDamaging = new HashSet<>();
	private Map<UUID, Pair<Integer, BukkitTask>> doubleJumpTimer = new HashMap<>();

	public DoubleJumpHandler(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		plugin.getServer().getOnlinePlayers().forEach(p -> enableDoubleJumpIfPresent(p, GenericItem.getItemBase(p.getEquipment().getBoots())));
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Iterator<UUID> it = doubleJumpEnabledPlayers.iterator();
				while (it.hasNext()) {
					Player p = Bukkit.getPlayer(it.next());
					if (p == null || !p.isOnline()) {
						it.remove();
						continue;
					}
					GenericItem base = GenericItem.getItemBase(p.getEquipment().getBoots());
		            if (base == null) {
		            	if (!EntityUtils.isPlayerImmune(p))
							p.setAllowFlight(false);
		            	it.remove();
		            	continue;
		            }
		            Ability ability = base.getAbility(DoubleJump.REGISTERED_KEY);
		            if (ability == null) {
		            	if (!EntityUtils.isPlayerImmune(p))
							p.setAllowFlight(false);
		            	it.remove();
		            	continue;
		            }
					if (!doubleJumpingPlayers.contains(p.getUniqueId()) && p.isOnGround())
						p.setAllowFlight(true);
				}
			}
		}.runTaskTimer(plugin, 0, 20);
		new BukkitRunnable() {
			@Override
			public void run() {
				Iterator<UUID> it = doubleJumpEnabledPlayers.iterator();
				while (it.hasNext()) {
					Entity e = Bukkit.getEntity(it.next());
					if (e == null || e.isDead())
						continue;
					UUID uuid = e.getUniqueId();
					if (!doubleJumpingPlayers.contains(uuid) && !doubleJumpDamaging.contains(uuid) && !EntityUtils.isEntityImmunePlayer(e)) {
						if (e.isOnGround()) {
							if (doubleJumpFall.getOrDefault(uuid, 0f) > 1) {
								if (e instanceof Player)
									((Player) e).setAllowFlight(false);
								e.setFallDistance(doubleJumpFall.remove(uuid));
								doubleJumpDamaging.add(uuid);
								plugin.getServer().getScheduler().runTaskLater(plugin, () -> doubleJumpDamaging.remove(uuid), 1);
							}
							Pair<Integer, BukkitTask> pair = doubleJumpTimer.get(uuid);
							if (pair != null && pair.getSecond() != null) {
								pair.getSecond().cancel();
								doubleJumpTimer.replace(uuid, Pair.of(pair.getFirst(), null));
							}
						} else {
							float fall = e.getFallDistance();
							if (doubleJumpFall.getOrDefault(uuid, 0f) < fall)
								doubleJumpFall.put(uuid, fall);
							if (fall > 0 && e instanceof Player) {
								Pair<Integer, BukkitTask> pair = doubleJumpTimer.get(uuid);
								if (pair != null && pair.getFirst() > 0 && pair.getSecond() == null)
									doubleJumpTimer.replace(uuid, Pair.of(pair.getFirst(), plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
										if (!EntityUtils.isEntityImmunePlayer(e))
											((Player) e).setAllowFlight(false);
									}, pair.getFirst())));
							}
						}
					}
					GenericItem base = GenericItem.getItemBase(((LivingEntity) e).getEquipment().getBoots());
					if (base == null)
						continue;
					Ability ability = base.getAbility(DoubleJump.REGISTERED_KEY);
					if (ability == null)
						continue;
					plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
						ItemField<?> field = base.getField(BunnyHop.bunnyHopParticleKey);
						if (field == null)
							return;
						switch ((byte) field.getSetting()) {
						case 0:
							for (int i=0; i < (int) (2.0 * ((DoubleJump) ability).getParticleMultiplier()); i++) {
		            			DustTransition dust = new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat());
		            			if (random.nextInt(2) == 0)
		            				dust = new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat());
		            			e.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, e.getLocation().add(random.nextDouble()/1.2-.4,0.1+(random.nextDouble()/3-.15),random.nextDouble()/1.2-.4), 1, 0, 0, 0, 0.001, dust);
		            		}
							break;
						case 1:
							for (Player p : e.getWorld().getPlayers())
								if (!p.equals(e) && p.getLocation().distanceSquared(e.getLocation()) < 400)
									for (int i=0; i < (int) (2.0 * ((DoubleJump) ability).getParticleMultiplier()); i++) {
										DustTransition dust = new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat());
										if (random.nextInt(2) == 0)
											dust = new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat());
										p.spawnParticle(Particle.DUST_COLOR_TRANSITION, e.getLocation().add(random.nextDouble()/1.2-.4,0.1+(random.nextDouble()/3-.15),random.nextDouble()/1.2-.4), 1, 0, 0, 0, 0.001, dust);
									}
							break;
						default:
							return;
						}
					});
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		enableDoubleJumpIfPresent(event.getPlayer(), GenericItem.getItemBase(event.getPlayer().getEquipment().getBoots()));
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		doubleJumpEnabledPlayers.remove(event.getPlayer().getUniqueId());
		doubleJumpingPlayers.remove(event.getPlayer().getUniqueId());
		doubleJumpTimer.remove(event.getPlayer().getUniqueId());
		if (!EntityUtils.isPlayerImmune(event.getPlayer()))
			event.getPlayer().setAllowFlight(false);
	}
	@EventHandler(ignoreCancelled = true)
	public void onArmorChange(PlayerArmorChangeEvent event) {
		if (event.getNewItem().getType() != Material.AIR) {
			if (event.getNewItem().getType().getEquipmentSlot() == EquipmentSlot.FEET)
				enableDoubleJumpIfPresent(event.getPlayer(), GenericItem.getItemBase(event.getNewItem()));
		} else {
			if (event.getOldItem().getType().getEquipmentSlot() == EquipmentSlot.FEET) {
				GenericItem base = GenericItem.getItemBase(event.getOldItem());
				if (base == null)
					return;
				if (base.hasAbility(DoubleJump.REGISTERED_KEY)) {
					doubleJumpEnabledPlayers.remove(event.getPlayer().getUniqueId());
					doubleJumpingPlayers.remove(event.getPlayer().getUniqueId());
					doubleJumpTimer.remove(event.getPlayer().getUniqueId());
					if (!EntityUtils.isPlayerImmune(event.getPlayer()))
						event.getPlayer().setAllowFlight(false);
				}
			}
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL && doubleJumpEnabledPlayers.contains(e.getEntity().getUniqueId())) {
			GenericItem base = GenericItem.getItemBase(((LivingEntity) e.getEntity()).getEquipment().getBoots());
            if (base == null) {
            	doubleJumpEnabledPlayers.remove(e.getEntity().getUniqueId());
            	return;
            }
            Ability ability = base.getAbility(DoubleJump.REGISTERED_KEY);
            if (ability == null) {
            	doubleJumpEnabledPlayers.remove(e.getEntity().getUniqueId());
            	return;
            }
            DoubleJump doubleJump = (DoubleJump) ability;
			if (e.getEntity().getFallDistance() < 5.0 * doubleJump.getJumpHeightMultiplier())
				e.setCancelled(true);
			doubleJumpingPlayers.remove(e.getEntity().getUniqueId());
			doubleJumpFall.put(e.getEntity().getUniqueId(), 0f);
			((Player) e.getEntity()).setAllowFlight(true);
			e.setDamage(e.getDamage()-(e.getDamage()*doubleJump.getFallDamageNegation()));
			return;
		}
	}
	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent e) {
		if (doubleJumpEnabledPlayers.contains(e.getPlayer().getUniqueId()) && !doubleJumpingPlayers.contains(e.getPlayer().getUniqueId()) && !EntityUtils.isPlayerImmune(e.getPlayer())) {
			Player p = e.getPlayer();
			e.setCancelled(true);
            p.setAllowFlight(false);
            p.setFlying(false);
            GenericItem base = GenericItem.getItemBase(p.getEquipment().getBoots());
            if (base == null) {
            	doubleJumpEnabledPlayers.remove(e.getPlayer().getUniqueId());
            	return;
            }
            Ability ability = base.getAbility(DoubleJump.REGISTERED_KEY);
            if (ability == null) {
            	doubleJumpEnabledPlayers.remove(e.getPlayer().getUniqueId());
            	return;
            }
            AbilityAttributes attributes = (AbilityAttributes) ability;
            if (!attributes.shouldActivate() || !attributes.use(p, attributes.doesSendCooldownMessages()))
            	return;
            attributes.internalActivation(p, e, base, p);
            doubleJumpFall.replace(p.getUniqueId(), p.getFallDistance());
            doubleJumpingPlayers.add(p.getUniqueId());
            new BukkitRunnable() {
				@SuppressWarnings("deprecation")
				@Override
            	public void run() {
            		if (p == null || !p.isOnline() || p.isDead() || EntityUtils.isPlayerImmune(p) || !doubleJumpingPlayers.contains(p.getUniqueId()) || p.isOnGround()) {
            			this.cancel();
            			doubleJumpingPlayers.remove(p.getUniqueId());
            			p.setAllowFlight(true);
            			return;
            		}
				}
            }.runTaskTimer(plugin, 5, 1);
            return;
		}
	}
	private void enableDoubleJumpIfPresent(Entity entity, GenericItem base) {
		if (base == null)
			return;
		Ability ability = base.getAbility(DoubleJump.REGISTERED_KEY);
		if (ability == null)
			return;
		doubleJumpEnabledPlayers.add(entity.getUniqueId());
		if (entity instanceof Player)
			((Player) entity).setAllowFlight(true);
		doubleJumpTimer.put(entity.getUniqueId(), Pair.of(((DoubleJump) ability).getActivationPeriod(), null));
	}
}
