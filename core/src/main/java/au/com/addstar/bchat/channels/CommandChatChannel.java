package au.com.addstar.bchat.channels;

import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

public class CommandChatChannel extends FormattedChatChannel {
	private String[] commands;
	private Optional<String> commandPermission;
	
	CommandChatChannel(String name, String[] commands, ChatChannelManager manager) {
		super(name, manager);
		
		this.commands = commands;
	}
	
	CommandChatChannel(String name, ChatChannelManager manager) {
		super(name, manager);
	}
	
	public String[] getCommands() {
		return commands;
	}
	
	public Optional<String> getCommandPermission() {
		return commandPermission;
	}
	
	public void setCommandPermission(Optional<String> permission) {
		commandPermission = permission;
	}
	
	@Override
	public void save(Map<String, String> values) {
		super.save(values);
		
		if (commandPermission.isPresent()) {
			values.put("commandPerm", commandPermission.get());
		}
		
		values.put("command", Joiner.on(';').join(commands));
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
	}
}
