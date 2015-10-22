package au.com.addstar.bchat;

import java.util.concurrent.Executors;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import au.com.addstar.bchat.channels.ChannelManagerListener;
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
	private ListeningExecutorService executorService; 
	
	@Override
	public void onEnable() {
		executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
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
		channel.addReceiver(new ChannelManagerListener(channelManager, executorService));
		
		// Load it async
		executorService.submit(() -> {
			channelManager.load();
		});
	}
}
