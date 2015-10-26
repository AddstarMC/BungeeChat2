package au.com.addstar.bchat.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.ChatColor;

public class BungeeChatCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		
		// Check permission, ignore for null command however
		if (!sender.hasPermission("bungeechat.command.bungeechat") && !args[0].equalsIgnoreCase("null")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
			return true;
		}
		
		switch (args[0].toLowerCase()) {
		case "null":
			return true;
		case "reload":
			// TODO: Reload command
			return true;
		case "debug":
			// TODO: Debug command
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> results;
		if (args.length == 1) {
			results = Arrays.asList("reload", "debug");
		} else {
			results = Lists.newArrayList();
		}
		
		results.removeIf((s) -> !s.startsWith(args[args.length-1]));
		
		return results;
	}
}
