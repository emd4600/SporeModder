package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4TriangleArray extends RW4Section {
	public int unk1;
	public int indCount;
	public int secNum;
	public int triCount;
	public RW4Buffer triBuffer;
	public static final int type_code = 0x20007;
	public static final int alignment = 4;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		unk1 = in.readLEInt();
		expect(in.readInt(), 0, "RW4-TA001", in.getFilePointer());
		indCount = in.readLEInt();
		expect(in.readLEInt(), 8, "RW4-TA002", in.getFilePointer());
		expect(in.readLEInt(), 101, "RW4-TA003", in.getFilePointer());
		expect(in.readLEInt(), 4, "RW4-TA004", in.getFilePointer());
		secNum = in.readLEInt();
		if (indCount % 3 != 0) System.out.println("RW4-TA010");
		triCount = indCount / 3;
		triBuffer = (RW4Buffer) sections.get(secNum);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		secNum = sections.indexOf(triBuffer);
		out.writeLEInt(unk1);
		out.writeInt(0);
		out.writeLEInt(indCount);
		out.writeLEInt(8);
		out.writeLEInt(101);
		out.writeLEInt(4);
		out.writeLEInt(secNum);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\ttriangle buffer section: " + secNum);
		System.out.println("\ttriangle count: " + triCount);
		System.out.println("\tunk1: " + unk1 + "\t" + Hasher.hashToHex(unk1));
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
