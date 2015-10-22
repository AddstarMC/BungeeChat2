package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

public class CommandChatChannel extends FormattedChatChannel {
	private String[] commands;
	private Optional<String> commandPermission;
	private boolean useHighlighter;
	
	CommandChatChannel(String name, Optional<String> listenPermission, String format, String[] commands, Optional<String> commandPermission, ChatChannelManager manager) {
		super(name, listenPermission, format, manager);
		
		this.commands = commands;
		this.commandPermission = commandPermission;
		useHighlighter = false;
	}
	
	CommandChatChannel(String name, ChatChannelManager manager) {
		super(name, manager);
	}
	
	public String[] getCommands() {
		return commands;
	}
	
	public void setCommands(String[] commands) {
		this.commands = commands;
	}
	
	public Optional<String> getCommandPermission() {
		return commandPermission;
	}
	
	public void setCommandPermission(Optional<String> permission) {
		commandPermission = permission;
	}
	
	public boolean getUseHighlighter() {
		return useHighlighter;
	}
	
	public void setUseHighlighter(boolean useHighlighter) {
		this.useHighlighter = useHighlighter;
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		if (commandPermission.isPresent()) {
			values.put("commandPerm", commandPermission.get());
		}
		
		values.put("command", Joiner.on(';').join(commands));
		values.put("highlight", String.valueOf(useHighlighter));
		values.put("type", "command");
	}
	
	@Override
	public void load(Map<String, String> values) {
		super.load(values);
		
		if (values.containsKey("commandPerm")) {
			commandPermission = Optional.of(values.get("commandPerm"));
		} else {
			commandPermission = Optional.absent();
		}
		
		commands = values.get("command").split(";");
		useHighlighter = Boolean.parseBoolean(values.get("highlight"));
	}
}
