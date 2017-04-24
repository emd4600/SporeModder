package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4VertexArray extends RW4Section {
	public static final int type_code = 0x20005;
	public static final int alignment = 4;
	
	public RW4VertexFormat vertexFormat;
	public RW4Buffer vertexBuffer;
	
	private int unk1;
	private int baseVertexIndex;
	private int vertexCount, vertexSize;
	private int vertSection;
	private int vertFormatSection;
	
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		vertFormatSection = in.readLEInt();
		vertexFormat = (RW4VertexFormat)sections.get(vertFormatSection);
		
		unk1 = in.readLEInt();
		expect(baseVertexIndex = in.readLEInt(), 0, "RW4-VA001", in.getFilePointer());
		vertexCount = in.readLEInt();
//		System.out.println("vertex count: " + vertexCount);
		expect(in.readLEInt(), 8, "RW4-VA002", in.getFilePointer());
		
		vertexSize = in.readLEInt();
		vertSection = in.readLEInt();
		vertexBuffer = (RW4Buffer)sections.get(vertSection);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		vertFormatSection = sections.indexOf(vertexFormat);
		vertSection = sections.indexOf(vertexBuffer);
		out.writeLEInt(vertFormatSection);
		out.writeLEInt(unk1);
		out.writeLEInt(baseVertexIndex);
		out.writeLEInt(vertexCount);
		out.writeLEInt(8);
		out.writeLEInt(vertexSize);
		out.writeLEInt(vertSection);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tvertex buffer section: " + vertSection);
		System.out.println("\tvertex format section: " + vertFormatSection);
		System.out.println("\tvertex count: " + vertexCount);
		System.out.println("\tvertex size: " + vertexSize);
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
