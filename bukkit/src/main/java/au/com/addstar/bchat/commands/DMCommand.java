package au.com.addstar.bchat.commands;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import au.com.addstar.bchat.ChatColorizer;
import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.DMChatChannel;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.util.Utilities;
import net.md_5.bungee.api.ChatColor;

public class DMCommand implements CommandExecutor, TabCompleter {
	private final ChatChannelManager manager;
	private final ChannelHandler handler;
	
	public DMCommand(ChatChannelManager manager, ChannelHandler handler) {
		this.manager = manager;
		this.handler = handler;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can direct message");
			return true;
		}
		
		if (args.length < 2) {
			return false;
		}
		
		GlobalPlayer source = Global.getPlayer(((Player)sender).getUniqueId());
		GlobalPlayer target;
		
		target = Global.getPlayer(args[0]);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
			return true;
		}
		
		String message = StringUtils.join(args, ' ', 1, args.length);
		message = ChatColorizer.colorizeWithPermission(message, sender);
		
		if (ChatColor.stripColor(message).trim().isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You cannot send an empty message");
			return true;
		}
		
		// TODO: Check message toggle
		DMChatChannel channel = manager.getDMChannel(source, target);
		handler.send(message, channel);
		
		// TODO: Update reply target
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			return Lists.newArrayList(Utilities.matchPlayerNames(args[0], true));
		}
		return null;
	}
}
