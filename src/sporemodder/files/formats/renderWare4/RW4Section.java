package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.utilities.Hasher;

public abstract class RW4Section extends FileStructure {
	protected static final int INFO_SIZE = 24;
	
	public SectionInfo sectionInfo;
	public static int type_code;
	public abstract void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException;
	public abstract void write(OutputStreamAccessor out,  List<RW4Section> sections) throws IOException;
	public abstract void print();
	public void printInfo() {
		System.out.println("SECTION " + sectionInfo.number);
		System.out.println("\tpos: " + sectionInfo.pos);
		System.out.println("\tsize: " + sectionInfo.size);
		System.out.println("\talignment: " + sectionInfo.alignment);
		System.out.println("\ttype_code_indirect: " + sectionInfo.type_code_indirect);
		System.out.println("\ttype_code: " + Hasher.hashToHex(sectionInfo.type_code) + " so " + this.getClass().getSimpleName());
	}
	
	public abstract int getSectionTypeCode();
	public abstract int getSectionAlignment();
	
	public RW4Section() {
		sectionInfo = new SectionInfo(getSectionTypeCode(), getSectionAlignment());
	}

	public static class SectionInfo extends FileStructure {
		public int number;
		public int pos;
		public int size;
		public int alignment;
		public int type_code;
		public int type_code_indirect;
		
		private SectionInfo(int type, int alignment) {
			this.type_code = type;
			this.alignment = alignment;
		}
		
		public SectionInfo() {
			// TODO Auto-generated constructor stub
		}

		public void read(InputStreamAccessor in, int secIndEnd, int num) throws IOException {
			number = num;
			pos = in.readLEInt();
			expect(in.readInt(), 0, "RW4-HS001", in.getFilePointer());
			size = in.readLEInt();
			alignment = in.readLEInt();
			type_code_indirect = in.readLEInt();
			type_code = in.readLEInt();
			if (type_code == 0x10030) pos += secIndEnd;
		}
		public void write(OutputStreamAccessor out) throws IOException {
			out.writeLEInt(pos);
			out.writeInt(0);
			out.writeLEInt(size);
			out.writeLEInt(alignment);
			out.writeLEInt(type_code_indirect);
			out.writeLEInt(type_code);
		}
	}
	
	public static final Class<? extends RW4Section> getType(int type) throws IOException {
		switch(type) {
		case 0x70003:	return RW4Matrices4x4.class;
		case 0x7000f:	return RW4Matrices4x3.class;
		case 0x20007:	return RW4TriangleArray.class;
		case 0x20005:	return RW4VertexArray.class;
		case 0x80005:	return RW4BBox.class;
		case 0x80003:	return RW4SimpleMesh.class;
		case 0x20009:	return RW4Mesh.class;
		case 0x20003:	return RW4Texture.class;
		case 0x2000b:	return RW4TexMetadata.class;
		case 0x2001a:	return RW4MeshMatAssignment.class;
		case 0x7000b:	return RW4Material.class;
		case 0x70002:	return RW4HierarchyInfo.class;
		case 0x7000c:	return RW4Skeleton.class;
		case 0x70001:	return RW4Anim.class;
		case 0xff0001:	return RW4Animations.class;
		case 0x20004:	return RW4VertexFormat.class;
		case 0xff0000:	return RW4ModelHandle.class;
		case 0x10030:	return RW4Buffer.class;
		case 0xff0002:	return RW4BlendShapeConfig.class;
		case 0x200af:	return RW4Buffer.class; //TODO Some model thing
		//case 0xff0002:	return RW4BlendShapeConfig.class; //TODO Some animations thing
		//case 0x200af:	return RW4BlendShapeMesh.class; //TODO Some model thing
		default: throw new IOException("RW4-S000; Unknown section type: 0x" + Integer.toHexString(type));
		}
	}
}
