package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4SimpleMesh extends RW4Section {
	public static final int type_code = 0x80003;
	public static final int alignment = 16;
	public int unk1;
	public RW4BBox bbox, bbox2;
	public UnknownData[] unk_data_2;
	public float[][] vertices;
	public int[][] triangles;
	public ArrayList<Integer> triUnks;
	public int tri_count, vert_count, u2_count;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		System.out.println(in.getFilePointer());
		bbox = new RW4BBox();
		bbox.read(in, sections);
		expect(in.readLEInt(), 0x00D59208, "RW4-SM001", in.getFilePointer()); // most but not all creature models
		unk1 = in.readLEInt();
		tri_count = in.readLEInt();
		expect(in.readInt(), 0, "RW4-SM002", in.getFilePointer());
		vert_count = in.readLEInt();
		int p2 = in.readLEInt();
		int p1 = in.readLEInt();
		int p4 = in.readLEInt();
		int p3 = in.readLEInt();
		if (p1 != ((in.getFilePointer() +15) & ~15)) addError("RW4-SM003", in.getFilePointer());
		in.seek(p1);
		//in.skipBytes((int)(p1 - in.getFilePointer()));
		if (p2 != p1 + vert_count * 16) addError("RW4-SM004", in.getFilePointer());
		if (p3 != p2 + tri_count * 16) addError("RW4-SM005", in.getFilePointer());
		if (p4 != p3 + (((tri_count/2)+15)&~15)) addError("RW4-SM006", in.getFilePointer());
		vertices = new float[vert_count][3];
		for (int i = 0; i < vert_count; i++) {
			vertices[i] = new float[3];
			vertices[i][0] = in.readLEFloat();
			vertices[i][1] = in.readLEFloat();
			vertices[i][2] = in.readLEFloat();
			expect(in.readInt(), 0, "RW4-SM007", in.getFilePointer());
		}
		triangles = new int[tri_count][3];
		for (int i = 0; i < tri_count; i++) {
			triangles[i] = new int[3];
			triangles[i][0] = in.readLEInt();
			triangles[i][1] = in.readLEInt();
			triangles[i][2] = in.readLEInt();
			expect(in.readInt(), 0, "RW4-SM008", in.getFilePointer());
		}
		int x = 0;
		triUnks = new ArrayList<Integer>();
        for (int i = 0; i < tri_count; i++) {
        	// if it's divisible per 8
            if ((i & 7) == 0)
                x = in.readLEInt();
            triUnks.add(x >> ((i & 7) * 4) & 0xf);
        }
        for (int t = tri_count; t < ((tri_count + 7) & ~7); t++) {
        	if ((byte)((x >> ((t & 7) * 4)) & 0xf) != 0xf) System.out.println("RW4-SM009; Unexpected in pos: " + in.getFilePointer());
        }
        //in.skipBytes((int)(p4 - in.getFilePointer()));
        in.seek(p4);
        System.out.println(in.getFilePointer());
        expect(in.readLEInt(), p1 - 8*4, "RW4-SM010", in.getFilePointer());
        u2_count = in.readLEInt();
        expect(in.readLEInt(), tri_count, "RW4-SM011", in.getFilePointer());
        expect(in.readInt(), 0, "RW4-SM012", in.getFilePointer());
        bbox2 = new RW4BBox();
        bbox2.read(in, sections);
        unk_data_2 = new UnknownData[u2_count];  // Actually this is int*6 + float*2
        for (int i = 0; i < unk_data_2.length; i++) {
        	unk_data_2[i] = new UnknownData();
        	unk_data_2[i].read(in);
        }
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		bbox.write(out, sections);
		out.writeLEInt(0x00D59208);
		out.writeLEInt(unk1);
		out.writeLEInt(tri_count);
		out.writeLEInt(0);
		out.writeLEInt(vert_count);
		
		int p1 = (out.getFilePointer() + 31) & ~15;
		int p4 = p1 + vert_count * 16 + tri_count * 16 + (((tri_count / 2) + 15) & ~15);
		
		out.writeLEInt(p1 + vert_count * 16);
		out.writeLEInt(p1);
		out.writeLEInt(p4);
		out.writeLEInt(p1 + vert_count * 16 + tri_count * 16);
		
		out.write(new byte[p1 - out.getFilePointer()]);
		
		for (float[] vertex : vertices) {
			out.writeLEFloats(vertex);
			out.writeInt(0);
		}
		for (int[] tri : triangles) {
			out.writeLEInts(tri);
			out.writeInt(0);
		}
		//TODO TRI UNKS
		int todo = 0;
	}
	
	public class UnknownData {
		public int[] unkInt = new int[6];
		public float[] unkFloat = new float[2];
		public void read(InputStreamAccessor in) throws IOException {
			unkInt[0] = in.readLEInt(); unkInt[1] = in.readLEInt(); unkInt[2] = in.readLEInt(); 
			unkInt[3] = in.readLEInt(); unkInt[4] = in.readLEInt(); unkInt[5] = in.readLEInt();
			unkFloat[0] = in.readLEFloat(); unkFloat[1] = in.readLEFloat();
//			if (unkInt[5] < 1) System.out.println("5th COL LESS THAN 1: " + unkInt[5]);
//			if (unkInt[3] >= tri_count) System.out.println("4th COL BIGGER THAN TRIANGLE COUNT (" + tri_count + "): " + unkInt[3]);
//			if (unkInt[5] >= tri_count) System.out.println("6th COL BIGGER THAN TRIANGLE COUNT (" + tri_count + "): " + unkInt[5]);
//			if (unkFloat[0] < -3 || unkFloat[0] > 3) System.out.println("1st UV: " + unkFloat[0]);
//			if (unkFloat[1] < -3 || unkFloat[1] > 3) System.out.println("2nd UV: " + unkFloat[1]);
//			System.out.println(unkInt[0]+"\t"+unkInt[1]+"\t"+unkInt[2]+"\t"+unkInt[3]+"\t"+unkInt[4]+"\t"+unkInt[5]+"  |  "+unkFloat[0]+"\t"+unkFloat[1]);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tbbox 1: [" + bbox.min[0]+", "+bbox.min[1]+", "+bbox.min[2]+"] [" + bbox.max[0]+", "+bbox.max[1]+", "+bbox.max[2]+"] " 
		+ bbox.unk1 + "\t" + Hasher.hashToHex(bbox.unk1)+ "\t"+bbox.unk2 + "\t" + Hasher.hashToHex(bbox.unk2));
		System.out.println("\tunk1: " + unk1 + "\t" + Hasher.hashToHex(unk1));
		System.out.println("\ttriangle count: " + tri_count);
		System.out.println("\tvertex count: " + vert_count);
		System.out.println("\tunknown data count: " + u2_count);
		System.out.println("\tbbox 2: [" + bbox2.min[0]+", "+bbox2.min[1]+", "+bbox2.min[2]+"] [" + bbox2.max[0]+", "+bbox2.max[1]+", "+bbox2.max[2]+"] "
				+ bbox2.unk1 + "\t" + Hasher.hashToHex(bbox2.unk1)+ "\t"+bbox2.unk2 + "\t" + Hasher.hashToHex(bbox2.unk2));
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
