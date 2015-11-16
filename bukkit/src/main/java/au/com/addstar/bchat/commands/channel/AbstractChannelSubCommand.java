package au.com.addstar.bchat.commands.channel;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChatChannelTemplate;
import au.com.addstar.bchat.channels.TemporaryChatChannel;
import au.com.addstar.bchat.util.BadArgumentException;
import au.com.addstar.bchat.util.SubCommand;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.util.Utilities;
import net.md_5.bungee.api.ChatColor;

public abstract class AbstractChannelSubCommand implements SubCommand {
	protected final ChatChannelManager manager;
	
	public AbstractChannelSubCommand(ChatChannelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public String getUsage() {
		return "[-p <player>] <channel> [<subchannel>]";
	}
	
	public abstract String getPermissionOther();
	public abstract String getPermissionMessageOther();
	
	protected boolean allowClear() {
		return false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException {
		if (args.length < 1) {
			return false;
		}
		
		int start = 0;
		boolean isSelf;
		GlobalPlayer target;
		// Specify player
		if (args[0].equalsIgnoreCase("-p")) {
			if (!sender.hasPermission(getPermissionOther())) {
				sender.sendMessage(ChatColor.RED + getPermissionMessageOther());
				return true;
			}
			
			// Check available arguments
			if (args.length < 3) {
				return false;
			}
			
			isSelf = false;
			target = Global.getPlayer(args[1]);
			if (target == null) {
				throw new BadArgumentException(1, "Unknown player");
			}
			
			start = 2;
		} else {
			if (sender instanceof Player) {
				isSelf = true;
				target = Global.getPlayer(((Player)sender).getUniqueId());
			} else {
				sender.sendMessage(ChatColor.RED + "You must specify a player with '-p <player>' to use this from console");
				return true;
			}
		}
		
		// Get the channel
		ChatChannel channel;
		if (args.length >= start + 2) {
			// Temporary chat channel
			ChatChannelTemplate template = manager.getTemplate(args[start]);
			if (template == null) {
				throw new BadArgumentException(start, "Unknown parent channel or channel does not have sub channels");
			}
			
			channel = manager.getChannel(template, args[start+1]);
			if (channel == null) {
				throw new BadArgumentException(start+1, "Unknown sub channel");
			}
		} else {
			if (allowClear() && args[start].equals("CLEAR")) {
				channel = null;
			} else {
				channel = manager.getChannel(args[start]);
				if (channel == null) {
					throw new BadArgumentException(start, "Unknown channel");
				}
			}
		}
		
		onCommand(sender, isSelf, target, channel);
		return true;
	}
	
	protected abstract void onCommand(CommandSender sender, boolean isSelf, GlobalPlayer target, ChatChannel channel);

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		String input = args[args.length-1];
		Iterable<String> options;
		if (args.length == 1) {
			if (input.startsWith("-")) {
				options = Arrays.asList("-p");
			} else {
				options = Iterables.concat(
					Iterables.transform(Iterables.filter(manager.getChannels(), (c) -> !(c instanceof TemporaryChatChannel)), (c) -> c.getName()),
					Iterables.transform(manager.getTemplates(), (t) -> t.getName())
				);
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("-p")) {
				options = Utilities.matchPlayerNames(input, true);
			} else {
				// Subchannel
				ChatChannelTemplate template = manager.getTemplate(args[0]);
				if (template == null) {
					return null;
				}
				
				options = Iterables.transform(manager.getChannels(template), c -> ((TemporaryChatChannel)c).getSubName());
			}
		} else if (args.length == 3) {
			options = Iterables.concat(
				Iterables.transform(Iterables.filter(manager.getChannels(), (c) -> !(c instanceof TemporaryChatChannel)), (c) -> c.getName()),
				Iterables.transform(manager.getTemplates(), (t) -> t.getName())
			);
		} else if (args.length == 4) {
			// Subchannel
			ChatChannelTemplate template = manager.getTemplate(args[3]);
			if (template == null) {
				return null;
			}
			
			options = Iterables.transform(manager.getChannels(template), c -> ((TemporaryChatChannel)c).getSubName());
		} else {
			return null;
		}
		
		return Iterables.filter(options, s -> s.toLowerCase().startsWith(input.toLowerCase()));
	}
}
