package au.com.addstar.bchat.commands.channel;

import org.bukkit.command.CommandSender;

import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.ChatColor;

public class UnsubscribeCommand extends AbstractChannelSubCommand {
	public UnsubscribeCommand(ChatChannelManager manager) {
		super(manager);
	}

	@Override
	public String getName() {
		return "unsubscribe";
	}

	@Override
	public String getDescription() {
		return "Unsubscribes from a channel";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "bungeechat.command.channel.subscribe";
	}

	@Override
	public String getPermissionMessage() {
		return "You do not have permission to unsubscribe from channels";
	}

	@Override
	public String getPermissionOther() {
		return "bungeechat.command.channel.subscribe.other";
	}

	@Override
	public String getPermissionMessageOther() {
		return "You do not have permission to unsubscribe other players from channels";
	}

	@Override
	protected void onCommand(CommandSender sender, boolean isSelf, GlobalPlayer target, ChatChannel channel) {
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
	}
}
