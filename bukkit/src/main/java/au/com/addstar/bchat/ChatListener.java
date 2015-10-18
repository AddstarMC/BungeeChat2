package au.com.addstar.bchat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ChatListener implements Listener {
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onChatPre(AsyncPlayerChatEvent event) {
		// TODO: Set the format and change receivers
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onChatPost(AsyncPlayerChatEvent event) {
		// TODO: Send off the chat to those that should receive it
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		// TODO: Check for chat aliases
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onServerCommand(ServerCommandEvent event) {
		// TODO: Check for chat aliases
	}
}
