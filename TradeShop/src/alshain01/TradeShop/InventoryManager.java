package alshain01.TradeShop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import alshain01.Flags.Director;
import alshain01.Flags.Flag;
import alshain01.Flags.Flags;
import alshain01.Flags.area.Area;
import alshain01.TradeShop.PlayerCommand.CommandAction;

/**
 * Class that handles creation of trades in a shop
 */
public class InventoryManager implements Listener {
	/*
	 * Returns true if the provided inventory belongs to a trade shop
	 */
	private boolean isTradeShop(Inventory inventory) {
		if(inventory.getHolder() instanceof Block) {
			Block block = (Block)inventory.getHolder();
			if(block.getType()== Material.CHEST) {
				ConfigurationSection shop = TradeShop.instance.shopData.getConfig().getConfigurationSection("TradeShop");
				return shop.getKeys(false).contains(block.getLocation().toString());
			}
		}
		return false;
	}
	
	/*
	 * Ensures players don't do something unexpected.
	 * Dragging will cause issues with the design so it's not allowed.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private void onInventoryDrag(InventoryDragEvent e) {
		if(isTradeShop(e.getInventory())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onInventoryOpenEvent(InventoryOpenEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }
		
		Chest shop = (Chest)e.getInventory().getHolder();
		ConfigurationSection shopData = TradeShop.instance.shopData.getConfig().getConfigurationSection("TradeShop." + shop.getLocation().toString());
		String owner = shopData.getString((shop.getLocation().toString()) + ".Owner");
		
		if(!TradeShop.instance.adminMode.contains(e.getPlayer().getName()) &&
				(owner != null && owner != e.getPlayer().getName())) {
			
			// Handle the trade flag here
			if(TradeShop.instance.flags) {
				Area a = Director.getAreaAt(e.getPlayer().getLocation());
				Flag f = Flags.instance.getRegistrar().getFlag("TSAllowTrade");
				
				if(!a.getValue(f, false)
						&& !f.hasBypassPermission((Player)e.getPlayer()) 
						&& !a.getTrustList(f).contains(e.getPlayer().getName())) { 
					((Player)e.getPlayer()).sendMessage(a.getMessage(f).replaceAll("\\{Player\\}", e.getPlayer().getName()));
					e.setCancelled(true);
					return;
				}
			}
			
			e.setCancelled(true);
			// TODO Open trade window here
		}
	}
	
	/*
	 * Handles adding items to a shop
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private void onInventoryAdd(InventoryClickEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }

		// You must add/remove all or none of an item stack.
		if(e.getAction() != InventoryAction.PICKUP_ALL && e.getAction() != InventoryAction.DROP_ALL_CURSOR) { 
			e.setCancelled(true);
			return;
		}
		
		// We'll handle this in a MONITOR priority event
		if(e.getAction() == InventoryAction.PICKUP_ALL) { return; }
		
		// Is the player trying to create a trade?
		if(!TradeShop.instance.commandQueue.contains(e.getWhoClicked().getName()) 
				|| TradeShop.instance.commandQueue.get(e.getWhoClicked().getName()).getAction() != CommandAction.ADD)
		{
			e.setCancelled(true);
			return;
		}
		
		// You can only create a trade on empty blocks.
		if(e.getCurrentItem() != null) {
			e.setCancelled(true);
			return;
		}
		
		Chest shop = (Chest)e.getInventory().getHolder();
		ConfigurationSection shopData = TradeShop.instance.shopData.getConfig().getConfigurationSection("TradeShop." + shop.getLocation().toString());

		//TODO
	}
	
	/*
	 * Handles removing items from a shop
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onInventoryRemove(InventoryClickEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }
		
		// Thanks to the onInventoryAdd, we can't be here if they aren't picking up an item stack.
		
		Chest shop = (Chest)e.getInventory().getHolder();
		ConfigurationSection shopData = TradeShop.instance.shopData.getConfig().getConfigurationSection("TradeShop." + shop.getLocation().toString());
		
		if(shopData.getKeys(false).contains(String.valueOf(e.getSlot()))) {
			// The item slot was an assigned trade, so remove it.
			((Player)e.getWhoClicked()).sendMessage(Message.TradeRemoved.get());
			shopData.set(String.valueOf(e.getSlot()), null);
		}
		// The item slot was most likely the sale valuables.
	}
}
