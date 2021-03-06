package au.com.addstar.bchat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChannelScope;
import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.CommandChatChannel;
import au.com.addstar.bchat.channels.FormattedChatChannel;
import au.com.addstar.bchat.channels.TemporaryChatChannel;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;

public class ChatListener implements Listener {
	private final ChatChannelManager manager;
	private final ChannelHandler handler;
	
	public ChatListener(ChatChannelManager manager, ChannelHandler handler) {
		this.manager = manager;
		this.handler = handler;
	}
	
	private ChatChannel getOutputChannel(GlobalPlayer player, String world) {
		StateAttachment state = player.getAttachment(StateAttachment.class);
		ChatChannel channel = null;
		if (state != null && state.getOutputChannel() != null) {
			channel = manager.getChannel(state.getOutputChannel());
		}
		
		if (channel == null) {
			channel = manager.getDefaultChannel(Global.getServer(), world);
		}
		
		return channel;
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onChatPre(AsyncPlayerChatEvent event) {
		GlobalPlayer sender = Global.getPlayer(event.getPlayer().getUniqueId());
		ChatChannel channel = getOutputChannel(sender, event.getPlayer().getWorld().getName());
		
		event.setMessage(ChatColorizer.colorizeWithPermission(event.getMessage(), event.getPlayer()).trim());
		
		// Update chat formatting for other plugins
		if (channel instanceof FormattedChatChannel) {
			String format;
			format = ((FormattedChatChannel)channel).getFormat();
			if (channel instanceof TemporaryChatChannel) {
				format = handler.getFormatter().formatIn(event.getMessage(), format, (TemporaryChatChannel)channel, sender);
			} else {
				format = handler.getFormatter().format(event.getMessage(), format, sender);
			}
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
		// Eliminate all remaining receivers so bungeechat handles chat completely
		event.getRecipients().clear();
		
		// Send out chat
		handler.disableConsoleEcho();
		
		GlobalPlayer sender = Global.getPlayer(event.getPlayer().getUniqueId());
		ChatChannel channel = getOutputChannel(sender, event.getPlayer().getWorld().getName());
		if (channel instanceof FormattedChatChannel) {
			handler.sendFormat(event.getMessage(), (FormattedChatChannel)channel, event.getPlayer());
		} else {
			handler.send(event.getMessage(), null, channel);
		}
		
		handler.enableConsoleEcho();
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
		
		message = ChatColorizer.colorizeWithPermission(message, sender);
		message = message.trim();
		
		if (ChatColor.stripColor(message).trim().isEmpty()) {
			// Absorb but dont broadcast
			return true;
		}
		
		handler.sendFormat(message, channel, sender);
		return true;
	}
}
