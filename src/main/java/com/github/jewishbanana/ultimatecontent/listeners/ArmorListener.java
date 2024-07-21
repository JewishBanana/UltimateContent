package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.ultimatecontent.events.PlayerArmorChangeEvent;
import com.github.jewishbanana.ultimatecontent.events.PlayerArmorChangeEvent.Reason;

public class ArmorListener implements Listener {
 
    private Set<InventoryAction> pickUpActions = new HashSet<>(Arrays.asList(InventoryAction.COLLECT_TO_CURSOR, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT, InventoryAction.MOVE_TO_OTHER_INVENTORY));
    private Set<InventoryAction> placeActions = new HashSet<>(Arrays.asList(InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME));
    private Set<EquipmentSlot> armorSlots = new HashSet<>(Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
 
    public ArmorListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player))
            return;
        if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && event.getSlotType() == SlotType.ARMOR) {
            if (isNotNullOrAir(event.getCurrentItem()) && isNotNullOrAir(event.getCursor())) {
                PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent((Player) event.getWhoClicked(), event.getCurrentItem().getType().getEquipmentSlot(), event.getCurrentItem(), event.getCursor(), Reason.INVENTORY_ACTION);
                Bukkit.getPluginManager().callEvent(armorEvent);
                if (armorEvent.isCancelled()) {
                    event.setResult(Result.DENY);
                    if (((Player) event.getWhoClicked()).getGameMode() == GameMode.CREATIVE)
                        ((Player) event.getWhoClicked()).setItemOnCursor(event.getCursor());
                }
            }
            return;
        }
        if (event.getClickedInventory().getType() == InventoryType.PLAYER && event.getView().getTopInventory() != null && event.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
            if (event.getSlot() > 35 && event.getSlot() < 40) {
                if (pickUpActions.contains(event.getAction()) && isNotNullOrAir(event.getCurrentItem())) {
                    PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent((Player) event.getWhoClicked(), event.getCurrentItem().getType().getEquipmentSlot(), event.getCurrentItem(), new ItemStack(Material.AIR), Reason.INVENTORY_ACTION);
                    Bukkit.getPluginManager().callEvent(armorEvent);
                    if (armorEvent.isCancelled())
                        event.setResult(Result.DENY);
                    return;
                }
                if (placeActions.contains(event.getAction()) && isNotNullOrAir(event.getCursor())) {
                    PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent((Player) event.getWhoClicked(), event.getCursor().getType().getEquipmentSlot(), new ItemStack(Material.AIR), event.getCursor(), Reason.INVENTORY_ACTION);
                    Bukkit.getPluginManager().callEvent(armorEvent);
                    if (armorEvent.isCancelled()) {
                        event.setResult(Result.DENY);
                        if (((Player) event.getWhoClicked()).getGameMode() == GameMode.CREATIVE)
                            ((Player) event.getWhoClicked()).setItemOnCursor(event.getCursor());
                    }
                    return;
                }
            } else if (event.isShiftClick() && isNotNullOrAir(event.getCurrentItem())) {
                EquipmentSlot slot = event.getCurrentItem().getType().getEquipmentSlot();
                if (!isNotNullOrAir(((Player) event.getWhoClicked()).getEquipment().getItem(slot))) {
                    PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent((Player) event.getWhoClicked(), slot, new ItemStack(Material.AIR), event.getCurrentItem(), Reason.INVENTORY_ACTION);
                    Bukkit.getPluginManager().callEvent(armorEvent);
                    if (armorEvent.isCancelled())
                        event.setResult(Result.DENY);
                    return;
                }
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getHand() != null && !(event.hasBlock() && isInteractable(event.getClickedBlock()))) {
            Player p = event.getPlayer();
            ItemStack item = event.getHand() == EquipmentSlot.HAND ? p.getEquipment().getItemInMainHand() : p.getEquipment().getItemInOffHand();
            if (!isNotNullOrAir(item) || item.getType() == Material.CARVED_PUMPKIN)
                return;
            EquipmentSlot slot = item.getType().getEquipmentSlot();
            if (!armorSlots.contains(slot))
                return;
            PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent(p, slot, isNotNullOrAir(p.getEquipment().getItem(slot)) ? p.getEquipment().getItem(slot) : new ItemStack(Material.AIR), item, Reason.RIGHT_CLICK);
            Bukkit.getPluginManager().callEvent(armorEvent);
            if (armorEvent.isCancelled())
                event.setCancelled(true);
            return;
        }
    }
    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.isCancelled() || !(event.getTargetEntity() instanceof Player))
            return;
        PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent((Player) event.getTargetEntity(), event.getItem().getType().getEquipmentSlot(), new ItemStack(Material.AIR), event.getItem(), Reason.DISPENSER);
        Bukkit.getPluginManager().callEvent(armorEvent);
        if (armorEvent.isCancelled())
            event.setCancelled(true);
    }
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.isCancelled() || ((Damageable) event.getItem().getItemMeta()).getDamage()+event.getDamage() < event.getItem().getType().getMaxDurability())
            return;
        PlayerArmorChangeEvent armorEvent = new PlayerArmorChangeEvent(event.getPlayer(), event.getItem().getType().getEquipmentSlot(), event.getItem(), new ItemStack(Material.AIR), Reason.ITEM_BREAK);
        Bukkit.getPluginManager().callEvent(armorEvent);
        if (armorEvent.isCancelled())
            event.setCancelled(true);
    }
    private boolean isNotNullOrAir(ItemStack item) {
        return item == null ? false : item.getType() != Material.AIR;
    }
    private boolean isInteractable(Block block) {
        Material type = block.getType();
        if (!type.isInteractable())
            return false;
        if (Tag.STAIRS.isTagged(type) || Tag.FENCES.isTagged(type) || Tag.CANDLES.isTagged(type) || Tag.CANDLE_CAKES.isTagged(type) || Tag.CAULDRONS.isTagged(type))
            return false;
        switch (type) {
        case MOVING_PISTON:
        case PUMPKIN:
        case CAKE:
            return false;
        case REDSTONE_WIRE:
            RedstoneWire wire = (RedstoneWire) block.getBlockData();
            for (BlockFace face : Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST))
                if (wire.getFace(face) == Connection.NONE)
                    continue;
                else if (wire.getFace(face) == Connection.SIDE) {
                    if (block.getRelative(face).getType() == Material.REDSTONE_WIRE)
                        return false;
                } else
                    return false;
            return true;
        default:
            return true;
        }
    }
}
