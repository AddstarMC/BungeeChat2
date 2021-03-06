package au.com.addstar.bchat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import au.com.addstar.bchat.channels.ChannelManagerListener;
import au.com.addstar.bchat.channels.ChatChannelManager;
import au.com.addstar.bchat.commands.BungeeChatCommand;
import au.com.addstar.bchat.groups.GroupManager;
import au.com.addstar.bchat.groups.GroupManagerListener;
import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.PacketManager;
import au.com.addstar.bchat.packets.ReloadPacket;
import au.com.addstar.bchat.packets.ReloadPacket.ReloadType;
import au.com.addstar.bchat.tab.TabListener;
import au.com.addstar.bchat.tab.TabManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeChat extends Plugin {
	private ChatChannelManager channelManager;
	private GroupManager groupManager;
	private PacketManager packetManager;
	private Channel<BasePacket> channel;
	private ListeningExecutorService executorService;
	private TabManager tabManager;
	
	@Override
	public void onEnable() {
		Debugger.initialize(getLogger());
		try {
			prepareDataFolder();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to initialize the data folder", e);
			return;
		}
		
		executorService = MoreExecutors.listeningDecorator(getExecutorService());
		
		setupChannel();
		
		// Get the bungeechat storage interface
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");

		loadChannels(backend);
		loadGroups(backend);
		loadHighlighter(backend);
		setupTabList();
		registerCommands();
	}

	private void prepareDataFolder() throws IOException {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists()) {
			Files.createDirectories(dataFolder.toPath());
		}

		// Save default configs
		saveDefaultFile("channels.yml", dataFolder);
		saveDefaultFile("groups.yml", dataFolder);
		saveDefaultFile("config.yml", dataFolder);
		saveDefaultFile("keywords.txt", dataFolder);
	}

	private void saveDefaultFile(String fileName, File directory) throws IOException {
		File file = new File(directory, fileName);
		if (!file.exists()) {
			Files.copy(BungeeChat.class.getResourceAsStream("/defaults/" + fileName), file.toPath());
		}
	}

	private void loadChannels(StorageInterface backend) {
		channelManager = new ChatChannelManager(backend, channel);
		channel.addReceiver(new ChannelManagerListener(channelManager, executorService));
		
		loadChannelConfig();
	}
	
	private void loadChannelConfig() {
		channelManager.clear();
		
		// Populate with config loaded channels
		File configFile = new File(getDataFolder(), "channels.yml");
		if (configFile.exists()) {
			ChannelConfigLoader loader = new ChannelConfigLoader(channelManager, getLogger());
			
			try {
				loader.load(configFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to load channels.yml", e);
			}
			
			// Push to the backend
			channelManager.save();
		}
	}
	
	private void setupChannel() {
		packetManager = new PacketManager();
		channel = Global.getChannelManager().createChannel("bungeechat", BasePacket.class);
		channel.setCodec(packetManager.createCodec());
	}
	
	private void loadGroups(StorageInterface backend) {
		groupManager = new GroupManager(backend, channel);
		channel.addReceiver(new GroupManagerListener(groupManager, executorService));
		
		loadGroupConfig();
	}
	
	private void loadGroupConfig() {
		groupManager.clear();
		
		// Populate with config loaded groups
		File configFile = new File(getDataFolder(), "groups.yml");
		if (configFile.exists()) {
			GroupConfigLoader loader = new GroupConfigLoader(groupManager, getLogger());
			
			try {
				loader.load(configFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to load groups.yml", e);
			}
			
			// Push to the backend
			groupManager.save();
		}
	}
	
	private void loadHighlighter(StorageInterface backend) {
		// TODO: Configurable file name and enable / disable
		File configFile = new File(getDataFolder(), "keywords.txt");
		if (configFile.exists()) {
			HighlighterConfigLoader loader = new HighlighterConfigLoader(backend, getLogger());
			try {
				loader.load(configFile);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to load " + configFile.getName(), e);
			}
			
			channel.broadcast(new ReloadPacket(ReloadType.Highlighter));
		}
	}
	
	private void setupTabList() {
		tabManager = new TabManager(groupManager, 20, TimeUnit.MILLISECONDS);
		tabManager.startSendingTask(this, getProxy().getScheduler());
		getProxy().getPluginManager().registerListener(this, new TabListener(tabManager, channel));
	}
	
	private void registerCommands() {
		getProxy().getPluginManager().registerCommand(this, new BungeeChatCommand(this));
	}
	
	public void reload() {
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");
		
		loadChannelConfig();
		loadGroupConfig();
		loadHighlighter(backend);
	}
}
