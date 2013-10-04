package alshain01.TradeShop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Self-cleaning class for storing pending commands
 */
class PlayerCommand extends BukkitRunnable {
	CommandAction action;
	Player player;
	Trade trade;
	
	/**
	 * Enumeration for describing the types 
	 * of commands a player can queue. 
	 */
	protected enum CommandAction {
		CREATE, ADD;
	}
	
	/**
	 * Public due to override requirement.
	 * Use constructor, do not run directly.
	 */
	@Override
	public void run() {
		player.sendMessage(Message.CommandTimeout.get());
		TradeShop.instance.commandQueue.remove(player.getName());
	}
	
	/**
	 * Initiates a new create command timer.
	 * 
	 * @param instance The plug-in making the request
	 * @param player The player to remove at a later time
	 * @param action The command list to remove the player from.
	 */	
	protected PlayerCommand(Plugin instance, Player player) {
		this.action = CommandAction.CREATE;
		this.player = player;
		this.trade = null;
		this.runTaskLaterAsynchronously(instance, TradeShop.instance.getConfig().getConfigurationSection("TradeShop").getLong("CommandTimeout"));
	}
	
	/**
	 * Initiates a new add command timer.
	 * 
	 * @param instance The plug-in making the request
	 * @param player The player to remove at a later time
	 * @param action The command list to remove the player from.
	 * @param trade The trade data being created.
	 */	
	protected PlayerCommand(Plugin instance, Player player, Trade trade) {
		this.action = CommandAction.ADD;
		this.player = player;
		this.trade = trade;
		this.runTaskLaterAsynchronously(instance, TradeShop.instance.getConfig().getConfigurationSection("TradeShop").getLong("CommandTimeout"));
	}
	
	/**
	 * Gets the action the player has queued
	 * 
	 * @return The command action
	 */
	protected CommandAction getAction() {
		return action;
	}
	
	/**
	 * Gets the trade the player has queued
	 * 
	 * @return The trade, null if not adding
	 */
	protected Trade getTrade() {
		return trade;
	}
	
	/**
	 * Removes a player command that has been processed
	 * before the timeout occurs.
	 */
	protected void remove() {
		this.cancel();
		TradeShop.instance.commandQueue.remove(player.getName());
	}
}