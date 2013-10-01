package alshain01.TradeShop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

public class ShopManager implements Listener {
	/*
	 *  Handles the creation of a shop
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if(e.getClickedBlock().getType() != Material.CHEST) { return; }
		
		// Is the player in "creation mode"?
		if(!TradeShop.instance.commandQueue.containsKey(e.getPlayer().getName())) { return; }
		
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
	
		String blockLoc = e.getClickedBlock().getLocation().toString();
		ConfigurationSection data = TradeShop.instance.shopData.getConfig().getConfigurationSection("Shops");
		
		// Is that chest already a shop?
		if(data.getKeys(false).contains(blockLoc)) {
			e.getPlayer().sendMessage(Message.ShopExistsError.get());
			return;
		}
		
		// Everything seems to be in order, create the shop.
		data.set(blockLoc + ".Owner", e.getPlayer().getName());
		data.set(blockLoc + ".NextID", 0);
		e.getPlayer().sendMessage(Message.ShopCreated.get());
		TradeShop.instance.commandQueue.get(e.getPlayer().getName()).remove();
	}
	
	/*
	 * Handles the shop destruction security
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private void onBlockBreak(BlockBreakEvent e) {
		if(e.getBlock().getType() != Material.CHEST) { return; }
		// TODO: Change this to an admin mode so admins don't accidentally kill shops (similar to /ignoreclaims in GriefPrevention)
		if(e.getPlayer().hasPermission("simpleshop.admin")) { return; }
		
		String blockLoc = e.getBlock().getLocation().toString();
		ConfigurationSection data = TradeShop.instance.shopData.getConfig().getConfigurationSection("Shops");
		
		// Check to see if the chest is one that TradeShop claims domain over.
		if(data.getKeys(false).contains(blockLoc.toString())) {
			// Check to see if the player can destroy this block
			String owner = data.getString(blockLoc.toString() + ".Owner");
			if(!owner.equalsIgnoreCase(e.getPlayer().getName())) {
				e.getPlayer().sendMessage(Message.RemoveShopError.get()
						.replaceAll("\\{Owner\\}", owner));
				e.setCancelled(true);
			}
		}
	}
	
	/*
	 * Handles the shop removal
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onBlockBroken(BlockBreakEvent e) {
		String blockLoc = e.getBlock().getLocation().toString();
		ConfigurationSection data = TradeShop.instance.shopData.getConfig().getConfigurationSection("Shops");
		
		if(data.getKeys(false).contains(blockLoc.toString())) {
			e.getPlayer().sendMessage(Message.TradeShopRemoved.get());
			data.set(blockLoc, null);
		}
	}
}
