package au.com.addstar.bchat;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.PacketManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.storage.StorageInterface;

public class BungeeChat extends JavaPlugin {
	private ChatChannelManager channelManager;
	private PacketManager packetManager;
	private Channel<BasePacket> channel;
	
	@Override
	public void onEnable() {
		setupChannel();
		setupChannelManager();
	}
	
	private void setupChannel() {
		packetManager = new PacketManager();
		channel = Global.getChannelManager().createChannel("bungeechat", BasePacket.class);
		channel.setCodec(packetManager.createCodec());
	}
	
	private void setupChannelManager() {
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");
		channelManager = new ChatChannelManager(backend, channel);
		
		channelManager.load();
	}
}
