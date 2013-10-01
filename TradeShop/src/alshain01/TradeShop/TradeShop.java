package alshain01.TradeShop;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import alshain01.Flags.Flags;


public class TradeShop extends JavaPlugin {
	public static TradeShop instance;
	protected CustomYML shopData = new CustomYML(this, "data.yml");
	protected CustomYML messageReader = new CustomYML(this, "message.yml");
	
	//TODO: Create an async task to remove players from the queues after a timeout.
	protected ConcurrentHashMap<Player, PlayerCommand> commandQueue = new ConcurrentHashMap<Player, PlayerCommand>();
	
	// Keep tabs on the timers so we can kill them cleanly if necessary.
	//protected Set<PlayerCommand> activeTimers = new HashSet<PlayerCommand>();
	//protected ConcurrentHashMap<Player, Trade> addQueue = new ConcurrentHashMap<Player, Trade>();	
	//protected ConcurrentHashMap<Player, Long> listQueue = new ConcurrentHashMap<Player, Long>();
	
	protected boolean flags = false;
		
	@Override
	public void onEnable() {
		instance = this;
		flags = Bukkit.getServer().getPluginManager().isPluginEnabled("Flags");
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
				
				// Add the creation to the queue
				sender.sendMessage(Message.CreateMode.get());
				commandQueue.put(player, new PlayerCommand(this, player));
				return true;
				
			} else if (args[0].equalsIgnoreCase("add")) {
				
				// Add the trade to the queue
				Trade trade = buildTrade((Player)sender, args);
				if (trade != null) {
					sender.sendMessage(Message.ModifyMode.get());
					commandQueue.put(player, new PlayerCommand(this, player, trade));
				}
				return true;
			} 
		}
		return false;
	}
	
	/*
	 * Builds a trade from arguments
	 * @return Null if the trade could not be built (player is notified)
	 */
	private Trade buildTrade(Player player, String[] args) {
		// Check the argument formatting
		if(args.length != 3 || args.length != 5) {
			player.sendMessage(getHelp("add"));
			return null;
		}
		
		// Parse the arguments
		// Get the materials
		Material buyItem1 = Material.getMaterial(args[3]);
		if (buyItem1 == null) {
			player.sendMessage(Message.InvalidMaterialError.get()
					.replaceAll("\\{Material\\}", args[3]));
			return null;
		}
		
		Material buyItem2 = null;
		if(args.length == 5) { 
			buyItem2 = Material.getMaterial(args[5]);
			if(buyItem2 == null) {
				player.sendMessage(Message.InvalidMaterialError.get()
						.replaceAll("\\{Material\\}", args[5]));
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
	private String getHelp(String action) {
		if(action.equalsIgnoreCase("add")) { return "/tradeshop add <SellMaterial> <SellQuantity> <BuyMaterial> <BuyQuantity> [BuyMaterial] [BuyQuantity]"; }
		else { return "/tradeshop remove <id>"; }
	}
}
