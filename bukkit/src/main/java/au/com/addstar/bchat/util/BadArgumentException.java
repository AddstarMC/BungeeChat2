package au.com.addstar.bchat.util;

import java.util.List;

import com.google.common.collect.Lists;

public class BadArgumentException extends IllegalArgumentException {
	private static final long serialVersionUID = -6202307868577695232L;
	
	private final int index;
	private final List<String> info;
	
	public BadArgumentException(int index, String message) {
		super(message);
		this.index = index;
		info = Lists.newArrayList();
	}
	
	public int getIndex() {
		return index;
	}
	
	public List<String> getInfo() {
		return info;
	}
}
