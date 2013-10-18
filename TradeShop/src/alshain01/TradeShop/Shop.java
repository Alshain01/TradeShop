package alshain01.TradeShop;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class Shop {
	private ConfigurationSection shopData;
	
	/**
	 * Returns a shop based on the location provided.
	 * Location may be a stock or a repository.
	 * Returns null if not found.
	 * 
	 * @param location
	 * @return The shop containing the location as a stock or repository
	 */
	public static Shop getAt(Location location) {
		ConfigurationSection data = 
				TradeShop.dataStore.getConfig().getConfigurationSection("Shops." + location.getWorld().getName());
		
		Set<String> shops = data.getKeys(false);
		for(String s : shops) {
			Vector stockLoc = data.getVector(s + ".StockLocation");
			Vector repoLeftLoc = data.getVector(s + ".RepositoryLocation.Left");
			Vector repoRightLoc = data.getVector(s + ".RepositoryLocation.Right");
			
			if((stockLoc != null && stockLoc.equals(location.toVector()) 
					|| (repoLeftLoc != null && repoLeftLoc.equals(location.toVector()))
					|| (repoRightLoc != null && repoRightLoc.equals(location.toVector())))) {
				return new Shop(location.getWorld().getName(), s);
			}
		}
		return null;
	}
	
	/**
	 * Returns a shop based on the location provided.
	 * Location must be a stock chest.
	 * Returns null if not found.
	 * 
	 * @param location
	 * @return The shop containing the location as a stock
	 */
	public static Shop getStockAt(Location location) {
		Shop shop = getAt(location);
		if(shop != null && shop.getStockLocation().equals(location)) { return shop; }
		return null;
	}
	
	/**
	 * Returns a shop based on the location provided.
	 * Location must be a repository chest.
	 * Returns null if not found.
	 * 
	 * @param location
	 * @return The shop containing the location as a repository
	 */
	public static Shop getRepositoryAt(Location location) {
		Shop shop = getAt(location);
		if(shop != null && 
				(shop.getRepositoryLocation()[0].equals(location) 
						|| shop.getRepositoryLocation()[1].equals(location))) 
		{ return shop; }
		return null;
	}
	
	/**
	 * Creates a new shop at the given block location.
	 * 
	 * @param location The location of the chest that will be a shop
	 */
	protected Shop(String worldName, String playerName) {
		shopData = TradeShop.dataStore.getConfig().getConfigurationSection("Shops." + worldName + "." + playerName);
	}
	
	/**
	 * Retrieves the owner of the current shop.
	 * 
	 * @return The owner name of the shop.
	 */
	public String getOwner() {
		String[] path = shopData.getCurrentPath().split("\\.");
		return path[path.length - 1];
	}
	
	/**
	 * Retrieves the location of the current shop 
	 * 
	 * @return The shop location.
	 */
	public Location getStockLocation() {
		String[] path = shopData.getCurrentPath().split("\\.");
		
		World w = Bukkit.getServer().getWorld(path[path.length - 2]);
		Vector v = shopData.getVector("ShopLocation");
		
		return new Location(w, v.getX(), v.getY(), v.getZ());
	}
	
	/**
	 * Sets the location of the current shop
	 * 
	 * @param location The location to set.
	 * @return False if that location is already a shop.
	 */
	protected boolean setStockLocation(Vector vector) {
		Set<String> shops = shopData.getParent().getKeys(true);
		for(String s : shops) {
			if(s.contains("ShopLocation") && shopData.getVector(s).equals(vector)) { return false; }
		}
		
		shopData.set("ShopLocation", vector);
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	/**
	 * Retrieves the location of the current repository 
	 * 
	 * @return The shop location.
	 */
	public Location[] getRepositoryLocation() {
		String[] path = shopData.getCurrentPath().split("\\.");
		
		World w = Bukkit.getServer().getWorld(path[path.length - 2]);
		Vector vl = shopData.getVector("RepositoryLocation.Left");
		Vector vr = shopData.getVector("RepositoryLocation.Right");
		
		Location[] repo = {new Location(w, vl.getX(), vl.getY(), vl.getZ()), new Location(w, vr.getX(), vr.getY(), vr.getZ())};
		return repo;
	}
	
	/**
	 * Sets the location of the current repository
	 * 
	 * @param location The location to set.
	 */
	protected void setRepositoryLocation(Vector vectorLeft, Vector vectorRight) {
		shopData.set("RepositoryLocation.Left", vectorLeft);
		shopData.set("RepositoryLocation.Right", vectorRight);
		TradeShop.dataStore.saveConfig();
	}
	
	/**
	 * Retrieves whether or not the shop has been configured.
	 * A shop is considered non existent if it has no assigned owner.
	 * 
	 * @return True if the shop exists
	 */
	public boolean exists() {
		return (shopData.getVector("ShopLocation") != null || shopData.getVector("RepositoryLocation.Left") != null);
	}

	/**
	 * Removes a shop from the data storage.
	 * 
	 * @return False if the shop did not exist.
	 */
	protected boolean remove() {
		if(!exists()) { return false; }
		shopData.getParent().set(getOwner(), null);
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	/**
	 * Adds a new trade to the shop.
	 * 
	 * @param slot The chest slot number where the sell item is located.
	 * @param newTrade The trade that occupies this slot number.
	 * @return False if a trade already existed in that slot.
	 */
	protected boolean addTrade(int slot, Trade newTrade) {
		if(shopData.getString(String.valueOf(slot)) != null) {
			return false;
		}
		
		ConfigurationSection trade = shopData.getConfigurationSection(String.valueOf(slot));
		
		trade.set("Buy1", newTrade.getBuyItem(0));
		trade.set("Buy2", newTrade.getBuyItem(1));
		trade.set("Sell", newTrade.getSellItem());
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	/**
	 * Adds a new trade to the shop
	 * 
	 * @param newTrade The trade to be added to the shop
	 * @return The slot number the trade was assigned.
	 */
	protected int addTrade(Trade newTrade) {
		Set<String> keys = shopData.getKeys(false);
		int openSlot = -1;
		
		// Find the first empty slot
		for(int i = 0; i < 9; i++) {
			if(keys.contains(String.valueOf(i))) { continue; }
			openSlot = i;
			break;
		}
			
		if(openSlot != -1) { addTrade(openSlot, newTrade); }
		return openSlot;
	}
	
	/**
	 * Retrieves a trade.
	 * 
	 * @param slot The slot for which to retrieve a trade.
	 * @return The trade requested. 
	 */
	public Trade getTrade(int slot) {
		ConfigurationSection trade = shopData.getConfigurationSection(String.valueOf(slot)); 
		return new Trade(trade.getItemStack("Buy1"), trade.getItemStack("Buy2"), trade.getItemStack("Sell"));
	}
	
	/**
	 * Retrieves all trades in the shop
	 * 
	 * @return An ordered list of trades in the shop by slot number. Size will always be 9, trade will be null if it does not exist.
	 */
	public Trade[] getTrades() {
		Trade[] allTrades = new Trade[27];
		
		for(int trade = 0; trade < 27; trade++) {
			if(shopData.getKeys(false).contains(String.valueOf(trade))) {
				ConfigurationSection tradeData = shopData.getConfigurationSection(String.valueOf(trade));
				allTrades[trade] =new Trade(tradeData.getItemStack("Buy1"), tradeData.getItemStack("Buy2"), tradeData.getItemStack("Sell"));
			} else {
				//Make sure we fill every slot.
				Trade newTrade = null;
				allTrades[trade] = (newTrade);
			}
		}
		return allTrades;
	}
	
	/**
	 * Removes a trade based on its slot number
	 * 
	 * @param slot The chest slot ID of the trade item.
	 */
	protected void removeTrade(int slot) {
		shopData.set(String.valueOf(slot), null);
		TradeShop.dataStore.saveConfig();
	}
}
