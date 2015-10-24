package au.com.addstar.bchat.channels;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.bchat.ChatFormatter;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.BroadcastPacket;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Handles sending and broadcasting messages for channels
 */
public class ChannelHandler {
	private final ChatChannelManager manager;
	private final Channel<BasePacket> pipe;
	private final ChatFormatter formatter;
	
	public ChannelHandler(ChatChannelManager manager, Channel<BasePacket> pipe, ChatFormatter formatter) {
		this.manager = manager;
		this.pipe = pipe;
		this.formatter = formatter;
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param channel The channel to send on
	 */
	public void send(BaseComponent[] message, ChatChannel channel) {
		send(message, channel, null);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param channel The channel to send on
	 */
	public void send(String message, ChatChannel channel) {
		send(TextComponent.fromLegacyText(message), channel, null);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param channel The channel to send on
	 * @param sourceWorld The source world for this message
	 */
	public void send(String message, ChatChannel channel, World sourceWorld) {
		send(TextComponent.fromLegacyText(message), channel, sourceWorld);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method obeys world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param channel The channel to send on
	 * @param sourceWorld The source world for this message
	 */
	public void send(BaseComponent[] message, ChatChannel channel, World sourceWorld) {
		// TODO: Keyword Highlighting
		// Handle it locally
		broadcastLocal(message, channel, sourceWorld);
		
		// Broadcast if needed
		if (channel.getScope() == ChannelScope.GLOBAL) {
			pipe.broadcast(new BroadcastPacket(channel, message, false));
		}
	}
	
	/**
	 * Sends a message to all receivers of the channel.
	 * This message will be formatted in accordance with
	 * the defined format on the channel.
	 * @param message The unformatted message to send
	 * @param channel The channel to send on
	 * @param sender The sender of this message
	 */
	public void sendFormat(String message, FormattedChatChannel channel, CommandSender sender) {
		String format = channel.getFormat();
		
		if (sender instanceof Player) {
			GlobalPlayer player = Global.getPlayer(((Player)sender).getUniqueId());
			if (channel instanceof TemporaryChatChannel) {
				message = formatter.formatIn(message, format, (TemporaryChatChannel)channel, player);
			} else {
				message = formatter.format(message, format, player);
			}
		} else {
			if (channel instanceof TemporaryChatChannel) {
				message = formatter.formatConsoleIn(message, format, (TemporaryChatChannel)channel, sender.getName());
			} else {
				message = formatter.formatConsole(message, format, sender.getName());
			}
		}
		
		send(message, channel);
	}
	
	void handleIncomming(BroadcastPacket packet) {
		if (packet.isHighlightChannel) {
			return; // TODO: Keyword highlighting
		}
		
		ChatChannel channel = manager.getChannel(packet.channelId);
		if (channel == null) {
			return; // TODO: debug log
		}
		
		broadcastLocal(packet.message, channel, null);
	}
	
	/*
	 * Broadcast a message to all players that are allowed to hear it
	 */
	private void broadcastLocal(BaseComponent[] message, ChatChannel channel, World sourceWorld) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			// Check world scope
			if (channel.getScope() == ChannelScope.WORLD && sourceWorld != null) {
				if (player.getWorld() != sourceWorld) {
					continue;
				}
			}
			
			// Check listen permission
			if (channel.getListenPermission().isPresent()) {
				if (!player.hasPermission(channel.getListenPermission().get())) {
					continue;
				}
			}
			
			player.spigot().sendMessage(message);
		}
	}
}
