package au.com.addstar.bchat.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class BasePacket {
	public abstract void write(DataOutput out) throws IOException;
	public abstract void read(DataInput in) throws IOException;
}
