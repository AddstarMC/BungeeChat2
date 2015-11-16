package au.com.addstar.bchat.commands.channel;

import org.bukkit.command.CommandSender;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.channels.ChatChannel;
import au.com.addstar.bchat.channels.ChatChannelManager;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.ChatColor;

public class OutputCommand extends AbstractChannelSubCommand {
	public OutputCommand(ChatChannelManager manager) {
		super(manager);
	}

	@Override
	public String getName() {
		return "output";
	}

	@Override
	public String getDescription() {
		return "Sets your output channel";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"setoutput"};
	}

	@Override
	public String getPermission() {
		return "bungeechat.command.channel.output";
	}

	@Override
	public String getPermissionMessage() {
		return "You do not have permission to set output channels";
	}

	@Override
	public String getPermissionOther() {
		return "bungeechat.command.channel.output.other";
	}

	@Override
	public String getPermissionMessageOther() {
		return "You do not have permission to set the output channel of others";
	}
	
	@Override
	protected boolean allowClear() {
		return true;
	}

	@Override
	protected void onCommand(CommandSender sender, boolean isSelf, GlobalPlayer target, ChatChannel channel) {
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
