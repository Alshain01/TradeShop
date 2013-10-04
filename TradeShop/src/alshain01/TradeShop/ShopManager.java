package alshain01.TradeShop;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import alshain01.Flags.Director;
import alshain01.Flags.Flag;
import alshain01.Flags.Flags;
import alshain01.Flags.area.Area;
import alshain01.TradeShop.PlayerCommand.CommandAction;

class ShopManager implements Listener {
	/*
	 *  Handles the creation of a shop
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	private static void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.LEFT_CLICK_BLOCK) { return; }
		if(e.getClickedBlock().getType() != Material.CHEST) { return; }
		
		// Is the player in "creation mode"?
		PlayerCommand command = TradeShop.instance.commandQueue.get(e.getPlayer().getName());
		if(command == null || command.action != CommandAction.CREATE) { return; }
		
		// Handle the shop creation flag
		if(TradeShop.instance.flags) {
			Area a = Director.getAreaAt(e.getPlayer().getLocation());
			Flag f = Flags.instance.getRegistrar().getFlag("TSAllowCreate");
			
			if(!a.getValue(f, false)
					&& !f.hasBypassPermission(e.getPlayer()) 
					&& !a.getTrustList(f).contains(e.getPlayer().getName())) { 
				e.getPlayer().sendMessage(a.getMessage(f).replaceAll("\\{Player\\}", e.getPlayer().getName()));
				return;
			}
		}
	
		// Is that chest already a shop?
		if(TradeShop.instance.shopData.containsKey(e.getClickedBlock().getLocation())) {
			e.getPlayer().sendMessage(Message.ShopExistsError.get());
			return;
		}
		
		// Everything seems to be in order, create the shop.
		TradeShop.instance.shopData.put(e.getClickedBlock().getLocation(), new Shop(e.getPlayer().getName()));
		e.getPlayer().sendMessage(Message.ShopCreated.get());
		TradeShop.instance.commandQueue.get(e.getPlayer().getName()).remove();
	}
	
	/*
	 * Handles the shop destruction by a player security
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onBlockBreak(BlockBreakEvent e) {
		if(e.getBlock().getType() != Material.CHEST) { return; }
		if(TradeShop.instance.adminMode.contains(e.getPlayer().getName())) { return; }
		
		// Check to see if the chest is one that TradeShop claims domain over.
		if(!TradeShop.instance.shopData.containsKey(e.getBlock().getLocation())) { return; }
		Shop shop = TradeShop.instance.shopData.get(e.getBlock().getLocation());
		
		// Check to see if the player can destroy this block
		if(shop.getOwner().equals(e.getPlayer().getName())) { return; }
		
		// This player can't destroy this shop.
		e.getPlayer().sendMessage(Message.RemoveShopError.get()
				.replaceAll("\\{Owner\\}", shop.getOwner()));
		e.setCancelled(true);
	}
	
	/*
	 * Handles the shop removal
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private static void onBlockBroken(BlockBreakEvent e) {
		if(!TradeShop.instance.shopData.containsKey(e.getBlock().getLocation())) { return; }
		
		TradeShop.instance.shopData.remove(e.getBlock().getLocation());
	}
}
