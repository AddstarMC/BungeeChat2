package au.com.addstar.bchat.commands.channel;

import org.bukkit.command.CommandSender;

import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChatChannelTemplate;
import au.com.addstar.bchat.channels.TemporaryChatChannel;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.ChatColor;

public class SubscribeCommand extends AbstractChannelSubCommand {
	public SubscribeCommand(ChatChannelManager manager) {
		super(manager);
	}

	@Override
	public String getName() {
		return "subscribe";
	}

	@Override
	public String getDescription() {
		return "Subscribes to a channel. The channel must allow subscriptions and you must have permission to join it";
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
		return "You do not have permission to subscribe to channels";
	}

	@Override
	public String getPermissionOther() {
		return "bungeechat.command.channel.subscribe";
	}

	@Override
	public String getPermissionMessageOther() {
		return "You do not have permission to subscribe other players to channels";
	}

	@Override
	protected void onCommand(CommandSender sender, boolean isSelf, GlobalPlayer target, ChatChannel channel) {
		if (channel instanceof TemporaryChatChannel) {
			ChatChannelTemplate template = ((TemporaryChatChannel) channel).getTemplate();
			if (template.getJoinPermission().isPresent()) {
				if (!sender.hasPermission(template.getJoinPermission().get())) {
					throw new IllegalArgumentException("You do not have permission to join this channel");
				}
			}
		}
		
		if (!channel.allowSubscriptions()) {
			throw new IllegalArgumentException("This channel does not allow subscriptions");
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
}
