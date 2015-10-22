package au.com.addstar.bchat.channels;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.Storable;

public class ChatChannel implements Storable {
	private final String name;
	private final Set<GlobalPlayer> subscribers;
	
	private Optional<String> listenPermission;
	private ChannelScope scope;
	
	protected final ChatChannelManager manager;
	
	ChatChannel(String name, Optional<String> listenPermission, ChatChannelManager manager) {
		this.name = name;
		this.listenPermission = listenPermission;
		this.manager = manager;
		
		subscribers = Sets.newHashSet();
		scope = ChannelScope.GLOBAL;
	}
	
	ChatChannel(String name, ChatChannelManager manager) {
		this(name, Optional.absent(), manager);
	}
	
	public String getName() {
		return name;
	}
	
	public Optional<String> getListenPermission() {
		return listenPermission;
	}
	
	public void setListenPermission(Optional<String> permission) {
		listenPermission = permission;
	}
	
	public Set<GlobalPlayer> getSubscribers() {
		return Collections.unmodifiableSet(subscribers);
	}
	
	public ChannelScope getScope() {
		return scope;
	}
	
	public void setScope(ChannelScope scope) {
		this.scope = scope;
	}
	
	@Override
	public void save(Map<String, String> values) {
		if (listenPermission.isPresent()) {
			values.put("listen", listenPermission.get());
		}
		
		values.put("scope", scope.name());
		values.put("type", "plain");
	}
	
	@Override
	public void load(Map<String, String> values) {
		if (values.containsKey("listen")) {
			listenPermission = Optional.of(values.get("listen"));
		} else {
			listenPermission = Optional.absent();
		}
		
		scope = ChannelScope.valueOf(values.get("scope"));
	}
}
