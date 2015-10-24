package au.com.addstar.bchat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.groups.Group;
import au.com.addstar.bchat.groups.GroupManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;

public class StateListener implements Listener {
	private final GroupManager groupManager;
	
	public StateListener(GroupManager groupManager) {
		this.groupManager = groupManager;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		updateState(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		updateState(event.getPlayer());
	}
	
	private void updateState(Player bPlayer) {
		// Update players group setting
		GlobalPlayer player = Global.getPlayer(bPlayer.getUniqueId());
		
		StateAttachment attachment = player.getAttachment(StateAttachment.class);
		if (attachment == null) {
			attachment = new StateAttachment();
			player.addAttachment(attachment);
		}
		
		// Find group
		Group group = groupManager.getHighestGroup(g -> {
			if (g.getPermission() != null) {
				return bPlayer.hasPermission(g.getPermission());
			} else {
				return true;
			}
		});
		
		attachment.setGroup(group);
		
		// Update other state info
		attachment.setWorld(bPlayer.getWorld().getName());
		if (Global.getServer() != null) {
			attachment.setServer(Global.getServer().getName());
		}
		
		player.saveIfModified();
	}
}
