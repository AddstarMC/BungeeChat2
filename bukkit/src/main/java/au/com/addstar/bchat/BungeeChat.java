package au.com.addstar.bchat;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.bchat.channels.ChatChannelManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.storage.StorageInterface;

public class BungeeChat extends JavaPlugin {
	private ChatChannelManager channelManager;
	
	@Override
	public void onEnable() {
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");
		channelManager = new ChatChannelManager(backend);
		
		channelManager.load();
	}
}
