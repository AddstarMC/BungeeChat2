package au.com.addstar.bchat.tab;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import au.com.addstar.bchat.Debugger;
import au.com.addstar.bchat.groups.Group;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

class TabHandler extends TabListAdapter {
	private static final int PING_THRESHOLD = 20;
	
	private final TabManager manager;
	
	// TODO: Player Visibility
	private int lastUpdatedPing;
	private String lastUpdatedName;
	private int lastUpdatedGamemode;
	
	// Access to queuedPackets must be synchronized on queuedPackets
	private final ListMultimap<Action, Item> queuedPackets;
	private final Set<UUID> currentlyVisible;
	
	public TabHandler(TabManager manager) {
		this.manager = manager;
		
		queuedPackets = ArrayListMultimap.create();
		currentlyVisible = Sets.newHashSet();
	}
	
	public GlobalPlayer getGPlayer() {
		return Global.getPlayer(getPlayer().getUniqueId());
	}
	
	private String getDisplayName() {
		GlobalPlayer player = getGPlayer();
		if (player == null) {
			return getPlayer().getDisplayName();
		} else {
			Group group = manager.getGroup(player);
			return group.getColorFormatted() + player.getDisplayName();
		}
	}
	
	@Override
	public void onConnect() {
		manager.getLog().info(String.format("Connect %s", getPlayer().getName()));
		
		lastUpdatedName = getDisplayName();
		lastUpdatedPing = getPlayer().getPing();
		lastUpdatedGamemode = 0;
		
		manager.queue(this, Action.ADD_PLAYER, getAddItem());
		
		manager.refresh(getPlayer());
	}
	
	/**
	 * Gets the Item that will add this player in its current state
	 * @return The state of the player
	 */
	Item getAddItem() {
		return new TabItemBuilder(getPlayer())
			.withProfile(getPlayer().getProfile())
			.withName(lastUpdatedName)
			.withPing(lastUpdatedPing)
			.withGamemode(lastUpdatedGamemode)
			.build();
	}
	
	@Override
	public void onDisconnect() {
		manager.getLog().info(String.format("Disconnect %s", getPlayer().getName()));
		manager.queue(this, Action.REMOVE_PLAYER, 
			new TabItemBuilder(getPlayer())
			.build()
		);
	}
	
	@Override
	public void onPingChange(int newPing) {
		if (newPing - PING_THRESHOLD > lastUpdatedPing && newPing + PING_THRESHOLD < lastUpdatedPing) {
			manager.getLog().fine(String.format("Updating ping %s to %d", getPlayer().getName(), newPing));
			manager.queue(this, Action.UPDATE_LATENCY,
				new TabItemBuilder(getPlayer())
				.withPing(newPing)
				.build()
			);
			
			lastUpdatedPing = newPing;
		}
	}
	
	@Override
	public void onUpdateName() {
		String newName = getDisplayName();
		if (!newName.equals(lastUpdatedName)) {
			manager.getLog().fine(String.format("Updating name %s from %s to %s", getPlayer().getName(), lastUpdatedName, newName));
			manager.queue(this, Action.UPDATE_DISPLAY_NAME,
				new TabItemBuilder(getPlayer())
				.withName(newName)
				.build()
			);
			
			lastUpdatedName = newName;
		}
		
		// TODO: We need to know when the colour changes somehow
	}
	
	public void onGamemodeChange(int newGamemode) {
		if (newGamemode != lastUpdatedGamemode) {
			manager.getLog().fine(String.format("Updating gamemode %s to %d", getPlayer().getName(), newGamemode));
			manager.queue(this, Action.UPDATE_GAMEMODE,
				new TabItemBuilder(getPlayer())
				.withGamemode(newGamemode)
				.build()
			);
			
			lastUpdatedGamemode = newGamemode;
		}
	}
	
	@Override
	public void onUpdate(PlayerListItem packet) {
		// Allow update gamemode
		if (packet.getAction() == Action.UPDATE_GAMEMODE) {
			for (Item item : packet.getItems()) {
				if (item.getUuid().equals(getPlayer().getUniqueId())) {
					onGamemodeChange(item.getGamemode());
					break;
				}
			}
		}
		
		// TODO: Allow npc packets
	}
	
