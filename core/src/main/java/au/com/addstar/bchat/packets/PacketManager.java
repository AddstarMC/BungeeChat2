package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.cubespace.geSuit.core.channel.ChannelCodec;

public final class PacketManager {
	private final BiMap<Integer, Class<? extends BasePacket>> registry;
	
	public PacketManager() {
		registry = HashBiMap.create();
		
		// Default packets
		registerPacket(0, ReloadPacket.class);
		registerPacket(1, BroadcastPacket.class);
		registerPacket(2, SendPacket.class);
		registerPacket(3, RefreshPacket.class);
		registerPacket(4, PostFormatBroadcastPacket.class);
		registerPacket(5, SubscriberChangePacket.class);
	}
	
	public void registerPacket(int id, Class<? extends BasePacket> packetType) {
		registry.put(id, packetType);
	}
	
	public int getId(Class<? extends BasePacket> packetType) {
		return registry.inverse().get(packetType);
	}
	
	public BasePacket create(int id) {
		Class<? extends BasePacket> packetType = registry.get(id);
		if (packetType == null) {
			throw new IllegalArgumentException("Unknown packet id");
		}
		
		try {
			return packetType.newInstance();
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error in packet " + packetType + ". Constructor cannot be accessed", e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Error in packet " + packetType + ".", e);
		}
	}
	
	public ChannelCodec<BasePacket> createCodec() {
		return new PacketCodec();
	}
	
	private class PacketCodec implements ChannelCodec<BasePacket> {
		@Override
		public void encode(BasePacket value, DataOutput out) throws IOException {
			int id = getId(value.getClass());
			out.writeByte(id);
			value.write(out);
		}
		
		@Override
		public BasePacket decode(DataInput in) throws IOException {
			BasePacket packet = create(in.readByte());
			packet.read(in);
			
			return packet;
		}
	}
}
