package au.com.addstar.bchat;

import com.google.common.base.Strings;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.channels.DMChatChannel;
import au.com.addstar.bchat.channels.TemporaryChatChannel;
import au.com.addstar.bchat.groups.Group;
import au.com.addstar.bchat.groups.GroupManager;
import net.cubespace.geSuit.core.GlobalPlayer;

public class ChatFormatter {
	private final Group blankGroup = new Group("");
	private final StateAttachment blankState = new StateAttachment();
	
	private final GroupManager groupManager;
	
	public ChatFormatter(GroupManager groupManager) {
		this.groupManager = groupManager;
	}
	
	private String formatWithGroup(String message, Group group, String displayName, String name, boolean isTarget) {
		if (isTarget) {
			message = message.replace("{TGROUP}", group.getName());
			message = message.replace("{TPREFIX}", Strings.nullToEmpty(group.getPrefix()));
			message = message.replace("{TSUFFIX}", Strings.nullToEmpty(group.getSuffix()));
			message = message.replace("{TDISPLAYNAME}", group.getColorFormatted() + displayName);
			message = message.replace("{TNAME}", group.getColor() + name);
			message = message.replace("{TRAWDISPLAYNAME}", displayName);
			message = message.replace("{TRAWNAME}", name);
		} else {
			message = message.replace("{GROUP}", group.getName());
			message = message.replace("{PREFIX}", Strings.nullToEmpty(group.getPrefix()));
			message = message.replace("{SUFFIX}", Strings.nullToEmpty(group.getSuffix()));
			message = message.replace("{DISPLAYNAME}", group.getColorFormatted() + displayName);
			message = message.replace("{NAME}", group.getColor() + name);
			message = message.replace("{RAWDISPLAYNAME}", displayName);
			message = message.replace("{RAWNAME}", name);
		}
		return message;
	}
	
	public String format(String message, String format, GlobalPlayer sender) {
		StateAttachment state = sender.getAttachment(StateAttachment.class);
		if (state == null) {
			state = blankState;
		}
		
		// Do group formatting
		String formatted = format;
		Group group = null;
		if (state.getGroupName() != null) {
			group = groupManager.getGroup(state.getGroupName());
		}
		
		if (group == null) {
			group = blankGroup;
		}
		
		formatted = formatWithGroup(formatted, group, sender.getDisplayName(), sender.getName(), false);
		
		// rest of the formatting
		formatted = formatted.replace("{SERVER}", Strings.nullToEmpty(state.getServer()));
		formatted = formatted.replace("{WORLD}", Strings.nullToEmpty(state.getWorld()));
		formatted = formatted.replace("{MESSAGE}", message);
		
		return formatted;
	}
	
	public String formatConsole(String message, String format, String consoleName) {
		// Do group formatting
		String formatted = format;
		Group group = groupManager.getConsoleGroup();
		
		if (group == null) {
			group = blankGroup;
		}
		
		String displayName = consoleName;
		if (groupManager.getConsoleName() != null) {
			displayName = groupManager.getConsoleName();
		}
		
		formatted = formatWithGroup(formatted, group, displayName, consoleName, false);
		
		formatted = formatted.replace("{SERVER}", "");
		formatted = formatted.replace("{WORLD}", "");
		formatted = formatted.replace("{MESSAGE}", message);
		
		return formatted;
	}
	
	public String formatIn(String message, String format, TemporaryChatChannel channel, GlobalPlayer sender) {
		String partial = format(message, format, sender);
		partial.replace("{CHANNEL}", channel.getSubName());
		return partial;
	}
	
	public String formatConsoleIn(String message, String format, TemporaryChatChannel channel, String consoleName) {
		String partial = formatConsole(message, format, consoleName);
		partial.replace("{CHANNEL}", channel.getSubName());
		return partial;
	}
	
	/**
	 * Formats a DM message
	 * @param message The message to send
	 * @param sender The sender of the dm
	 * @param listener The player that will be receiving this (for name replacement). Can be null
	 * @param channel The dm channel
	 * @return The formatted string
	 */
	public String formatDM(String message, GlobalPlayer sender, GlobalPlayer listener, DMChatChannel channel) {
		GlobalPlayer target;
		
		if (channel.getEnd1().equals(sender)) {
			target = channel.getEnd2();
		} else {
			target = channel.getEnd1();
		}
		
		// Get the senders state
		StateAttachment senderState = sender.getAttachment(StateAttachment.class);
		if (senderState == null) {
			senderState = blankState;
		}
		
		// Do sender group formatting
		String formatted = channel.getFormat();
		Group senderGroup = null;
		if (senderState.getGroupName() != null) {
			senderGroup = groupManager.getGroup(senderState.getGroupName());
		}
		
		if (senderGroup == null) {
			senderGroup = blankGroup;
		}
		
		// Replace the sender tags
		if (channel.shouldReplaceSelf() && sender.equals(listener)) {
			formatted = formatWithGroup(formatted, senderGroup, channel.getReplaceWord(), channel.getReplaceWord(), false);
		} else {
			formatted = formatWithGroup(formatted, senderGroup, sender.getDisplayName(), sender.getName(), false);
		}
		
		// Get the targets state
		StateAttachment targetState = target.getAttachment(StateAttachment.class);
		if (targetState == null) {
			targetState = blankState;
		}
		
		// Do target group formatting
		Group targetGroup = null;
		if (targetState.getGroupName() != null) {
			targetGroup = groupManager.getGroup(targetState.getGroupName());
		}
		
		if (targetGroup == null) {
			targetGroup = blankGroup;
		}
		
		// Replace the target tags
		if (channel.shouldReplaceSelf() && target.equals(listener)) {
			formatted = formatWithGroup(formatted, targetGroup, channel.getReplaceWord(), channel.getReplaceWord(), true);
		} else {
			formatted = formatWithGroup(formatted, targetGroup, target.getDisplayName(), target.getName(), true);
		}
		
		// rest of the formatting
		formatted = formatted.replace("{SERVER}", Strings.nullToEmpty(senderState.getServer()));
		formatted = formatted.replace("{WORLD}", Strings.nullToEmpty(senderState.getWorld()));
		formatted = formatted.replace("{MESSAGE}", message);
		
		return formatted;
	}
}
