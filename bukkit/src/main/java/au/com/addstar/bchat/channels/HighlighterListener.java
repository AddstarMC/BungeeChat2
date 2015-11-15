package au.com.addstar.bchat.channels;

import java.util.concurrent.ExecutorService;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.ReloadPacket;
import au.com.addstar.bchat.packets.ReloadPacket.ReloadType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;

public class HighlighterListener implements ChannelDataReceiver<BasePacket> {
	private final Highlighter highlighter;
	private final ExecutorService service;
	
	public HighlighterListener(Highlighter highlighter, ExecutorService service) {
		this.highlighter = highlighter;
		this.service = service;
	}
	
	@Override
	public void onDataReceive(Channel<BasePacket> channel, BasePacket packet, int sourceId, boolean isBroadcast) {
		if (packet instanceof ReloadPacket) {
			ReloadPacket reloadPacket = (ReloadPacket)packet;
			if (reloadPacket.type == ReloadType.All || reloadPacket.type == ReloadType.Highlighter) {
				// Force the reload
				service.submit(() -> {
					highlighter.load();
				});
			}
		}
	}
}
