package au.com.addstar.bchat;

import java.util.concurrent.Executors;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChannelManagerListener;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.PacketListener;
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
	
	private ChannelHandler handler;
	
	@Override
	public void onEnable() {
		executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
		setupChannel();
		setupChannelManager();
		setupHandlers();
		registerListeners();
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
	
	private void setupHandlers() {
		handler = new ChannelHandler(channelManager, channel);
		channel.addReceiver(new PacketListener(handler));
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new ChatListener(channelManager, handler), this);
	}
}
