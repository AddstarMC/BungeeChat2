package au.com.addstar.bchat.util;

import org.bukkit.command.CommandSender;

public interface SubCommand {
	public String getName();
	public String getDescription();
	public String[] getAliases();
	
	public String getUsage();
	public String getPermission();
	public String getPermissionMessage();
	
	public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException;
	public Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
