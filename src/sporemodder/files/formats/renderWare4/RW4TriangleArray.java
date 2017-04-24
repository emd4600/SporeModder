package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4TriangleArray extends RW4Section {
	private int field_0;
	private int startIndex;
	private int indicesCount;
	private int primitiveType = 4;
	private int secNum;
	private RW4Buffer triBuffer;
	
	public static final int type_code = 0x20007;
	public static final int alignment = 4;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		field_0 = in.readLEInt();
		expect(startIndex = in.readLEInt(), 0, "RW4-TA001", in.getFilePointer());
		indicesCount = in.readLEInt();
		expect(in.readLEInt(), 8, "RW4-TA002", in.getFilePointer());
		expect(in.readLEInt(), 101, "RW4-TA003", in.getFilePointer());
		expect(primitiveType = in.readLEInt(), 4, "RW4-TA004", in.getFilePointer());
		secNum = in.readLEInt();
		if (indicesCount % 3 != 0) System.out.println("RW4-TA010");
		triBuffer = (RW4Buffer) sections.get(secNum);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		secNum = sections.indexOf(triBuffer);
		out.writeLEInt(field_0);
		out.writeLEInt(startIndex);
		out.writeLEInt(indicesCount);
		out.writeLEInt(8);
		out.writeLEInt(101);
		out.writeLEInt(primitiveType);
		out.writeLEInt(secNum);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\ttriangle buffer section: " + secNum);
		System.out.println("\ttriangle count: " + (indicesCount / 3));
		System.out.println("\tunk1: " + field_0 + "\t" + Hasher.hashToHex(field_0));
	}
	
	@Override
	public int getSectionTypeCode() {
		return type_code;
	}
	@Override
	public int getSectionAlignment() {
		return alignment;
	}
}
