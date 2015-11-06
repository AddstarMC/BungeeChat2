package au.com.addstar.bchat.tab;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.RefreshPacket;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerAttachmentUpdateEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TabListener implements Listener {
	private final TabManager manager;
	private final Channel<BasePacket> channel;
	public TabListener(TabManager manager, Channel<BasePacket> channel) {
		this.manager = manager;
		this.channel = channel;
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
			channel.broadcast(new RefreshPacket(event.getPlayer().getUniqueId()));
		}
	}
}
