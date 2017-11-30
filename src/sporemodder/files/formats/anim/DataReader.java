package sporemodder.files.formats.anim;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class DataReader {
	
	private byte[] data;
	private int pointer;

	public DataReader(byte[] data) {
		this.data = data;
		this.pointer = 0;
	}
	
	public DataReader(DataReader other, int pointer) {
		this.data = other.data;
		this.pointer = pointer;
	}
	
	public int getInt(int offset) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, pointer + offset, pointer + offset + 4)).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	public float getFloat(int offset) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, pointer + offset, pointer + offset + 4)).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	
	// Treats offset as an array
	public int getInt(int offset, int index) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, pointer + offset + index*4, pointer + offset + index*4 + 4)).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	// Treats offset as an array
	public float getFloat(int offset, int index) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, pointer + offset + index*4, pointer + offset + index*4 + 4)).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
}
