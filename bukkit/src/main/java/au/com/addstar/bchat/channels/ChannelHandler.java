package au.com.addstar.bchat.channels;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.google.common.collect.Lists;

import au.com.addstar.bchat.ChatFormatter;
import au.com.addstar.bchat.Debugger;
import au.com.addstar.bchat.events.ChannelChatEvent;
import au.com.addstar.bchat.events.ChannelReceiveEvent;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.BroadcastPacket;
import au.com.addstar.bchat.packets.PostFormatBroadcastPacket;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Handles sending and broadcasting messages for channels
 */
public class ChannelHandler {
	private final ChatChannelManager manager;
	private final Channel<BasePacket> pipe;
	private final ChatFormatter formatter;
	private final Highlighter highlighter;
	
	private ThreadLocal<Boolean> enableConsoleEcho;
	
	public ChannelHandler(ChatChannelManager manager, Channel<BasePacket> pipe, ChatFormatter formatter, Highlighter highlighter) {
		this.manager = manager;
		this.pipe = pipe;
		this.formatter = formatter;
		this.highlighter = highlighter;
		
		enableConsoleEcho = ThreadLocal.withInitial(() -> true);
	}
	
	/**
	 * Enables console echo on broadcast 
	 * for this thread
	 */
	public void enableConsoleEcho() {
		enableConsoleEcho.set(true);
	}
	
