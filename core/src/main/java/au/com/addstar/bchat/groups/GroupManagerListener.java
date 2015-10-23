package au.com.addstar.bchat.groups;

import java.util.concurrent.ExecutorService;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.ReloadPacket;
import au.com.addstar.bchat.packets.ReloadPacket.ReloadType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;

public class GroupManagerListener implements ChannelDataReceiver<BasePacket> {
	private final GroupManager manager;
	private final ExecutorService service;
	
	public GroupManagerListener(GroupManager manager, ExecutorService service) {
		this.manager = manager;
		this.service = service;
	}
	
	@Override
	public void onDataReceive(Channel<BasePacket> channel, BasePacket packet, int sourceId, boolean isBroadcast) {
		if (packet instanceof ReloadPacket) {
			ReloadPacket reloadPacket = (ReloadPacket)packet;
			if (reloadPacket.type == ReloadType.All || reloadPacket.type == ReloadType.Groups) {
				// Force the reload
				service.submit(() -> {
					manager.load();
				});
			}
		}
	}
}
