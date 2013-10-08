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
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onPlayerInteract(PlayerInteractEvent e) {
		TradeShop.instance.Debug("Shop Manager Create Event");
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if(e.getClickedBlock().getType() != Material.CHEST) { return; }
		
		// Is the player in "creation mode"?
		PlayerCommand command = TradeShop.commandQueue.get(e.getPlayer().getName());
		if(command == null || command.action != CommandAction.CREATE) { return; }
		
		// Handle the shop creation flag
		if(TradeShop.flags) {
			Area a = Director.getAreaAt(e.getPlayer().getLocation());
			Flag f = Flags.instance.getRegistrar().getFlag("TSAllowCreate");
			
			if(!a.getValue(f, false)
					&& !f.hasBypassPermission(e.getPlayer()) 
					&& !a.getTrustList(f).contains(e.getPlayer().getName())) { 
				e.getPlayer().sendMessage(a.getMessage(f).replaceAll("\\{Player\\}", e.getPlayer().getName()));
				return;
			}
		}
	
		// Create the shop
		if(!(new Shop(e.getClickedBlock().getLocation()).setOwner(e.getPlayer().getName()))) {
			e.getPlayer().sendMessage(Message.ShopExistsError.get());
			return;
		}
		
		// Everything seems to be in order, clean up
		e.getPlayer().sendMessage(Message.ShopCreated.get());
		TradeShop.commandQueue.get(e.getPlayer().getName()).remove();
		e.setCancelled(true);
	}
	
	/*
	 * Handles the shop destruction by a player security
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private static void onBlockBreak(BlockBreakEvent e) {
		if(e.getBlock().getType() != Material.CHEST) { return; }
		if(TradeShop.adminMode.contains(e.getPlayer().getName())) { return; }
		
		// Check to see if the chest is one that TradeShop claims domain over.
		Shop shop = new Shop(e.getBlock().getLocation());
		if(!shop.exists()) { return; }
		
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
		new Shop(e.getBlock().getLocation()).remove();
	}
}
