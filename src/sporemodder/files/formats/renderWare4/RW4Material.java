package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Material extends RW4Section {
	public static final int type_code = 0x7000b;
	public static final int alignment = 4;
	public int unk1;
	public RW4TexMetadata texMet;
	public RW4HierarchyInfo hInfo;
	public int texMet_secNum, hInfo_secNum;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		expect(in.readLEInt(), 0x00400000, "RW4-MT001", in.getFilePointer());
		expect(in.readLEInt(), 0x0063FFB0, "RW4-MT002", in.getFilePointer());
		expect(in.readLEInt(), in.getFilePointer() + 12, "RW4-MT003", in.getFilePointer()); //TODO Not universal //(int)in.getFilePointer() + 16 , but 4 less because it reads it
		expect(in.readLEInt(), 0x00400000, "RW4-MT004", in.getFilePointer());
		hInfo_secNum = in.readLEInt();
		expect(in.readLEInt(), 1, "RW4-MT005", in.getFilePointer());
		texMet_secNum = in.readLEInt();
		expect(in.readLEInt(), 0, "RW4-MT006", in.getFilePointer()); //TODO Not Universal
		
		hInfo = (RW4HierarchyInfo) sections.get(hInfo_secNum);
		texMet = (RW4TexMetadata) sections.get(texMet_secNum);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		hInfo_secNum = sections.indexOf(hInfo);
		texMet_secNum = sections.indexOf(texMet);
		out.writeLEInt(0x00400000);
		out.writeLEInt(0x0063FFB0);
		out.writeLEInt(out.getFilePointer() + 16);
		out.writeLEInt(0x00400000);
		out.writeLEInt(hInfo_secNum);
		out.writeLEInt(1);
		out.writeLEInt(texMet_secNum);
		out.writeInt(0);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\thierarchy info section: " + hInfo_secNum);
		System.out.println("\ttex metadata section: " + texMet_secNum);
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
