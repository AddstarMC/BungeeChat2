package au.com.addstar.bchat.channels;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.GlobalPlayer;

public class ChatChannel {
	private final String name;
	private final Optional<String> listenPermission;
	private final Set<GlobalPlayer> subscribers;
	
	public ChatChannel(String name, Optional<String> listenPermission) {
		this.name = name;
		this.listenPermission = listenPermission;
		
		subscribers = Sets.newHashSet();
	}
	
	public String getName() {
		return name;
	}
	
	public Optional<String> getListenPermission() {
		return listenPermission;
	}
	
	public Set<GlobalPlayer> getSubscribers() {
		return Collections.unmodifiableSet(subscribers);
	}
}
