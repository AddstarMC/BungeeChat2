package au.com.addstar.bchat.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.bchat.ChatColorizer;
import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.DMChatChannel;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;

public class ReplyCommand implements CommandExecutor {
	private final ChatChannelManager manager;
	private final ChannelHandler handler;
	
	public ReplyCommand(ChatChannelManager manager, ChannelHandler handler) {
		this.manager = manager;
		this.handler = handler;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this");
			return true;
		}
		
		if (args.length < 1) {
			return false;
		}
		
		GlobalPlayer source = Global.getPlayer(((Player)sender).getUniqueId());
		GlobalPlayer target = null;
		
		// Find reply target
		StateAttachment attachment = source.getAttachment(StateAttachment.class);
		if (attachment != null) {
			if (attachment.getReplyTo() != null) {
				target = Global.getPlayer(attachment.getReplyTo());
			}
		}
		
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "You have nobody to reply to");
			return true;
		}
		
		String message = StringUtils.join(args, ' ');
		message = ChatColorizer.colorizeWithPermission(message, sender);
		
		if (ChatColor.stripColor(message).trim().isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You cannot send an empty message");
			return true;
		}
		
		DMChatChannel channel = manager.getDMChannel(source, target);
		handler.sendFormat(message, channel, sender);
		
		// Update reply targets
		attachment = target.getAttachment(StateAttachment.class);
		if (attachment == null) {
			attachment = new StateAttachment();
			target.addAttachment(attachment);
		}
		
		attachment.setReplyTo(source.getUniqueId());
		
		target.saveIfModified();
		return true;
	}
}
