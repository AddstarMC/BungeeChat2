package au.com.addstar.bchat.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import au.com.addstar.bchat.Debugger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeChatCommand extends Command implements TabExecutor {
	public BungeeChatCommand() {
		super("!bungeechat", "bungeechat.command.bungeechat", "!bchat");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("/!bungeechat <reload|debug> [<options>]");
			return;
		}
		
		switch (args[0].toLowerCase()) {
		case "reload":
			// TODO: Reload command
			return;
		case "debug":
			handleDebug(sender, Arrays.copyOfRange(args, 1, args.length));
			return;
		default:
			sender.sendMessage("/!bungeechat <reload|debug> [<options>]");
			return;
		}
	}
	
	private void handleDebug(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("/!bungeechat debug <type> <level>");
			return;
		}
		
		// Parse logger
		Logger logger = null;
		for (String type : Debugger.KnownTypes) {
			if (type.equalsIgnoreCase(args[0])) {
				logger = Debugger.getLogger(type);
				break;
			}
		}
		
		if (logger == null) {
			sender.sendMessage(ChatColor.RED + "Unknown type '" + args[0] + "'. Available types: " + Debugger.KnownTypes);
			return;
		}
		
		// Parse Level
		Level level;
		try {
			level = Level.parse(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Unknown debug level '" + args[1] + "'. Available levels: all, finest, finer, fine, config, info, warning, severe, off");
			return;
		}
		
		logger.setLevel(level);
		
		sender.sendMessage("Debug log " + args[0] + " was set to " + level.getName());
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> results = Collections.emptyList();
		if (args.length == 1) {
			results = Arrays.asList("reload", "debug");
		} else {
			if (args[0].equalsIgnoreCase("debug")) {
				if (args.length == 2) {
					results = Debugger.KnownTypes;
				} else if (args.length == 3) {
					results = Arrays.asList("all", "finest", "finer", "fine", "config", "info", "warning", "severe", "off");
				}
			}
		}
		
		Iterable<String> filtered = Iterables.filter(results, (s) -> {
			return s.toLowerCase().startsWith(args[args.length-1].toLowerCase());
		});
		results = Lists.newArrayList(filtered);
		
		return results;
	}
}
