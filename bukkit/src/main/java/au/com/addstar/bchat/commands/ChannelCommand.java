package au.com.addstar.bchat.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import com.google.common.collect.Lists;

import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.commands.channel.*;
import au.com.addstar.bchat.util.CommandDispatcher;

public class ChannelCommand extends CommandDispatcher implements TabExecutor {
	public ChannelCommand(ChatChannelManager manager) {
		registerCommand(new SubscribeCommand(manager));
		registerCommand(new UnsubscribeCommand(manager));
		registerCommand(new OutputCommand(manager));		
		registerCommand(new AddCommand(manager));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		Iterable<String> results = dispatchTabComplete(sender, args);
		if (results != null) {
			return Lists.newArrayList(results);
		} else {
			return null;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		dispatchCommand(sender, args);
		return true;
	}
}

