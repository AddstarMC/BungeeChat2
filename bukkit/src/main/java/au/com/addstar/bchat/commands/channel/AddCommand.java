package au.com.addstar.bchat.commands.channel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChatChannelTemplate;
import au.com.addstar.bchat.channels.TemporaryChatChannel;
import au.com.addstar.bchat.util.BadArgumentException;
import au.com.addstar.bchat.util.SubCommand;
import net.cubespace.geSuit.core.Global;
import net.md_5.bungee.api.ChatColor;

public class AddCommand implements SubCommand {
	private final ChatChannelManager manager;
	public AddCommand(ChatChannelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public String getName() {
		return "add";
	}

	@Override
	public String getDescription() {
		return "Adds a new temporary channel";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getUsage() {
		return "<template> <name>";
	}

	@Override
	public String getPermission() {
		return "bungeechat.command.add";
	}

	@Override
	public String getPermissionMessage() {
		return "You do not have permission to create channels";
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException {
		if (args.length < 2) {
			return false;
		}
		
		ChatChannelTemplate template = manager.getTemplate(args[0]);
		if (template == null) {
			throw new BadArgumentException(0, "Unknown template");
		}
		
		if (template.getCreatePermission().isPresent()) {
			if (!sender.hasPermission(template.getCreatePermission().get())) {
				throw new IllegalArgumentException("You dont have permission to create one of these channels");
			}
		}
		
		if (manager.getChannel(template, args[1]) != null) {
			throw new IllegalArgumentException("You cannot create that channel, it already exists");
		}
		
		TemporaryChatChannel channel = manager.createTemporaryChannel(args[1], template);
		if (sender instanceof Player) {
			channel.setChannelAdmin(((Player)sender).getUniqueId());
			channel.addSubscriber(Global.getPlayer(((Player)sender).getUniqueId()));
		}
		
		manager.save();
		sender.sendMessage(ChatColor.GREEN + "Channel created.");
		return true;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return Iterables.filter(Iterables.transform(manager.getTemplates(), t -> t.getName()), t -> t.startsWith(args[0]));
		}
		return null;
	}

}
