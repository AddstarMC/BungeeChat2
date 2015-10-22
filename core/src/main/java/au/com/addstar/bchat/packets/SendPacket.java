package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.util.NetworkUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Represents a message being sent to a specific player
 */
public class SendPacket extends BasePacket {
	public UUID target;
	public BaseComponent[] message;
	
	public SendPacket() {
	}
	
	public SendPacket(UUID target, BaseComponent[] message) {
		this.target = target;
		this.message = message;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		NetworkUtils.writeUUID(out, target);
		out.writeUTF(ComponentSerializer.toString(message));
	}

	@Override
	public void read(DataInput in) throws IOException {
		target = NetworkUtils.readUUID(in);
		
		String json = in.readUTF();
		try {
			message = ComponentSerializer.parse(json);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}
}
