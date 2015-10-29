package au.com.addstar.bchat.tab;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.GameProfile;
import net.md_5.bungee.api.GameProfile.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

class TabItemBuilder {
	private final Item item;
	
	public TabItemBuilder(UUID id) {
		item = new Item();
		item.setUuid(id);
	}
	
	public TabItemBuilder(ProxiedPlayer player) {
		this(player.getUniqueId());
		item.setUsername(ChatColor.stripColor(player.getDisplayName()));
	}
	
	public TabItemBuilder withProfile(GameProfile profile) {
		String[][] properties = new String[profile.getProperties().length][];
		for (int i = 0; i < properties.length; ++i) {
			Property prop = profile.getProperties()[i];
			
			if (prop.getSignature() != null) {
				properties[i] = new String[] { prop.getName(), prop.getValue(), prop.getSignature() };
			} else {
				properties[i] = new String[] { prop.getName(), prop.getValue() };
			}
		}
		
		item.setProperties(properties);
		return this;
	}
	
	public TabItemBuilder withName(String name) {
		item.setDisplayName(TextComponent.fromLegacyText(name));
		return this;
	}
	
	public TabItemBuilder withName(BaseComponent[] name) {
		item.setDisplayName(name);
		return this;
	}
	
	public TabItemBuilder withPing(int ping) {
		item.setPing(ping);
		return this;
	}
	
	public TabItemBuilder withGamemode(int gamemode) {
		item.setGamemode(gamemode);
		return this;
	}
	
	public Item build() {
		return item;
	}
}
