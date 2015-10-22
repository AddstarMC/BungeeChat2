package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import au.com.addstar.bchat.channels.ChatChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Represents a message being sent to a channel
 */
public class BroadcastPacket extends BasePacket {
	public String channelId;
	public BaseComponent[] message;
	public boolean isHighlightChannel;
	
	public BroadcastPacket() {
	}
	
	public BroadcastPacket(ChatChannel channel, BaseComponent[] message, boolean isHighlightChannel) {
		this.channelId = channel.getName();
		this.message = message;
		this.isHighlightChannel = isHighlightChannel;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBoolean(isHighlightChannel);
		out.writeUTF(channelId);
		out.writeUTF(ComponentSerializer.toString(message));
	}

	@Override
	public void read(DataInput in) throws IOException {
		isHighlightChannel = in.readBoolean();
		channelId = in.readUTF();
		
		String json = in.readUTF();
		try {
			message = ComponentSerializer.parse(json);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}
}
