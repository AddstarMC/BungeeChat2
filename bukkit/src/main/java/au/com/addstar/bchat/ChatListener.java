package au.com.addstar.bchat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChannelScope;
import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.CommandChatChannel;
import au.com.addstar.bchat.channels.FormattedChatChannel;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;

public class ChatListener implements Listener {
	private final ChatChannelManager manager;
	private final ChannelHandler handler;
	
	public ChatListener(ChatChannelManager manager, ChannelHandler handler) {
		this.manager = manager;
		this.handler = handler;
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onChatPre(AsyncPlayerChatEvent event) {
		ChatChannel channel = manager.getDefaultChannel(Global.getServer(), event.getPlayer().getWorld().getName());
		GlobalPlayer sender = Global.getPlayer(event.getPlayer().getUniqueId());
		
		// TODO: Chat color formatting
		
		// Update chat formatting for other plugins
		if (channel instanceof FormattedChatChannel) {
			String format;
			format = ((FormattedChatChannel)channel).getFormat();
			format = handler.getFormatter().format(event.getMessage(), format, sender);
			event.setFormat(format);
		}
		
		// Reduce scope if needed
		if (channel.getScope() == ChannelScope.WORLD) {
			event.getRecipients().removeIf((p) -> {
				return p.getWorld() != event.getPlayer().getWorld();
			});
		}
		
		// Remove those not permitted to see the chat
		if (channel.getListenPermission().isPresent()) {
			event.getRecipients().removeIf((p) -> {
				if (p == event.getPlayer()) {
					// Sender is excluded, they should always see their chat
					return false;
				} else {
					return !p.hasPermission(channel.getListenPermission().get());
				}
			});
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onChatPost(AsyncPlayerChatEvent event) {
		ChatChannel channel = manager.getDefaultChannel(Global.getServer(), event.getPlayer().getWorld().getName());
		GlobalPlayer sender = Global.getPlayer(event.getPlayer().getUniqueId());
		
		// Get the final message to display
		String finalMessage;
		if (channel instanceof FormattedChatChannel) {
			finalMessage = ((FormattedChatChannel)channel).getFormat();
			finalMessage = handler.getFormatter().format(event.getMessage(), finalMessage, sender);
		} else {
			finalMessage = String.format(event.getFormat(), sender.getDisplayName(), event.getMessage());
		}
		
		// Send if needed
		if (channel.getScope() == ChannelScope.GLOBAL) {
			handler.sendRemoteOnly(finalMessage, channel);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		String commandString = event.getMessage();
		String[] parts = commandString.split(" ", 2);
		
		if (parts.length != 2) {
			return;
		}
		
		if (onCommand(event.getPlayer(), parts[0].substring(1), parts[1])) {
			event.setMessage("/bungeechat null");
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onServerCommand(ServerCommandEvent event) {
		String commandString = event.getCommand();
		String[] parts = commandString.split(" ", 2);
		
		if (parts.length != 2) {
			return;
		}
		
		if (onCommand(event.getSender(), parts[0], parts[1])) {
			event.setCommand("bungeechat null");
		}
	}
	
	private boolean onCommand(CommandSender sender, String command, String message) {
		CommandChatChannel channel = manager.getChannelForCommand(command);
		if (channel == null) {
			return false;
		}
		
		// Do a perm check
		if (channel.getCommandPermission().isPresent()) {
			if (!sender.hasPermission(channel.getCommandPermission().get())) {
				return false;
			}
		}
		
		// TODO: chat colour code formatting
		message = message.trim();
		
		if (ChatColor.stripColor(message).trim().isEmpty()) {
			// Absorb but dont broadcast
			return true;
		}
		
		handler.sendFormat(message, channel, sender);
		return true;
	}
}
