package alshain01.TradeShop;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import alshain01.Flags.Flags;


public class TradeShop extends JavaPlugin {
	public static TradeShop instance;
	protected static CustomYML messageReader = new CustomYML(Bukkit.getServer().getPluginManager().getPlugin("TradeShop"), "message.yml");
	protected static CustomYML dataStore = new CustomYML(Bukkit.getServer().getPluginManager().getPlugin("TradeShop"), "data.yml");

	protected static Set<String> adminMode = new HashSet<String>(); 
	//protected ConcurrentHashMap<Location, Shop> shopData = new ConcurrentHashMap<Location, Shop>();
	protected static ConcurrentHashMap<String, PlayerCommand> commandQueue = new ConcurrentHashMap<String, PlayerCommand>();

	protected static boolean flags = false;
		
	@Override
	public void onEnable() {
		//instance = this;
		messageReader.saveDefaultConfig();
		flags = this.getServer().getPluginManager().isPluginEnabled("Flags");
		if(flags) {
			String plugin = this.getName();
			Flags.instance.getRegistrar().register("TSAllowCreate", 
					"Allows the creation of a " + plugin + " shop.", false, plugin,
					"\\{Player\\} You are not allowed to create a " + plugin + "in \\{Owner\\}''s \\{AreaType\\}.",
					"\\{Player\\} You are not allowed to create a " + plugin + "in \\{World\\}");
			Flags.instance.getRegistrar().register("TSAllowTrade", 
	                "Allows players to use a " + plugin + "shop.", true, plugin,
	                "\\{Player\\} You are not allowed to use a " + plugin + "in \\{Owner\\}''s \\{AreaType\\}.",
					"\\{Player\\} You are not allowed to use a " + plugin + "in \\{World\\}");
		}
		
		this.getServer().getPluginManager().registerEvents(new ShopManager(), this);
	}

	@Override
	public void onDisable() {
		//Kill the active timers cleanly.
		Iterator<Entry<String, PlayerCommand>> it = commandQueue.entrySet().iterator();
		while (it.hasNext()) {
			PlayerCommand cmd = (PlayerCommand)it.next();
			cmd.cancel();
		}
		commandQueue.clear();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Message.NoConsoleError.get());
			return true;
		}
		if(args.length < 1) { return false; }
		Player player = (Player)sender;
		
		if(cmd.getName().equalsIgnoreCase("tradeshop")) {
			if(args[0].equalsIgnoreCase("create")) {
				
				/*
				 *  Add the creation to the queue
				 */
				if(commandQueue.containsKey(player.getName())) { commandQueue.get(player.getName()).remove(); }
				player.sendMessage(Message.CreateMode.get());
				commandQueue.put(player.getName(), new PlayerCommand(this, player));
				return true;
				
			} else if (args[0].equalsIgnoreCase("add")) {
				
				/*
				 *  Add the trade to the queue
				 */
				Trade trade = buildTrade(player, args);
				if (trade != null) {
					
					if(commandQueue.containsKey(player.getName())) { commandQueue.get(player.getName()).remove(); }
					player.sendMessage(Message.AddMode.get());
					commandQueue.put(player.getName(), new PlayerCommand(this, player, trade));
				}
				return true;

			} else if (args[0].equalsIgnoreCase("admin")) {

				/*
				 *  Add or remove a player from admin mode.
				 */
				if(!player.hasPermission("tradeshop.admin")) {
					player.sendMessage(Message.PermError.get());
				} else {
					if(adminMode.contains(player.getName())) {
						player.sendMessage(Message.ExitAdminMode.get());
						adminMode.remove(player.getName());
					} else {
						player.sendMessage(Message.EnterAdminMode.get());
						adminMode.add(player.getName());
					}
				}
				return true;
				
			}
		}
		return false;
	}
	
	/*
	 * Builds a trade from command arguments
	 * @return Null if the trade could not be built (player is notified)
	 */
	private static Trade buildTrade(Player player, String[] args) {
		// Check the argument formatting
		if(args.length != 3 || args.length != 5) {
			player.sendMessage(getHelp("add"));
			return null;
		}
		
		// Parse the arguments
		// Get the materials
		Material buyItem1 = Material.getMaterial(args[1]);
		if (buyItem1 == null) {
			player.sendMessage(Message.InvalidMaterialError.get()
					.replaceAll("\\{Material\\}", args[1]));
			return null;
		}
		
		Material buyItem2 = null;
		if(args.length == 5) { 
			buyItem2 = Material.getMaterial(args[3]);
			if(buyItem2 == null) {
				player.sendMessage(Message.InvalidMaterialError.get()
						.replaceAll("\\{Material\\}", args[3]));
				return null;
			}
		}

		//Get the quantities
		int buyItem1Qty, buyItem2Qty = 0;
		try {
			buyItem1Qty = Integer.valueOf(args[2]);
			if(args.length == 5) {
				buyItem2Qty = Integer.valueOf(args[4]);
			}
		} catch (NumberFormatException ex) {
			player.sendMessage(getHelp("add"));
			return null;
		}
		
		// Return the trade.
		return new Trade(new ItemStack(buyItem1, buyItem1Qty), new ItemStack(buyItem2, buyItem2Qty));
	}
	
	/*
	 * Returns a custom help for individual sub-commands
	 */
	private static String getHelp(String action) {
		if(action.equalsIgnoreCase("add")) { return "/tradeshop add <BuyMaterial> <BuyQuantity> [BuyMaterial] [BuyQuantity]"; }
		else { return "/tradeshop remove <id>"; }
	}
	
	/*
	 * Converts Location.toString() back to Location
	 * This is unguarded against improper strings
	 */
	private Location stringToLocation(String location) {
		String[] elements = location.split(",");
		String[] values = new String[elements.length];
		for(int i=0; i < elements.length; i++) {
			values[i] = elements[i].split("=")[1]; 
		}
		return new Location(this.getServer().getWorld(values[0]), Double.valueOf(values[1]), Double.valueOf(values[2]), Double.valueOf(values[3]), Float.valueOf(values[4]), Float.valueOf(values[5]));
	}
	
	public void Debug(String message) {
		instance.getLogger().info("[DEBUG] " + message);
	}
}
