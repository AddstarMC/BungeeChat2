package au.com.addstar.bchat.tab;

import au.com.addstar.bchat.attachments.StateAttachment;
import net.cubespace.geSuit.core.events.player.GlobalPlayerAttachmentUpdateEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TabListener implements Listener {
	private final TabManager manager;
	public TabListener(TabManager manager) {
		this.manager = manager;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onConnect(PostLoginEvent event) {
		manager.onConnect(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDisconnect(PlayerDisconnectEvent event) {
		manager.onDisconnect(event.getPlayer());
	}
	
	@EventHandler
	public void onGroupChange(GlobalPlayerAttachmentUpdateEvent event) {
		if (event.getAttachment() instanceof StateAttachment) {
			manager.onStateUpdate(event.getPlayer());
		}
	}
}
