package au.com.addstar.bchat.channels;

import au.com.addstar.bchat.packets.BasePacket;
import au.com.addstar.bchat.packets.BroadcastPacket;
import au.com.addstar.bchat.packets.SendPacket;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;

public class ChannelPacketListener implements ChannelDataReceiver<BasePacket> {
	private final ChannelHandler handler;
	
	public ChannelPacketListener(ChannelHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void onDataReceive(Channel<BasePacket> channel, BasePacket packet, int sourceId, boolean isBroadcast) {
		if (packet instanceof BroadcastPacket) {
			handleBroadcast((BroadcastPacket)packet);
		} else if (packet instanceof SendPacket) {
			handleSend((SendPacket)packet);
		}
	}
	
	private void handleBroadcast(BroadcastPacket packet) {
		handler.handleIncomming(packet);
	}
	
	private void handleSend(SendPacket packet) {
		// TODO: See if target player is online
	}
}
