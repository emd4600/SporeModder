package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class RW4Header extends FileStructure {
	private static final byte[] CorrectMagic = {(byte)0x89, 0x52, 0x57, 0x34, 0x77, 0x33, 0x32, 0x00, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x20, 0x04
		, 0x00, 0x34, 0x35, 0x34, 0x00, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public byte[] magic = new byte[28];
	protected int file_type;
	protected int section_count;
	protected int section_index_begin;
	protected int section_index_padding;
	protected int section_index_end; //{ get { return (uint)(section_index_begin + 6 * 4 * sections.get()size() + 8 * getFixupCount() + section_index_padding); } }
	protected int section_index_end1;
	protected int unknown_bits_030;
	protected static final int[] fixed_section_types = new int[] { 0, 0x10030, 0x10031, 0x10032, 0x10010 };
	
	protected int fileSize;
	
	public static final int TYPE_MODEL = 1;
	public static final int TYPE_TEXTURE = 0x04000000;
	public static final int TYPE_UNKNOWN = 0xCAFED00D;
	
	protected static final int HEADER_SIZE = 0x98;
	
	public void read(InputStreamAccessor in) throws IOException {
		byte[] magic = new byte[28];
		in.read(magic);
		expect(magic, CorrectMagic, "RW4-H000", in.getFilePointer());
		
		file_type = in.readLEInt();
		
		int ft_const1 = file_type == TYPE_MODEL ? 16 : 4;
		section_count = in.readLEInt();
		expect(in.readLEInt(), section_count, "RW4-H001", in.getFilePointer());
		expect(in.readLEInt(), ft_const1, "RW4-H002", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-H003", in.getFilePointer());
		section_index_begin = in.readLEInt();
		int first_header_section_begin = in.readLEInt(); //Always 0x98? (SM) --> It must be, if not it would crash when checking the position
//		System.out.println("first_header_section_begin: " + Hasher.getPropertyIDString(first_header_section_begin));
		for (int i = 0; i < 3; i++) {
			expect(in.readInt(), 0, "RW4-H004", in.getFilePointer());
		}
		section_index_end1  = in.readLEInt(); //pos 68
		expect(in.readLEInt(), ft_const1, "RW4-H005", in.getFilePointer());
		fileSize = in.readLEInt() + section_index_end1;
		expect(fileSize, in.length(), "RW4-H006", in.getFilePointer());
		int[] ExpectedValues = new int[] { 4, 0, 1, 0, 1 };
		expect(new int[]{in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt()}, ExpectedValues, "RW4-H007", in.getFilePointer());
		unknown_bits_030 = in.readLEInt(); //Why?
		expect(new int[]{in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt()}, ExpectedValues, "RW4-H008", in.getFilePointer());
		expect(new int[]{in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt()}, 
				new int[] { 0, 1, 0, 0, 0, 0, 0 }, "H009", in.getFilePointer());
		if (in.getFilePointer() != 0x98) System.out.println("RW4-H010");
		expect(in.readLEInt(), 0x00010004, "RW4-H011", in.getFilePointer());
		// Offsets of header sections relative to the 0x10004.  4, 12, 28, 28 + (12+4*section_type_count), ... + 36, ... + 28
        int[] offsets = new int[6];
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = in.readLEInt() + first_header_section_begin;
        }
        // A list of section types in the file?  If so, redundant with section index
        if (offsets[2] != in.getFilePointer()) System.out.println("RW4-H012");
        expect(in.readLEInt(), 0x00010005, "RW4-H013", in.getFilePointer());
        int count = in.readLEInt();
        expect(in.readLEInt(), 12, "H014", in.getFilePointer());
        int[] section_types = new int[count];
        in.readLEInts(section_types);

        expect(in.getFilePointer(), offsets[3], "RW4-H015", in.getFilePointer());
        expect(in.readLEInt(), 0x00010006, "RW4-H016", in.getFilePointer());
        // TODO: I think this is actually a variable length structure, with 12 byte header and 3 being the length in qwords
        expect(new int[] {in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt(), in.readLEInt()},
        		new int[] {3, 0x18, file_type, 0xffb00000, file_type, 0, 0, 0}, "RW4-H017", in.getFilePointer());
        expect(in.getFilePointer(), offsets[4], "RW4-H018", in.getFilePointer());
        expect(in.readLEInt(), 0x00010007, "RW4-H019", in.getFilePointer());
        int fixup_count = in.readLEInt();
        expect(in.readInt(), 0, "RW4-H020", in.getFilePointer());
        expect(in.readInt(), 0, "RW4-H021", in.getFilePointer());
        
        // Fixup index always immediately follows section index
        expect(in.readLEInt(), section_index_begin + section_count * 24 + fixup_count * 8, "RW4-H022", in.getFilePointer());
        expect(in.readLEInt(), section_index_begin + section_count * 24, "RW4-H023", in.getFilePointer());
        expect(in.readLEInt(), fixup_count, "RW4-H024", in.getFilePointer());
        expect(in.getFilePointer(), offsets[5], "RW4-H025", in.getFilePointer());
        expect(in.readLEInt(), 0x00010008, "RW4-H026", in.getFilePointer());
        expect(in.readInt(), 0, "RW4-H027", in.getFilePointer());
        expect(in.readInt(), 0, "RW4-H028", in.getFilePointer());
	}
	
	public void write(OutputStreamAccessor out, List<Integer> sectionTypes, List<RW4Section> sections, LinkedHashMap<Integer, Integer> fixups) throws IOException {
		int sectionTypesCount = sectionTypes.size();
		out.write(CorrectMagic);
		out.writeLEInt(file_type);
		out.writeLEInt(sections.size());
		out.writeLEInt(sections.size());
		int constant = (file_type == TYPE_MODEL ? 16 : 4);
		out.writeLEInt(constant);
		out.writeInt(0);
		out.writeLEInt(section_index_begin);
		out.writeLEInt(0x98);
		out.writeInt(0); out.writeInt(0); out.writeInt(0);
		out.writeLEInt(section_index_end);
		out.writeLEInt(constant);
		out.writeLEInt(fileSize - section_index_end);
		out.writeLEInt(4); out.writeLEInt(0); out.writeLEInt(1); out.writeLEInt(0); out.writeLEInt(1);
		out.writeLEInt(0); //0x00C00758
		out.writeLEInt(4); out.writeLEInt(0); out.writeLEInt(1); out.writeLEInt(0); out.writeLEInt(1);
		out.writeLEInt(0); out.writeLEInt(1); out.writeLEInt(0); out.writeLEInt(0); out.writeLEInt(0); out.writeLEInt(0); out.writeLEInt(0);
		out.writeLEInt(0x00010004);
		out.writeLEInt(4); out.writeLEInt(12); out.writeLEInt(28);
		out.writeLEInt(12 + 4*sectionTypesCount + 28);
		out.writeLEInt(36 + 12 + 4*sectionTypesCount + 28);
		out.writeLEInt(28 + 36 + 12 + 4*sectionTypesCount + 28);
		out.writeLEInt(0x00010005);
		out.writeLEInt(sectionTypesCount); 
		out.writeLEInt(12);
		for (Integer i : sectionTypes) {
			out.writeLEInt(i);
		}
		out.writeLEInt(0x00010006);
		out.writeLEInt(3); out.writeLEInt(0x18); out.writeLEInt(file_type); out.writeLEInt(0xffb00000);
		out.writeLEInt(file_type); out.writeLEInt(0); out.writeLEInt(0); out.writeLEInt(0);
		out.writeLEInt(0x00010007); out.writeLEInt(fixups.size()); out.writeLEInt(0); out.writeLEInt(0);
		out.writeLEInt(section_index_begin + sections.size()*24 + fixups.size()*8);
		out.writeLEInt(section_index_begin + sections.size()*24);
		out.writeLEInt(fixups.size());
		out.writeLEInt(0x00010008); out.writeLEInt(0); out.writeLEInt(0);
	}
	
}
