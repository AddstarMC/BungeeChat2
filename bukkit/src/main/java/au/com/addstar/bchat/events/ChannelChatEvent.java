package au.com.addstar.bchat.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.google.common.base.Preconditions;

import au.com.addstar.bchat.channels.ChatChannel;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;

/**
 * Called when a somebody talks in a chat channel.
 * This could be a player, or a console.
 * 
 * This is only called when local players (senders)
 * chat in a channel, not remote. 
 * @see ChannelReceiveEvent 
 */
public class ChannelChatEvent extends Event implements Cancellable {
	private final static HandlerList handlers = new HandlerList();
	
	private final CommandSender sender;
	private final ChatChannel channel;
	private String message;
	
	private boolean isCancelled;
	
	public ChannelChatEvent(CommandSender sender, ChatChannel channel, String message) {
		this.sender = sender;
		this.channel = channel;
		this.message = message;
	}
	
	/**
	 * Gets the sender of this message
	 * @return The CommandSender
	 */
	public final CommandSender getSender() {
		return sender;
	}
	
	/**
	 * Gets a GlobalPlayer object for the sender
	 * if it is a player.
	 * @return The GlobalPlayer instance, or null
	 */
	public GlobalPlayer getGlobalPlayer() {
		if (sender instanceof Player) {
			return Global.getPlayer(((Player)sender).getUniqueId());
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the channel the message is being sent on
	 * @return The channel
	 */
	public final ChatChannel getChannel() {
		return channel;
	}
	
	/**
	 * Gets the message being sent
	 * @return The message
	 */
	public final String getMessage() {
		return message;
	}
	
	/**
	 * Sets the message to send
	 * @param message The message. Cannot be null
	 */
	public void setMessage(String message) {
		Preconditions.checkNotNull(message);
		this.message = message;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
