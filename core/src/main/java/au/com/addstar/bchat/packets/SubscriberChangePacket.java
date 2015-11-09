package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import au.com.addstar.bchat.channels.ChatChannel;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class SubscriberChangePacket extends BasePacket {
	public String channelId;
	public Type type;
	public List<UUID> ids;
	
	public SubscriberChangePacket() {
	}
	
	public SubscriberChangePacket(ChatChannel channel, Type type, Iterable<UUID> ids) {
		this.channelId = channel.getName();
		this.type = type;
		this.ids = Lists.newArrayList(ids);
	}
	
	public SubscriberChangePacket(ChatChannel channel, Type type, UUID... ids) {
		this.channelId = channel.getName();
		this.type = type;
		this.ids = Lists.newArrayList(ids);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		NetworkUtils.writeEnum(out, type);
		out.writeUTF(channelId);
		out.writeShort(ids.size());
		for (UUID id : ids) {
			NetworkUtils.writeUUID(out, id);
		}
	}

	@Override
	public void read(DataInput in) throws IOException {
		type = NetworkUtils.readEnum(in, Type.class);
		channelId = in.readUTF();
		int size = in.readUnsignedShort();
		ids = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; ++i) {
			ids.add(NetworkUtils.readUUID(in));
		}
	}

	public enum Type {
		Add,
		Reset,
		Remove
	}
}
