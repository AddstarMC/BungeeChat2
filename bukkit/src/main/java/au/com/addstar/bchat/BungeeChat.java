package au.com.addstar.bchat;

import java.util.concurrent.Executors;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import au.com.addstar.bchat.channels.ChannelHandler;
import au.com.addstar.bchat.channels.ChannelManagerListener;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.channels.ChannelPacketListener;
import au.com.addstar.bchat.commands.BungeeChatCommand;
import au.com.addstar.bchat.groups.GroupManager;
import au.com.addstar.bchat.groups.GroupManagerListener;
import au.com.addstar.bchat.groups.GroupPermissionHandler;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.PacketManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.storage.StorageInterface;

public class BungeeChat extends JavaPlugin {
	private ChatChannelManager channelManager;
	private GroupManager groupManager;
	private PacketManager packetManager;
	private Channel<BasePacket> channel;
	private ListeningExecutorService executorService;
	
	private ChannelHandler handler;
	
	@Override
	public void onEnable() {
		Debugger.initialize(getLogger());
		executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");
		setupChannel();
		setupChannelManager(backend);
		setupGroupManager(backend);
		setupHandlers();
		registerListeners();
		registerCommands();
	}
	
	private void setupChannel() {
		packetManager = new PacketManager();
		channel = Global.getChannelManager().createChannel("bungeechat", BasePacket.class);
		channel.setCodec(packetManager.createCodec());
	}
	
	private void setupChannelManager(StorageInterface backend) {
		channelManager = new ChatChannelManager(backend, channel);
		channel.addReceiver(new ChannelManagerListener(channelManager, executorService));
		
		// Load it async
		executorService.submit(() -> {
			channelManager.load();
		});
	}
	
	private void setupGroupManager(StorageInterface backend) {
		groupManager = new GroupManager(backend, channel);
		channel.addReceiver(new GroupManagerListener(groupManager, executorService));
		groupManager.setListener(new GroupPermissionHandler(getServer().getPluginManager()));
		
		// Load it async
		executorService.submit(() -> {
			groupManager.load();
		});
	}
	
	private void setupHandlers() {
		handler = new ChannelHandler(channelManager, channel, new ChatFormatter(groupManager));
		channel.addReceiver(new ChannelPacketListener(handler));
		channel.addReceiver(new PacketListener(this));
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new ChatListener(channelManager, handler), this);
		getServer().getPluginManager().registerEvents(new StateListener(groupManager), this);
	}
	
	private void registerCommands() {
		// /bungeechat command
		PluginCommand command = getCommand("bungeechat");
		BungeeChatCommand bchat = new BungeeChatCommand();
		command.setExecutor(bchat);
		command.setTabCompleter(bchat);
	}
}
