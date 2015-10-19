package au.com.addstar.bchat.channels;

import com.google.common.base.Optional;

public class CommandChatChannel extends FormattedChatChannel {
	private final String[] commands;
	private final Optional<String> commandPermission;
	private boolean useHighlighter;
	
	public CommandChatChannel(String name, Optional<String> listenPermission, String format, String[] commands, Optional<String> commandPermission) {
		super(name, listenPermission, format);
		
		this.commands = commands;
		this.commandPermission = commandPermission;
		useHighlighter = false;
	}
	
	public String[] getCommands() {
		return commands;
	}
	
	public Optional<String> getCommandPermission() {
		return commandPermission;
	}
	
	public boolean getUseHighlighter() {
		return useHighlighter;
	}
	
	public void setUseHighlighter(boolean useHighlighter) {
		this.useHighlighter = useHighlighter;
	}
}