	/**
	 * Queues the item to be sent soon. This will minimize packets
	 * reducing duplicates
	 * @param action The action to be done
	 * @param item The item
	 */
	public void queue(Action action, Item item) {
		synchronized(queuedPackets) {
			Predicate<Item> uuidMatches = (i) -> i.getUuid().equals(item.getUuid());
			
			// Minimize as we add
			switch (action) {
			// Both force a clean slate
			case ADD_PLAYER:
			case REMOVE_PLAYER:
				queuedPackets.get(Action.ADD_PLAYER).removeIf(uuidMatches);
				queuedPackets.get(Action.UPDATE_DISPLAY_NAME).removeIf(uuidMatches);
				queuedPackets.get(Action.UPDATE_GAMEMODE).removeIf(uuidMatches);
				queuedPackets.get(Action.UPDATE_LATENCY).removeIf(uuidMatches);
				queuedPackets.get(Action.REMOVE_PLAYER).removeIf(uuidMatches);
				break;
			case UPDATE_DISPLAY_NAME:
				queuedPackets.get(Action.UPDATE_DISPLAY_NAME).removeIf(uuidMatches);
				
				// Update add player display name
				for (Item i : queuedPackets.get(Action.ADD_PLAYER)) {
					if (i.getUuid().equals(item.getUuid())) {
						i.setDisplayName(item.getDisplayName());
						// Do not return as we do need to send this packet as well (due to a MC bug)
						break;
					}
				}
				break;
			case UPDATE_GAMEMODE:
				queuedPackets.get(Action.UPDATE_GAMEMODE).removeIf(uuidMatches);
				
				// Update add player gamemode rather than send other packet
				for (Item i : queuedPackets.get(Action.ADD_PLAYER)) {
					if (i.getUuid().equals(item.getUuid())) {
						i.setGamemode(item.getGamemode());
						return;
					}
				}
				break;
			case UPDATE_LATENCY:
				queuedPackets.get(Action.UPDATE_LATENCY).removeIf(uuidMatches);
				
				// Update add player ping rather than send other packet
				for (Item i : queuedPackets.get(Action.ADD_PLAYER)) {
					if (i.getUuid().equals(item.getUuid())) {
						i.setPing(item.getPing());
						return;
					}
				}
				break;
			default:
				break;
			}
			
			// Add in a name update due to the MC bug
			if (action == Action.ADD_PLAYER) {
				queuedPackets.put(Action.UPDATE_DISPLAY_NAME, new TabItemBuilder(item.getUuid()).withName(item.getDisplayName()).build());
			}
			
			manager.getLog().fine(Debugger.format("Queueing to %s: %s", getPlayer().getName(), TabUtil.toDebugString(action, item)));
			queuedPackets.put(action, item);
			
			manager.markRequiresSend(this);
		}
	}
	
	// Must ONLY be called from the send queue thread
	void sendQueuedPackets() {
		synchronized(queuedPackets) {
			manager.getLog().info(String.format("Sending queue to %s", getPlayer().getName()));
			
			Predicate<Item> isVisible = (i) -> currentlyVisible.contains(i.getUuid());
			
			// Do removals first
			List<Item> items = getQueue(Action.REMOVE_PLAYER, isVisible);
			
			if (!items.isEmpty()) {
				currentlyVisible.removeAll(Collections2.transform(items, (i) -> i.getUuid()));
				send(Action.REMOVE_PLAYER, items);
			}
			
			// Now do additions
			items = queuedPackets.get(Action.ADD_PLAYER);
			if (!items.isEmpty()) {
				currentlyVisible.addAll(Collections2.transform(items, (i) -> i.getUuid()));
				send(Action.ADD_PLAYER, items);
			}
			
			// Now updates
			items = getQueue(Action.UPDATE_DISPLAY_NAME, isVisible);
			if (!items.isEmpty()) {
				send(Action.UPDATE_DISPLAY_NAME, items);
			}
			
			items = getQueue(Action.UPDATE_LATENCY, isVisible);
			if (!items.isEmpty()) {
				send(Action.UPDATE_LATENCY, items);
			}
			
			items = getQueue(Action.UPDATE_GAMEMODE, isVisible);
			if (!items.isEmpty()) {
				send(Action.UPDATE_GAMEMODE, items);
			}
			
			queuedPackets.clear();
		}
	}
	
	private List<Item> getQueue(Action action, Predicate<Item> predicate) {
		List<Item> original = queuedPackets.get(action);
		List<Item> target = Lists.newCopyOnWriteArrayList(original);
		
		target.removeIf(predicate.negate());
		
		return target;
	}
	
	private void send(Action action, List<Item> items) {
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(action);
		packet.setItems(Iterables.toArray(items, Item.class));
		
		getPlayer().unsafe().sendPacket(packet);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TabHandler)) {
			return false;
		}
		
		return getPlayer().equals(((TabHandler)obj).getPlayer());
	}
	
	@Override
	public int hashCode() {
		return getPlayer().hashCode();
	}
	
	@Override
	public String toString() {
		return "TabHandler: " + getPlayer().getName();
	}
}
