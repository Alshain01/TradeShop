package alshain01.TradeShop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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

/*
 * Class that handles creation of trades in a shop
 */
class InventoryManager implements Listener {
	/*
	 * Returns true if the provided inventory belongs to a trade shop
	 */
	private static boolean isTradeShop(Inventory inventory) {
		if(!(inventory.getHolder() instanceof Block)) { return false; }
		
		Block block = (Block)inventory.getHolder();
		if(block.getType() != Material.CHEST) { return false; }
		
		return Shop.getAt(block.getLocation()) != null;
	}
	
	/*
	 * Ensures players don't do something unexpected.
	 * Dragging will cause issues with the design so it's not allowed.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onInventoryDrag(InventoryDragEvent e) {
		if(isTradeShop(e.getInventory())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onInventoryOpenEvent(InventoryOpenEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }
		
		Shop data = new Shop(((Chest)e.getInventory().getHolder()).getLocation());
		
		// If the player is the owner or in admin mode then don't show the trade window.
		if(!TradeShop.adminMode.contains(e.getPlayer().getName()) && (data.getOwner() != e.getPlayer().getName())) {

			// Handle the trade flag here
			if(TradeShop.flags) {
				Area a = Director.getAreaAt(e.getPlayer().getLocation());
				Flag f = Flags.getRegistrar().getFlag("TSAllowTrade");
				
				if(!a.getValue(f, false)
						&& !e.getPlayer().hasPermission(f.getBypassPermission()) 
						&& !a.getTrustList(f).contains(e.getPlayer().getName())) { 
					((Player)e.getPlayer()).sendMessage(a.getMessage(f).replaceAll("\\{Player\\}", e.getPlayer().getName()));
					e.setCancelled(true);
					return;
				}
			}
			
			e.setCancelled(true);
			
			// Inventory i = Bukkit.createInventory(null, InventoryType.MERCHANT);
			// TODO Open trade window here
		}
	}
	
	/*
	 * Handles adding items to a shop
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onInventoryAdd(InventoryClickEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }
		
		// We'll handle this in a MONITOR priority event
		if(e.getAction() == InventoryAction.PICKUP_ALL) { return; }
		
		// You must add/remove all or none of an item stack.
		if(e.getAction() != InventoryAction.DROP_ALL_CURSOR) { 
			e.setCancelled(true);
			return;
		}
		
		// Is the player trying to create a trade?
		if(!TradeShop.commandQueue.contains(e.getWhoClicked().getName()) 
				|| TradeShop.commandQueue.get(e.getWhoClicked().getName()).getAction() != CommandAction.ADD)
		{
			e.setCancelled(true);
			return;
		}
		
		if(!e.getCursor().equals(e.getCurrentItem())) { return; }
		
		// You can only create a trade on empty blocks 0 through 8.
		if(e.getCurrentItem() != null) {
			e.setCancelled(true);
			return;
		}
		
		// Add the trade
		Shop shop = new Shop(((Chest)e.getInventory().getHolder()).getLocation());
		Trade trade = TradeShop.commandQueue.get(e.getWhoClicked().getName()).getTrade();
		trade.setSellItem(e.getCursor());
		shop.addTrade(trade);
	}
	
	/*
	 * Handles removing items from a shop
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private static void onInventoryRemove(InventoryClickEvent e) {
		if(!isTradeShop(e.getInventory())) { return; }
		
		// Thanks to the onInventoryAdd, we can't be here if they aren't picking up an item stack.
		// If it's in the first 9 slots, it is a trade item.
		if(e.getSlot() < 8) {
			Shop data = new Shop(((Chest)e.getInventory().getHolder()).getLocation());
			
			// The item slot was an assigned trade, so remove it.
			((Player)e.getWhoClicked()).sendMessage(Message.TradeRemoved.get());
			data.removeTrade(e.getSlot());
		}
		// The item slot was most likely the sale valuables.  We do nothing.
	}
}
