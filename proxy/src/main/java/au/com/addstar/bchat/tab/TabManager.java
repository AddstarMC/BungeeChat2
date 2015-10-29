package au.com.addstar.bchat.tab;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import au.com.addstar.bchat.Debugger;
import au.com.addstar.bchat.attachments.StateAttachment;
import au.com.addstar.bchat.groups.Group;
import au.com.addstar.bchat.groups.GroupManager;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;

public class TabManager {
	private final GroupManager groupManager;
	private final Logger debugLogger;
	private final long queueInterval;
	
	// Access to handlers must be synchronized on handlers
	private final Map<UUID, TabHandler> handlers;
	
	// Access to orderedMarkedHandlers and markedHandlers must be synchronized on markedHandlers
	private final TreeMultimap<Long, TabHandler> orderedMarkedHandlers;
	private final Set<TabHandler> markedHandlers;
	
	public TabManager(GroupManager groupManager, long queueInterval, TimeUnit queueIntervalUnit) {
		this.groupManager = groupManager;
		this.queueInterval = queueIntervalUnit.toMillis(queueInterval);
		
		debugLogger = Debugger.getLogger(Debugger.Tab);
		handlers = Maps.newConcurrentMap();
		
		markedHandlers = Sets.newHashSet();
		orderedMarkedHandlers = TreeMultimap.create(Ordering.natural(), Ordering.allEqual());
	}
	
	public void onConnect(ProxiedPlayer player) {
		TabHandler handler = new TabHandler(this);
		player.setTabListHandler(handler);
		
		handlers.put(player.getUniqueId(), handler);
	}
	
	public void onDisconnect(ProxiedPlayer player) {
		handlers.remove(player.getUniqueId());
	}
	
	public void onStateUpdate(GlobalPlayer player) {
		TabHandler handler = handlers.get(player.getUniqueId());
		if (handler != null) {
			handler.onUpdateName();
		}
	}
	
	/**
	 * Starts the queue sending task. This MUST be run before any packets are queued
	 * @param plugin The owning plugin
	 * @param scheduler The scheduler
	 */
	public void startSendingTask(Plugin plugin, TaskScheduler scheduler) {
		scheduler.schedule(plugin, new QueueSender(), 5, 5, TimeUnit.MILLISECONDS);
	}
	
	Logger getLog() {
		return debugLogger;
	}
	
	TabHandler getHandlerFor(ProxiedPlayer player) {
		return handlers.get(player.getUniqueId());
	}
	
	TabHandler getHandlerFor(GlobalPlayer player) {
		return handlers.get(player.getUniqueId());
	}
	
	public Group getGroup(GlobalPlayer player) {
		StateAttachment attachment = player.getAttachment(StateAttachment.class);
		Group group = null;
		if (attachment != null) {
			group = groupManager.getGroup(attachment.getGroupName());
		}
		
		if (group != null) {
			return group;
		} else {
			return groupManager.getGroups().get(0);
		}
	}
	
	/**
	 * Queues an update to the tab list. this will be sent soon after
	 * being queued, but may be combined with other updates
	 * @param from The handler sending this
	 * @param action The action for the item
	 * @param item The item to send
	 */
	void queue(TabHandler from, PlayerListItem.Action action, PlayerListItem.Item item) {
		// TODO: Player Visibility
		
		synchronized (handlers) {
			for (TabHandler handler : handlers.values()) {
				handler.queue(action, item);
			}
		}
	}
	
	/**
	 * Updates the tab list for this player
	 * @param player The player to update
	 */
	public void refresh(ProxiedPlayer player) {
		TabHandler myHandler = handlers.get(player.getUniqueId());
		
		// TODO: Player Visibility
		synchronized (handlers) {
			for (TabHandler handler : handlers.values()) {
				myHandler.queue(Action.ADD_PLAYER, handler.getAddItem());
			}
		}
	}

	/**
	 * Used to mark that this handler needs to send
	 * @param tabHandler The handler
	 * @note This must only be called from {@link TabHandler#queue(Action, net.md_5.bungee.protocol.packet.PlayerListItem.Item)}
	 */
	void markRequiresSend(TabHandler tabHandler) {
		synchronized (markedHandlers) {
			if (markedHandlers.add(tabHandler)) {
				debugLogger.finer(Debugger.format("Marking to send %s", tabHandler.getPlayer().getName()));
				orderedMarkedHandlers.put(System.currentTimeMillis() + queueInterval, tabHandler);
			} else {
				return;
			}
		}
	}
	
	private class QueueSender implements Runnable {
		@Override
		public void run() {
			synchronized (markedHandlers) {
				if (orderedMarkedHandlers.isEmpty()) {
					return;
				}
				
				NavigableMap<Long, Collection<TabHandler>> toProcess = orderedMarkedHandlers.asMap().headMap(System.currentTimeMillis(), true);
				for (Collection<TabHandler> handlers : toProcess.values()) {
					for (TabHandler handler : handlers) {
						handler.sendQueuedPackets();
						markedHandlers.remove(handler);
						debugLogger.finer(Debugger.format("Removing send mark %s", handler.getPlayer().getName()));
					}
				}
				
				toProcess.clear();
			}
		}
	}
}
