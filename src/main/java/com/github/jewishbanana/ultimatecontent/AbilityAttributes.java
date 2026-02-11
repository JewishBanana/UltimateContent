package com.github.jewishbanana.ultimatecontent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class AbilityAttributes extends Ability {
	
	protected static final JavaPlugin plugin;
	protected static final RandomGenerator random;
	
	public static final Map<UIAbilityType, AbilityTypeBlacklists> globalBlacklists;
	static {
		plugin = Main.getInstance();
		random = RandomGenerator.of("SplittableRandom");
		globalBlacklists = new HashMap<>();
	}
	
	private String defaultPath;
	private String configPath;
	private Target target = Target.DEFAULT;
	private boolean cooldownMessages = true;
	private String displayName;
	private String description;
	
	private float chance = 1f;
	private float volume = 1f;
	private Set<Material> immuneMaterials;
	private Set<EntityType> immuneEntities;
	private Set<Class<? extends CustomEntity<?>>> immuneCustom;
	
	protected static class AbilityTypeBlacklists {
		protected Set<Material> materials;
		protected Set<EntityType> entityTypes;
		protected Set<Class<? extends CustomEntity<?>>> customEntityTypes;
	}
	public AbilityAttributes(UIAbilityType type) {
		super(type);
		AbilityTypeBlacklists blacklists = globalBlacklists.get(type);
		if (blacklists != null) {
			this.immuneMaterials = blacklists.materials;
			this.immuneEntities = blacklists.entityTypes;
			this.immuneCustom = blacklists.customEntityTypes;
		}
	}
	/**
	 * <STRONG>This method should be overriden in the abilities class.</STRONG>
	 * <p>
	 * Empty activation method that will be run whenever the ability is activated. This is used by the default listener methods to fire a default action of the ability when activated. 
	 * For example, you have an ability that is designed for when a player right clicks to heal themself. You should override the activate method and make the heal inside this method, 
	 * because if the user decides to change the activation to when a player for example consumes an item to activate, it will automatically call the activate on the consuming player by 
	 * default within UIFramework without you having to code anything extra!
	 * 
	 * @param activatingEntity The activating entity
	 */
	public void activate(Entity activatingEntity, GenericItem base) {}
	
	public void activate(Location loc, GenericItem base) {}
	
	public boolean shouldActivate() {
		return chance == 1f || random.nextFloat() < chance;
	}
	public void internalActivation(Entity entity, Event event, GenericItem base, Entity activator) {
		activate(entity, base);
		if (base instanceof BaseItem item) {
			item.activatedAbility(this, event, activator, entity);
			if (item.shouldConsumeItem())
				base.getItem().setAmount(base.getItem().getAmount()-1);
		}
	}
	public void internalActivation(Location loc, Event event, GenericItem base, Entity activator) {
		activate(loc, base);
		if (base instanceof BaseItem item) {
			item.activatedAbility(this, event, activator, activator);
			if (item.shouldConsumeItem())
				base.getItem().setAmount(base.getItem().getAmount()-1);
		}
	}
	public boolean canBlockBeDamaged(Block block) {
		return (immuneMaterials == null || !immuneMaterials.contains(block.getType())) && !DependencyUtils.isBlockProtected(block);
	}
	public boolean canEntityBeHarmed(Entity entity) {
		if (immuneCustom != null) {
			CustomEntity<?> custom = UIEntityManager.getEntity(entity);
			if (custom != null && immuneCustom.contains(custom.getClass()))
				return false;
		}
		return entity.isValid() && (immuneEntities == null || !immuneEntities.contains(entity.getType())) && !EntityUtils.isEntityImmunePlayer(entity) && !DependencyUtils.isEntityProtected(entity);
	}
	public boolean canEntityBeHarmed(Entity entity, Entity owner) {
		if (owner == null)
			return canEntityBeHarmed(entity);
		return canEntityBeHarmed(entity)
				&& !(entity instanceof Tameable tameable && Utils.isNotNullAndCondition(tameable.getOwner(), t -> t.getUniqueId().equals(owner.getUniqueId())))
				&& !(UIEntityManager.getEntity(entity) instanceof TameableEntity tameable && Utils.isNotNullAndCondition(tameable.getOwner(), t -> t.equals(owner.getUniqueId())));
	}
	public void playSound(Location loc, Sound sound, float volume, float pitch) {
		loc.getWorld().playSound(loc, sound, getSoundCategory(), volume * this.volume, pitch);
	}
	/**
	 * Run whenever a PlayerInteractEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the interacting player.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void interacted(PlayerInteractEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			internalActivation(event.getPlayer(), event, base, event.getPlayer());
	}
	/**
	 * Run whenever a PlayerInteractEntityEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the right clicked entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void interactedEntity(PlayerInteractEntityEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			switch (getTarget()) {
			case ACTIVATOR:
				internalActivation(event.getPlayer(), event, base, event.getPlayer());
				break;
			default:
				internalActivation(event.getRightClicked(), event, base, event.getPlayer());
				break;
			}
	}
	/**
	 * Run whenever an EntityDamageByEntityEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the damaged entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void hitEntity(EntityDamageByEntityEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getDamager(), cooldownMessages))
			switch (getTarget()) {
			case ATTACKER:
			case ACTIVATOR:
				internalActivation(event.getDamager(), event, base, event.getDamager());
				break;
			default:
				internalActivation(event.getEntity(), event, base, event.getDamager());
				break;
			}
	}
	/**
	 * Run whenever an EntityDamageByEntityEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the damaged entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void wasHit(EntityDamageByEntityEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getEntity(), cooldownMessages))
			switch (getTarget()) {
			case ATTACKER:
				internalActivation(event.getDamager(), event, base, event.getEntity());
				break;
			default:
				internalActivation(event.getEntity(), event, base, event.getEntity());
				break;
			}
	}
	/**
	 * Run whenever a ProjectileLaunchEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the projectile shooter. Will always activate on the projectile if 
	 * the shooter is null.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void projectileThrown(ProjectileLaunchEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Entity) {
			if (use((Entity) event.getEntity().getShooter(), cooldownMessages))
				switch (getTarget()) {
				case PROJECTILE:
					internalActivation(event.getEntity(), event, base, (Entity) event.getEntity().getShooter());
					break;
				default:
					internalActivation((Entity) event.getEntity().getShooter(), event, base, (Entity) event.getEntity().getShooter());
					break;
				}
			return;
		}
		internalActivation(event.getEntity(), event, base, event.getEntity());
	}
	/**
	 * Run whenever a ProjectileHitEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the hit entity if exists, otherwise will activate on the projectile.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void projectileHit(ProjectileHitEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Entity) {
			if (getTarget() == Target.HIT_ENTITY && event.getHitEntity() == null)
				return;
			if (use((Entity) event.getEntity().getShooter(), cooldownMessages))
				switch (getTarget()) {
				case PROJECTILE:
					internalActivation(event.getEntity(), event, base, (Entity) event.getEntity().getShooter());
					break;
				case HIT_ENTITY:
					internalActivation(event.getHitEntity(), event, base, (Entity) event.getEntity().getShooter());
					break;
				case SHOOTER:
				case ATTACKER:
				case ACTIVATOR:
					internalActivation((Entity) event.getEntity().getShooter(), event, base, (Entity) event.getEntity().getShooter());
					break;
				default:
					internalActivation(event.getHitEntity() != null ? event.getHitEntity() : event.getEntity(), event, base, (Entity) event.getEntity().getShooter());
					break;
				}
			return;
		}
		internalActivation(event.getHitEntity() != null ? event.getHitEntity() : event.getEntity(), event, base, event.getEntity());
	}
	/**
	 * Run whenever a ProjectileHitEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the hit entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void hitByProjectile(ProjectileHitEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if ((getTarget() == Target.SHOOTER || getTarget() == Target.ATTACKER) && !(event.getEntity().getShooter() instanceof Entity))
			return;
		if (use(event.getHitEntity(), cooldownMessages))
			switch (getTarget()) {
			case SHOOTER:
			case ATTACKER:
				if (event.getEntity().getShooter() instanceof Entity)
					internalActivation((Entity) event.getEntity().getShooter(), event, base, event.getHitEntity());
				break;
			case PROJECTILE:
				internalActivation(event.getEntity(), event, base, event.getHitEntity());
				break;
			default:
				internalActivation(event.getHitEntity(), event, base, event.getHitEntity());
				break;
			}
	}
	/**
	 * Run whenever an EntityShootBowEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the shooting entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void shotBow(EntityShootBowEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getEntity(), cooldownMessages))
			switch (getTarget()) {
			case PROJECTILE:
				internalActivation(event.getProjectile(), event, base, event.getEntity());
				break;
			default:
				internalActivation(event.getEntity(), event, base, event.getEntity());
				break;
			}
	}
	/**
	 * Run whenever an InventoryClickEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the clicking entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void inventoryClick(InventoryClickEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getWhoClicked(), cooldownMessages))
			internalActivation(event.getWhoClicked(), event, base, event.getWhoClicked());
	}
	/**
	 * Run whenever a PlayerItemConsumeEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the consuming player.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void consumeItem(PlayerItemConsumeEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			internalActivation(event.getPlayer(), event, base, event.getPlayer());
	}
	/**
	 * Run whenever an EntityDropItemEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the entity dropping the item.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void dropItem(EntityDropItemEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getEntity(), cooldownMessages))
			switch (getTarget()) {
			case DROPPED_ITEM:
				internalActivation(event.getItemDrop(), event, base, event.getEntity());
				break;
			default:
				internalActivation(event.getEntity(), event, base, event.getEntity());
				break;
			}
	}
	/**
	 * Run whenever an EntityPickupItemEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the entity picking up.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void pickupItem(EntityPickupItemEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getEntity(), cooldownMessages))
			switch (getTarget()) {
			case DROPPED_ITEM:
				internalActivation(event.getItem(), event, base, event.getEntity());
				break;
			default:
				internalActivation(event.getEntity(), event, base, event.getEntity());
				break;
			}
	}
	/**
	 * Run whenever an EntityDeathEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the dying entity.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void entityDeath(EntityDeathEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getEntity(), cooldownMessages))
			internalActivation(event.getEntity(), event, base, event.getEntity());
	}
	/**
	 * Run whenever a PlayerRespawnEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the player respawning.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void entityRespawn(PlayerRespawnEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			internalActivation(event.getPlayer(), event, base, event.getPlayer());
	}
	/**
	 * Run whenever a BlockPlaceEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the player placing the block.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void placeBlock(BlockPlaceEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			switch (getTarget()) {
			case BLOCK:
				internalActivation(event.getBlock().getLocation().add(.5,.5,.5), event, base, event.getPlayer());
				break;
			default:
				internalActivation(event.getPlayer(), event, base, event.getPlayer());
				break;
			}
	}
	/**
	 * Run whenever a BlockPlaceEvent is fired involving the ability.
	 * <p>
	 * <STRONG>Default Behavior:</STRONG> Will activate on the player breaking the block.
	 * 
	 * @param event The event involved
	 * @param base The base class of the firing item
	 */
	public void breakBlock(BlockBreakEvent event, GenericItem base) {
		if (!shouldActivate())
			return;
		if (use(event.getPlayer(), cooldownMessages))
			switch (getTarget()) {
			case BLOCK:
				internalActivation(event.getBlock().getLocation().add(.5,.5,.5), event, base, event.getPlayer());
				break;
			default:
				internalActivation(event.getPlayer(), event, base, event.getPlayer());
				break;
			}
	}
	
	public int getIntegerField(String field, int defaultValue) {
		if (plugin.getConfig().contains(configPath+'.'+field, true))
			try {
				return plugin.getConfig().getInt(configPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dinteger &evalue from config path '"+configPath+'.'+field+"' please fix this value!"));
			}
		if (plugin.getConfig().contains(defaultPath+'.'+field))
			try {
				return plugin.getConfig().getInt(defaultPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dinteger &evalue from config path '"+defaultPath+'.'+field+"' please fix this value!"));
			}
		return defaultValue;
	}
	public int getIntegerField(String field) {
		return getIntegerField(field, 0);
	}
	public double getDoubleField(String field, double defaultValue) {
		if (plugin.getConfig().contains(configPath+'.'+field, true))
			try {
				return plugin.getConfig().getDouble(configPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &ddouble &evalue from config path '"+configPath+'.'+field+"' please fix this value!"));
			}
		if (plugin.getConfig().contains(defaultPath+'.'+field))
			try {
				return plugin.getConfig().getDouble(defaultPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &ddouble &evalue from config path '"+defaultPath+'.'+field+"' please fix this value!"));
			}
		return defaultValue;
	}
	public double getDoubleField(String field) {
		return getDoubleField(field, 0.0);
	}
	public boolean getBooleanField(String field, boolean defaultValue) {
		if (plugin.getConfig().contains(configPath+'.'+field, true))
			try {
				return plugin.getConfig().getBoolean(configPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dboolean &evalue from config path '"+configPath+'.'+field+"' please fix this value!"));
			}
		if (plugin.getConfig().contains(defaultPath+'.'+field))
			try {
				return plugin.getConfig().getBoolean(defaultPath+'.'+field);
			} catch (NumberFormatException e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dboolean &evalue from config path '"+defaultPath+'.'+field+"' please fix this value!"));
			}
		return defaultValue;
	}
	public boolean getBooleanField(String field) {
		return getBooleanField(field, false);
	}
	public static void reload() {
		globalBlacklists.clear();
	}
	
	public String getDefaultConfigPath() {
		return defaultPath;
	}
	public void setDefaultConfigPath(String configPath) {
		this.defaultPath = configPath;
	}
	public String getConfigPath() {
		return configPath;
	}
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	public boolean doesSendCooldownMessages() {
		return cooldownMessages;
	}
	public void setSendCooldownMessages(boolean shouldSend) {
		this.cooldownMessages = shouldSend;
	}
	public double getChance() {
		return chance;
	}
	public void setChance(float chance) {
		this.chance = chance;
	}
	public float getVolume() {
		return volume;
	}
	public void setVolume(float volume) {
		this.volume = volume;
	}
	public String getDisplayName() {
		return displayName == null ? getType().getDisplayName() : displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description == null ? getType().getDescription() : description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Ability.Action getCustomUsage() {
		return null;
	}
	public SoundCategory getSoundCategory() {
		return SoundCategory.PLAYERS;
	}
	public Set<Material> getImmuneMaterials() {
		return immuneMaterials;
	}
	public void setImmuneMaterials(Set<Material> immuneMaterials) {
		AbilityTypeBlacklists blacklists = globalBlacklists.get(getType());
		if (blacklists != null) {
			Set<Material> temp = EnumSet.copyOf(blacklists.materials);
			temp.addAll(immuneMaterials);
			this.immuneMaterials = temp;
			return;
		}
		this.immuneMaterials = immuneMaterials;
	}
	public Set<EntityType> getImmuneEntities() {
		return immuneEntities;
	}
	public void setImmuneEntities(Set<EntityType> immuneEntities) {
		AbilityTypeBlacklists blacklists = globalBlacklists.get(getType());
		if (blacklists != null) {
			Set<EntityType> temp = EnumSet.copyOf(blacklists.entityTypes);
			temp.addAll(immuneEntities);
			this.immuneEntities = temp;
			return;
		}
		this.immuneEntities = immuneEntities;
	}
	public Set<Class<? extends CustomEntity<?>>> getImmuneCustomEntities() {
		return immuneCustom;
	}
	public void setImmuneCustomEntities(Set<Class<? extends CustomEntity<?>>> immuneCustom) {
		AbilityTypeBlacklists blacklists = globalBlacklists.get(getType());
		if (blacklists != null) {
			Set<Class<? extends CustomEntity<?>>> temp = new HashSet<>(blacklists.customEntityTypes);
			temp.addAll(immuneCustom);
			this.immuneCustom = temp;
			return;
		}
		this.immuneCustom = immuneCustom;
	}
	public int registerSerializedIntegerField(String identifier, Map<String, Object> map, int defaultValue) {
		return registerSerializedField(identifier, map, getIntegerField(identifier, defaultValue)).getValue();
	}
	public int registerSerializedIntegerField(String identifier, Map<String, Object> map) {
		return registerSerializedIntegerField(identifier, map, 0);
	}
	public double registerSerializedDoubleField(String identifier, Map<String, Object> map, double defaultValue) {
		return registerSerializedField(identifier, map, getDoubleField(identifier, defaultValue)).getValue();
	}
	public double registerSerializedDoubleField(String identifier, Map<String, Object> map) {
		return registerSerializedDoubleField(identifier, map, 0.0);
	}
	public boolean registerSerializedBooleanField(String identifier, Map<String, Object> map, boolean defaultValue) {
		return registerSerializedField(identifier, map, getBooleanField(identifier, defaultValue)).getValue();
	}
	public boolean registerSerializedBooleanField(String identifier, Map<String, Object> map) {
		return registerSerializedBooleanField(identifier, map, false);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		cooldownMessages = registerSerializedBooleanField("cooldownMessages", map, true);
		chance = (float) registerSerializedDoubleField("chance", map, 1f);
		volume = (float) registerSerializedDoubleField("volume", map, 1f);
		try {
			String value = (String) map.get("target");
			if (value != null) {
				Target target = Target.forName(value);
				if (target == null)
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading ability from config path &d'"+configPath+".target' &ethere is no such target type &a'"+DataUtils.getConfigString(configPath+".target")+"'"));
				else
					setTarget(target);
			}
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
		}
		if (map.containsKey("blacklist")) {
			if (plugin.getConfig().contains(configPath+".blacklist.blocks", true)) {
				Set<Material> types = new HashSet<>();
				for (String s : DataUtils.getConfigStringList(configPath+".blacklist.blocks")) {
					Material material = Material.matchMaterial(s);
					if (material == null) {
						Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+s+"' &cto ability &e"+getDisplayName()+" &cblock blacklist in config section &b"+configPath+".blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the abilities block blacklist!"));
						continue;
					}
					types.add(material);
				}
				if (!types.isEmpty())
					setImmuneMaterials(EnumSet.copyOf(types));
			}
			if (plugin.getConfig().contains(configPath+".blacklist.entities", true)) {
				Set<EntityType> types = new HashSet<>();
				Set<Class<? extends CustomEntity<?>>> customTypes = new HashSet<>();
				for (String s : DataUtils.getConfigStringList(configPath+".blacklist.entities"))
					try {
						UIEntityManager customType = UIEntityManager.getEntityType(s);
						if (customType != null) {
							customTypes.add(customType.getEntityClass());
							continue;
						}
						EntityType entityType = EntityType.valueOf(s.toUpperCase());
						types.add(entityType);
					} catch (IllegalArgumentException e) {
						Main.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+s+"' &cto ability &e"+getDisplayName()+" &centity blacklist in config section &b"+configPath+".blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the abilities entity blacklist!"));
					}
				if (!types.isEmpty())
					setImmuneEntities(EnumSet.copyOf(types));
				if (!customTypes.isEmpty())
					setImmuneCustomEntities(customTypes);
			}
		}
	}
	public static <T extends AbilityAttributes> T createInitializedAbilityInstance(Class<T> abilityClass) {
		T ability = UIAbilityType.createAbilityInstance(abilityClass);
		String path = "abilities."+ability.getType().getRegisteredName().replaceFirst("uc:", "");
		ability.setConfigPath(path);
		ability.setDefaultConfigPath(path);
		ability.deserialize(plugin.getConfig().getConfigurationSection(path).getValues(false));
		return ability;
	}
	public enum Target {
		
		DEFAULT,
		ACTIVATOR,
		PROJECTILE,
		HIT_ENTITY,
		INTERACTED_ENTITY,
		ATTACKER,
		SHOOTER,
		DROPPED_ITEM,
		BLOCK;
		
		public static Target forName(String name) {
			for (Target id : values())
				if (id.toString().equals(name.toUpperCase().replace('-', '_')))
					return id;
			return null;
		}
	}
}
