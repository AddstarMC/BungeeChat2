package au.com.addstar.bchat.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChatChannelTemplate;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.ChatColor;

public class ChannelCommand implements TabExecutor {
	private final ChatChannelManager manager;
	
	public ChannelCommand(ChatChannelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		
		label += " " + args[0];
		switch (args[0].toLowerCase()) {
		case "add":
			onAdd(sender, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "subscribe":
			onSubscribe(sender, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "unsubscribe":
			onUnsubscribe(sender, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "output":
			onOutput(sender, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown sub command " + args[0]);
			break;
		}
		
		return true;
	}
	
	private void onAdd(CommandSender sender, String label, String[] args) {
		// TODO: Add temporary channel
	}

	private void onSubscribe(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("bungeechat.command.channel.subscribe")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to subscribe to channels");
			return;
		}
		
		if (args.length < 1) {
			sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
			return;
		}
		
		int start = 0;
		GlobalPlayer target;
		boolean isSelf;
		// Specify player
		if (args[0].equalsIgnoreCase("-p")) {
			if (!sender.hasPermission("bungeechat.command.channel.subscribe.other")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to subscribe other players to channels");
				return;
			}
			
			// Check available arguments
			if (args.length < 3) {
				sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
				return;
			}
			
			target = Global.getPlayer(args[1]);
			isSelf = false;
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[1]);
				return;
			}
			
			start = 2;
		} else {
			isSelf = true;
			if (sender instanceof Player) {
				target = Global.getPlayer(((Player)sender).getUniqueId());
			} else {
				sender.sendMessage(ChatColor.RED + "You must specify a player with '-p <player>' to use this from console");
				return;
			}
		}
		
		// Get the channel
		ChatChannel channel;
		if (args.length >= start + 2) {
			// Temporary chat channel
			ChatChannelTemplate template = manager.getTemplate(args[start]);
			if (template == null) {
				sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
				return;
			}
			
			channel = manager.getChannel(template, args[start+1]);
			if (channel == null) {
				sender.sendMessage(ChatColor.RED + "Unknown sub channel " + args[start+1]);
				return;
			}
			
			if (template.getJoinPermission().isPresent()) {
				if (!sender.hasPermission(template.getJoinPermission().get())) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to join this channel");
					return;
				}
			}
		} else {
			channel = manager.getChannel(args[start]);
			if (channel == null) {
				sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
				return;
			}
		}
		
		if (!channel.allowSubscriptions()) {
			sender.sendMessage(ChatColor.RED + "This channel does not allow subscriptions");
			return;
		}
		
		// Do the subscription
		channel.addSubscriber(target);
		if (isSelf) {
			sender.sendMessage(ChatColor.GREEN + "You are now subscribed to that channel");
		} else {
			sender.sendMessage(ChatColor.GREEN + target.getDisplayName() + " is now subscribed to that channel");
		}
		return;
	}
	
	private void onUnsubscribe(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("bungeechat.command.channel.subscribe")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to unsubscribe to channels");
			return;
		}
		
		if (args.length < 1) {
			sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
			return;
		}
		
		int start = 0;
		GlobalPlayer target;
		boolean isSelf;
		// Specify player
		if (args[0].equalsIgnoreCase("-p")) {
			if (!sender.hasPermission("bungeechat.command.channel.subscribe.other")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to unsubscribe other players to channels");
				return;
			}
			
			// Check available arguments
			if (args.length < 3) {
				sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
				return;
			}
			
			target = Global.getPlayer(args[1]);
			isSelf = false;
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[1]);
				return;
			}
			
			start = 2;
		} else {
			isSelf = true;
			if (sender instanceof Player) {
				target = Global.getPlayer(((Player)sender).getUniqueId());
			} else {
				sender.sendMessage(ChatColor.RED + "You must specify a player with '-p <player>' to use this from console");
				return;
			}
		}
		
		// Get the channel
		ChatChannel channel;
		if (args.length >= start + 2) {
			// Temporary chat channel+
			ChatChannelTemplate template = manager.getTemplate(args[start]);
			if (template == null) {
				sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
				return;
			}
			
			channel = manager.getChannel(template, args[start+1]);
			if (channel == null) {
				sender.sendMessage(ChatColor.RED + "Unknown sub channel " + args[start+1]);
				return;
			}
		} else {
			channel = manager.getChannel(args[start]);
			if (channel == null) {
				sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
				return;
			}
		}
		
		// Do the subscription
		if (channel.removeSubscriber(target)) {
			if (isSelf) {
				sender.sendMessage(ChatColor.GREEN + "You were unsubscribed from that channel");
			} else {
				sender.sendMessage(ChatColor.GREEN + target.getDisplayName() + " was unsubscribed from that channel");
			}
		} else {
			if (isSelf) {
				sender.sendMessage(ChatColor.GOLD + "You were already not subscribed to that channel");
			} else {
				sender.sendMessage(ChatColor.GOLD + target.getDisplayName() + " was already not subscribed to that channel");
			}
		}
		return;
	}
	
	private void onOutput(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("bungeechat.command.channel.output")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to set output channels");
			return;
		}
		
		if (args.length < 1) {
			sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
			return;
		}
		
		int start = 0;
		GlobalPlayer target;
		boolean isSelf;
		// Specify player
		if (args[0].equalsIgnoreCase("-p")) {
			if (!sender.hasPermission("bungeechat.command.channel.output.other")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to set the output channel of others");
				return;
			}
			
			// Check available arguments
			if (args.length < 3) {
				sender.sendMessage("Usage: /" + label + " [-p <player>] <channel> [<subchannel>]");
				return;
			}
			
			target = Global.getPlayer(args[1]);
			isSelf = false;
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[1]);
				return;
			}
			
			start = 2;
		} else {
			isSelf = true;
			if (sender instanceof Player) {
				target = Global.getPlayer(((Player)sender).getUniqueId());
			} else {
				sender.sendMessage(ChatColor.RED + "You must specify a player with '-p <player>' to use this from console");
				return;
			}
		}
		
		// Get the channel
		ChatChannel channel;
		if (args.length >= start + 2) {
			// Temporary chat channel+
			ChatChannelTemplate template = manager.getTemplate(args[start]);
			if (template == null) {
				sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
				return;
			}
			
			channel = manager.getChannel(template, args[start+1]);
			if (channel == null) {
				sender.sendMessage(ChatColor.RED + "Unknown sub channel " + args[start+1]);
				return;
			}
		} else {
			if (args[start].equals("CLEAR")) {
				channel = null;
			} else {
				channel = manager.getChannel(args[start]);
				if (channel == null) {
					sender.sendMessage(ChatColor.RED + "Unknown channel " + args[start]);
					return;
				}
			}
		}
		
		// Apply the output channel
		StateAttachment state = target.getAttachment(StateAttachment.class);
		if (state == null) {
			state = new StateAttachment();
			target.addAttachment(state);
		}
		
		if (channel != null) {
			state.setOutputChannel(channel);
			if (isSelf) {
				sender.sendMessage(ChatColor.GREEN + "Your output channel has been set to " + channel.getName());
			} else {
				sender.sendMessage(ChatColor.GREEN + target.getDisplayName() + "'s output channel has been set to " + channel.getName());
			}
		} else {
			state.resetOutputChannel();
			if (isSelf) {
				sender.sendMessage(ChatColor.GREEN + "Your output channel has been reset");
			} else {
				sender.sendMessage(ChatColor.GREEN + target.getDisplayName() + "'s output channel has been reset");
			}
		}
		
		target.saveIfModified();
	}
}