	/**
	 * Disables console echo on broadcast
	 * for this thread
	 */
	public void disableConsoleEcho() {
		enableConsoleEcho.set(false);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param highlighted The highlighted version of {@code message}.
	 *                This may be null if not required
	 * @param channel The channel to send on
	 */
	public void send(BaseComponent[] message, BaseComponent[] highlighted, ChatChannel channel) {
		send(message, highlighted, channel, null);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param highlighted This is a highlighted version of {@code message}.
	 *                This may be null if not required
	 * @param channel The channel to send on
	 */
	public void send(String message, String highlighted, ChatChannel channel) {
		send(TextComponent.fromLegacyText(message), (highlighted != null ? TextComponent.fromLegacyText(highlighted) : null), channel, null);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method ignores scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param highlighted This is a highlighted version of {@code message}.
	 *                This may be null if not required
	 * @param channel The channel to send on
	 * @param sourceWorld The source world for this message
	 */
	public void send(String message, String highlighted, ChatChannel channel, World sourceWorld) {
		send(TextComponent.fromLegacyText(message), (highlighted != null ? TextComponent.fromLegacyText(highlighted) : null), channel, sourceWorld);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all receivers of
	 * the channel. This method obeys world scope
	 * @param message The message to send. No further formatting will be
	 *                done to this message
	 * @param highlighted This is a highlighted version of {@code message}.
	 *                This may be null if not required
	 * @param channel The channel to send on
	 * @param sourceWorld The source world for this message
	 */
	public void send(BaseComponent[] message, BaseComponent[] highlighted, ChatChannel channel, World sourceWorld) {
		// TODO: Keyword Highlighting
		// Handle it locally
		broadcastLocal(message, highlighted, channel, sourceWorld);
		
		// Broadcast if needed
		if (channel.getScope() == ChannelScope.GLOBAL) {
			pipe.broadcast(new BroadcastPacket(channel, message, highlighted));
		}
	}
	
	/**
	 * Sends a raw message (previously formatted) to all non local receivers
	 * of the channel.
	 * @param message The message to send
	 * @param channel The channel to send on
	 */
	public void sendRemoteOnly(String message, ChatChannel channel) {
		sendRemoteOnly(TextComponent.fromLegacyText(message), channel);
	}
	
	/**
	 * Sends a raw message (previously formatted) to all non local receivers
	 * of the channel.
	 * @param message The message to send
	 * @param channel The channel to send on
	 */
	public void sendRemoteOnly(BaseComponent[] message, ChatChannel channel) {
		pipe.broadcast(new BroadcastPacket(channel, message, null));
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
		
		ChannelChatEvent event = new ChannelChatEvent(sender, channel, message);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		message = event.getMessage();
		
		// TODO: source world handling
		// Handle post formatting channels differently
		if (channel instanceof PostFormattedChatChannel) {
			if (sender instanceof Player) {
				GlobalPlayer player = Global.getPlayer(((Player)sender).getUniqueId());
				sendPostFormatted(message, (PostFormattedChatChannel)channel, player);
			} else {
				throw new UnsupportedOperationException();
			}
			return;
		}
		
		String formatted = formatMessage(message, format, channel, sender);
		String highlighted = null;
		if (channel.getUseHighlighter()) {
			// Determine the intial color for the message
			String initialColor;
			int messagePos = format.indexOf("{MESSAGE}");
			if (messagePos == -1) {
				initialColor = ChatColor.RESET.toString();
			} else {
				initialColor = org.bukkit.ChatColor.getLastColors(format.substring(0, messagePos));
				if (initialColor.isEmpty()) {
					initialColor = ChatColor.RESET.toString();
				}
			}
			
			String highlightedMessage = highlighter.highlight(message, initialColor);
			if (highlightedMessage != message) {
				highlighted = formatMessage(highlightedMessage, format, channel, sender);
			}
		}
		send(formatted, highlighted, channel);
	}
	
	private String formatMessage(String message, String format, ChatChannel channel, CommandSender sender) {
		if (sender instanceof Player) {
			GlobalPlayer player = Global.getPlayer(((Player)sender).getUniqueId());
			if (channel instanceof TemporaryChatChannel) {
				return formatter.formatIn(message, format, (TemporaryChatChannel)channel, player);
			} else {
				return formatter.format(message, format, player);
			}
		} else {
			if (channel instanceof TemporaryChatChannel) {
				return formatter.formatConsoleIn(message, format, (TemporaryChatChannel)channel, sender.getName());
			} else {
				return formatter.formatConsole(message, format, sender.getName());
			}
		}
	}
	
	private void sendPostFormatted(String message, PostFormattedChatChannel channel, GlobalPlayer sender) {
		// Handle it locally
		broadcastLocalFormat(message, sender, channel, null);
		
		// Broadcast if needed
		if (channel.getScope() == ChannelScope.GLOBAL) {
			pipe.broadcast(new PostFormatBroadcastPacket(channel, message, sender, false));
		}
	}
	
	void handleIncomming(BroadcastPacket packet) {
		ChatChannel channel = manager.getChannel(packet.channelId);
		if (channel == null) {
			Debugger.getLogger(Debugger.Packet).warning("Invalid channel " + packet.channelId + " in packet");
			return;
		}
		
		broadcastLocal(packet.message, packet.highlightedMessage, channel, null);
	}
	
	void handleIncomming(PostFormatBroadcastPacket packet) {
		if (packet.isHighlightChannel) {
			return; // TODO: Keyword highlighting
		}
		
		ChatChannel channel = manager.getChannel(packet.channelId);
		if (channel == null) {
			Debugger.getLogger(Debugger.Packet).warning("Invalid channel " + packet.channelId + " in packet");
			return;
		}
		
		if (!(channel instanceof PostFormattedChatChannel)) {
			Debugger.getLogger(Debugger.Packet).warning("Invalid packet used for channel broadcast. Channel not post format channel: " + packet.channelId);
			return;
		}
		
		GlobalPlayer sender = Global.getPlayer(packet.sender);
		if (sender == null) {
			Debugger.getLogger(Debugger.Packet).warning("Sender was not valid for packet " + packet.channelId + ": " + packet.sender);
			return;
		}
		
		broadcastLocalFormat(packet.message, sender, (PostFormattedChatChannel)channel, null);
	}
	
	/*
	 * Broadcast a message to all players that are allowed to hear it
	 */
	private void broadcastLocal(BaseComponent[] message, BaseComponent[] highlighted, ChatChannel channel, World sourceWorld) {
		List<CommandSender> receivers = Lists.newArrayList();
		for (Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_USERS)) {
			if (permissible instanceof CommandSender) {
				CommandSender sender = (CommandSender)permissible;
				
				// Disable console echo
				if (sender == Bukkit.getConsoleSender() && !enableConsoleEcho.get()) {
					continue;
				}
				
				if (!canSee(sender, channel, sourceWorld)) {
					continue;
				}
				
				receivers.add(sender);
			}
		}
		
		ChannelReceiveEvent event = new ChannelReceiveEvent(channel, message, highlighted, receivers);
		Bukkit.getPluginManager().callEvent(event);
		
		receivers = event.getReceivers();
		
		// Now send it out
		for (CommandSender sender : receivers) {
			BaseComponent[] toSend;
			if (highlighted != null && sender.hasPermission("bungeechat.see.highlighted")) {
				toSend = highlighted;
			} else {
				toSend = message;
			}
			
			if (sender instanceof Player) {
				((Player)sender).spigot().sendMessage(toSend);
			} else {
				sender.sendMessage(TextComponent.toLegacyText(toSend));
			}
		}
	}
	
	/*
	 * Broadcast a message to all players that are allowed to hear it while formatting
	 */
	private void broadcastLocalFormat(String message, GlobalPlayer sender, PostFormattedChatChannel channel, World sourceWorld) {
		for (Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_USERS)) {
			if (permissible instanceof CommandSender) {
				CommandSender cs = (CommandSender)permissible;
				
				// Disable console echo
				if (cs == Bukkit.getConsoleSender() && !enableConsoleEcho.get()) {
					continue;
				}
				
				if (!canSee(cs, channel, sourceWorld)) {
					continue;
				}
				
				// Format this message specifically for this player
				GlobalPlayer listener = null;
				if (cs instanceof Player) {
					listener = Global.getPlayer(((Player)cs).getUniqueId());
				}
				
				String format = channel.getFormat(sender, listener);
				if (channel instanceof DMChatChannel) {
					format = formatter.formatDM(message, format, sender, ((DMChatChannel)channel).getTarget(sender));
				} else {
					format = formatter.format(message, format, sender);
				}
				
				cs.sendMessage(format);
			}
		}
	}
	
	private boolean canSee(CommandSender sender, ChatChannel channel, World sourceWorld) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			// Check world scope
			if (channel.getScope() == ChannelScope.WORLD && sourceWorld != null) {
				if (player.getWorld() != sourceWorld) {
					return false;
				}
			}
			
			// Check specialized DM settings 
			if (channel instanceof DMChatChannel) {
				DMChatChannel dm = (DMChatChannel)channel;
				if (!dm.isParticipant(player.getUniqueId())) {
					return false;
				}
			}
			
			GlobalPlayer gplayer = Global.getPlayer(player.getUniqueId());
			if (channel.getSubscribers().contains(gplayer)) {
				// Listen permission is irrelevant
				return true;
			}
		}
		
		// Check listen permission
		if (channel.getListenPermission().isPresent()) {
			if (!sender.hasPermission(channel.getListenPermission().get())) {
				return false;
			}
		}
		
		return true;
	}
	
	public ChatFormatter getFormatter() {
		return formatter;
	}
}
