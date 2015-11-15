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
	public BaseComponent[] highlightedMessage;
	
	public BroadcastPacket() {
	}
	
	public BroadcastPacket(ChatChannel channel, BaseComponent[] message) {
		this.channelId = channel.getName();
		this.message = message;
	}
	
	public BroadcastPacket(ChatChannel channel, BaseComponent[] message, BaseComponent[] highlightedMessage) {
		this.channelId = channel.getName();
		this.message = message;
		this.highlightedMessage = highlightedMessage;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBoolean(highlightedMessage != null);
		out.writeUTF(channelId);
		out.writeUTF(ComponentSerializer.toString(message));
		if (highlightedMessage != null) {
			out.writeUTF(ComponentSerializer.toString(highlightedMessage));
		}
	}

	@Override
	public void read(DataInput in) throws IOException {
		boolean hasHighlightVersion = in.readBoolean();
		channelId = in.readUTF();
		
		String json = in.readUTF();
		try {
			message = ComponentSerializer.parse(json);
		} catch (Throwable e) {
			throw new IOException(e);
		}
		
		if (hasHighlightVersion) {
			json = in.readUTF();
			try {
				highlightedMessage = ComponentSerializer.parse(json);
			} catch (Throwable e) {
				throw new IOException(e);
			}
		} else {
			highlightedMessage = null;
		}
	}
}
