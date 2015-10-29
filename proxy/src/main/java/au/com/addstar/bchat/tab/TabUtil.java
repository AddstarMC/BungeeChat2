package au.com.addstar.bchat.tab;

import java.util.function.Supplier;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

final class TabUtil {
	private TabUtil() {}
	
	public static PlayerListItem createPacket(Action action, Item... items) {
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(action);
		packet.setItems(items);
		return packet;
	}
	
	public static Supplier<String> toDebugString(final Action action, final Iterable<Item> items) {
		return new Supplier<String>() {
			@Override
			public String get() {
				StringBuilder overall = new StringBuilder();
				overall.append(action.name());
				overall.append(": [");
				
				for (Item item : items) {
					String name = item.getUsername();
					
					String message = null;
					switch (action) {
					case ADD_PLAYER:
						message = String.format("%d,%d,%s", item.getPing(), item.getGamemode(), BaseComponent.toLegacyText(item.getDisplayName()));
						break;
					case REMOVE_PLAYER:
						message = item.getUuid().toString();
						break;
					case UPDATE_DISPLAY_NAME:
						message = BaseComponent.toLegacyText(item.getDisplayName());
						break;
					case UPDATE_GAMEMODE:
						message = String.valueOf(item.getGamemode());
						break;
					case UPDATE_LATENCY:
						message = String.valueOf(item.getPing());
						break;
					}
					
					if (action == Action.ADD_PLAYER) {
						message = String.format("%s-%s: %s", name, item.getUuid().toString(), message);
					} else if (name == null) {
						message = String.format("%s: %s", item.getUuid().toString(), message);
					} else {
						message = String.format("%s: %s", name, message);
					}
					
					overall.append(message);
					overall.append(", ");
				}
				
				overall.append("]");
				
				return overall.toString();
			}
		};
	}
	
	public static Supplier<String> toDebugString(final Action action, final Item item) {
		return new Supplier<String>() {
			@Override
			public String get() {
				StringBuilder overall = new StringBuilder();
				overall.append(action.name());
				overall.append(':');
				
				String name = item.getUsername();
				
				String message = null;
				switch (action) {
				case ADD_PLAYER:
					message = String.format("%d,%d,%s", item.getPing(), item.getGamemode(), BaseComponent.toLegacyText(item.getDisplayName()));
					break;
				case REMOVE_PLAYER:
					message = item.getUuid().toString();
					break;
				case UPDATE_DISPLAY_NAME:
					message = BaseComponent.toLegacyText(item.getDisplayName());
					break;
				case UPDATE_GAMEMODE:
					message = String.valueOf(item.getGamemode());
					break;
				case UPDATE_LATENCY:
					message = String.valueOf(item.getPing());
					break;
				}
				
				if (action == Action.ADD_PLAYER) {
					message = String.format("%s-%s: %s", name, item.getUuid().toString(), message);
				} else if (name == null) {
					message = String.format("%s: %s", item.getUuid().toString(), message);
				} else {
					message = String.format("%s: %s", name, message);
				}
				
				overall.append(message);
				
				return overall.toString();
			}
		};
	}
}
