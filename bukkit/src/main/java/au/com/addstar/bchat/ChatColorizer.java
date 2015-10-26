package au.com.addstar.bchat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatColorizer {
	/**
	 * Colorize the message using permissions 
	 * @param message The message
	 * @param sender The sender to check permissions on
	 * @return The formatted string
	 */
	public static String colorizeWithPermission(String message, CommandSender sender) {
		for (ChatColor color : ChatColor.values()) {
			String permission = "bungeechat.";
			if (color.isFormat()) {
				permission += "format." + color.name().toLowerCase();
			} else {
				permission += "color." + color.getChar();
			}
			
			if (sender.hasPermission(permission)) {
				message = message.replace("&" + color.getChar(), ChatColor.COLOR_CHAR + String.valueOf(color.getChar()));
			}
		}
		
		return message;
	}
	
	/**
	 * Colorize the message fully.
	 * @param message The message
	 * @return The formatted message
	 */
	public static String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
