package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

public class CommandChatChannel extends FormattedChatChannel {
	private final String command;
	private final Optional<String> commandPermission;
	
	public CommandChatChannel(String name, Optional<String> listenPermission, String format, String command, Optional<String> commandPermission) {
		super(name, listenPermission, format);
		
		this.command = command;
		this.commandPermission = commandPermission;
	}
	
	public String getCommand() {
		return command;
	}
	
	public Optional<String> getCommandPermission() {
		return commandPermission;
	}
}
