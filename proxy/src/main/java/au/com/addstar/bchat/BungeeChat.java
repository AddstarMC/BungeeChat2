package au.com.addstar.bchat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import au.com.addstar.bchat.channels.ChatChannelManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeChat extends Plugin {
	private ChatChannelManager channelManager;
	
	@Override
	public void onEnable() {
		try {
			prepareDataFolder();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to initialize the data folder", e);
			return;
		}
		
		// Get the bungeechat storage interface
		StorageInterface backend = Global.getStorageProvider().create("bungeechat");

		loadChannels(backend);
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
	}

	private void saveDefaultFile(String fileName, File directory) throws IOException {
		File file = new File(directory, fileName);
		if (!file.exists()) {
			Files.copy(BungeeChat.class.getResourceAsStream("/defaults/" + fileName), file.toPath());
		}
	}

	private void loadChannels(StorageInterface backend) {
		channelManager = new ChatChannelManager(backend);
		
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
}
