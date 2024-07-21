package com.github.jewishbanana.ultimatecontent.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerArmorChangeEvent extends Event implements Cancellable {
 
    private static final HandlerList handlers = new HandlerList();
 
    private boolean cancelled;
    private Player player;
    private ItemStack oldItem;
    private ItemStack newItem;
    private EquipmentSlot slot;
    private Reason reason;

    public PlayerArmorChangeEvent(Player player, EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, Reason reason) {
        this.player = player;
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
        this.reason = reason;
    }
    public Player getPlayer() {
        return player;
    }
    public ItemStack getOldItem() {
        return oldItem;
    }
    public ItemStack getNewItem() {
        return newItem;
    }
    public EquipmentSlot getSlot() {
        return slot;
    }
    public Reason getReason() {
        return reason;
    }
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    public enum Reason {
        INVENTORY_ACTION,
        RIGHT_CLICK,
        DISPENSER,
        ITEM_BREAK;
    }
}
