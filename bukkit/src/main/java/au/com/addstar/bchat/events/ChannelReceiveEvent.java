package au.com.addstar.bchat.events;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import au.com.addstar.bchat.channels.ChatChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * This event is fired when a message is received to be
 * displayed. This is called for local and remote 
 * messages.
 * This is not called for post formatted chat channels
 */
public class ChannelReceiveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final ChatChannel channel;
	private final List<CommandSender> receivers;
	private final BaseComponent[] message;
	private final BaseComponent[] highlighted;
	
	public ChannelReceiveEvent(ChatChannel channel, BaseComponent[] message, BaseComponent[] highlighted, List<CommandSender> receivers) {
		this.channel = channel;
		this.message = message;
		this.highlighted = highlighted;
		this.receivers = receivers;
	}
	
	/**
	 * Gets the channel this was sent on
	 * @return The ChatChannel
	 */
	public ChatChannel getChannel() {
		return channel;
	}
	
	/**
	 * Gets the final message that was sent
	 * @return The message as BaseComponents
	 */
	public BaseComponent[] getMessage() {
		return message;
	}
	
	/**
	 * Gets the final message that was sent,
	 * as a string.
	 * @return The message as String
	 */
	public String getMessageString() {
		return TextComponent.toLegacyText(message);
	}
	
	/**
	 * Gets the highlighted version of the message.
	 * @return The message as BaseComponents
	 */
	public BaseComponent[] getHighlightMessage() {
		return highlighted;
	}
	
	/**
	 * Gets the highlighted version of the message
	 * as a string.
	 * @return The message as String
	 */
	public String getHighlightMessageString() {
		return TextComponent.toLegacyText(highlighted);
	}
	
	/**
	 * Gets a list of the local receivers of this
	 * message. This list is modifiable so you can
	 * alter who receives it.
	 * As for which message is seen (highlighted vs
	 * normal) depends on the permission 
	 * {@code bungeechat.see.highlighted}
	 * @return The modifiable list of receivers
	 */
	public List<CommandSender> getReceivers() {
		return receivers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
