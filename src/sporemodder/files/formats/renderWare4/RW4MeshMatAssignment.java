package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4MeshMatAssignment extends RW4Section {
	public static final int type_code = 0x2001a;
	public static final int alignment = 4;
	public RW4Mesh mesh;
	public RW4TexMetadata[] texMetadatas;
	public int tmCount, mesh_secNum;
	public int[] tm_secNums;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		mesh_secNum = in.readLEInt();
		tmCount = in.readLEInt();
		tm_secNums = new int[tmCount];
		for (int i = 0; i < tmCount; i++) {
			tm_secNums[i] = in.readLEInt();
		}
		mesh = (RW4Mesh) sections.get(mesh_secNum);
		texMetadatas = new RW4TexMetadata[tmCount];
		for (int i = 0; i < tmCount; i++) {
			texMetadatas[i] = (RW4TexMetadata)sections.get(tm_secNums[i]);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		mesh_secNum = sections.indexOf(mesh);
		tm_secNums = new int[texMetadatas.length];
		for (int i = 0; i < tm_secNums.length; i++) {
			tm_secNums[i] = sections.indexOf(texMetadatas[i]);
		}
		out.writeLEInt(mesh_secNum);
		out.writeLEInt(tmCount);
		out.writeLEInts(tm_secNums);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tmesh section: " + mesh_secNum);
		System.out.println("\ttex metadata count: " + tmCount);
		System.out.println("\ttex metadata section: " + tm_secNums[0]);
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
