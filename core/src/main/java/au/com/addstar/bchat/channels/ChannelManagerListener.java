package au.com.addstar.bchat.channels;

import java.util.concurrent.ExecutorService;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.ReloadPacket;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;

public class ChannelManagerListener implements ChannelDataReceiver<BasePacket> {
	private final ChatChannelManager manager;
	private final ExecutorService service;
	
	public ChannelManagerListener(ChatChannelManager manager, ExecutorService service) {
		this.manager = manager;
		this.service = service;
	}
	
	@Override
	public void onDataReceive(Channel<BasePacket> channel, BasePacket packet, int sourceId, boolean isBroadcast) {
		if (packet instanceof ReloadPacket) {
			// Force the reload
			service.submit(() -> {
				manager.load();
			});
		}
	}

}
