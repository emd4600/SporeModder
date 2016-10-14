package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Skeleton extends RW4Section {
	public static final int type_code = 0x7000c;
	public static final int alignment = 4;
	public RW4Matrices4x3 mat4x3; //It look like some kind of mask, always [1, 0, 0], [0, 0, 1], [0, 0, 0], [0, 1, 0]
	public RW4Matrices4x4 mat4x4;
	public RW4HierarchyInfo jointInfo;
	private int mat4x3_secNum, mat4x4_secNum, jointInfo_secNum;
	@Override
	public void read(InputStreamAccessor in,  List<RW4Section> sections) throws IOException {
		expect(in.readLEInt(), 0x00400000, "RW4-SK001", in.getFilePointer());
		expect(in.readLEInt(), 0x008d6da0, "RW4-SK002", in.getFilePointer());
		mat4x3_secNum = in.readLEInt();
		jointInfo_secNum = in.readLEInt();
		mat4x4_secNum = in.readLEInt();
		
		mat4x3 = (RW4Matrices4x3) sections.get(mat4x3_secNum);
		jointInfo = (RW4HierarchyInfo) sections.get(jointInfo_secNum);
		mat4x4 = (RW4Matrices4x4) sections.get(mat4x4_secNum);
	}
	
	@Override
	public void write(OutputStreamAccessor out,  List<RW4Section> sections) throws IOException {
		mat4x3_secNum = sections.indexOf(mat4x3);
		mat4x4_secNum = sections.indexOf(mat4x4);
		jointInfo_secNum = sections.indexOf(jointInfo);
		out.writeLEInt(0x00400000);
		out.writeLEInt(0x008d6da0); // Skeleton type? It's different in limbs, for example
		out.writeLEInt(mat4x3_secNum);
		out.writeLEInt(jointInfo_secNum);
		out.writeLEInt(mat4x4_secNum);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tmatrices 4x3 section: " + mat4x3_secNum);
		System.out.println("\tmatrices 4x4 section: " + mat4x4_secNum);
		System.out.println("\tjoint info section: " + jointInfo_secNum);
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
