package au.com.addstar.bchat;

import com.google.common.base.Strings;

import au.com.addstar.bchat.attachments.StateAttachment;
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
}
