package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Matrices4x4 extends RW4Section {
	public static final int type_code = 0x70003;
	public static final int alignment = 16;
	public Mat4x4[] matrices;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		int p1 = in.readLEInt();
		int count = in.readLEInt();
		expect(in.readInt(), 0, "RW4-MAT001", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-MAT002", in.getFilePointer());
		expect(in.getFilePointer(), p1, "RW4-MAT003", in.getFilePointer());
		matrices = new Mat4x4[count];
		for (int i = 0; i < count; i++) {
			matrices[i] = new Mat4x4();
			matrices[i].read(in);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeLEInt(out.getFilePointer() + 16);
		out.writeLEInt(matrices.length);
		out.writeInt(0);
		out.writeInt(0);
		for (Mat4x4 matrix : matrices) {
			matrix.write(out);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		for (Mat4x4 mt : matrices) {
			mt.print();
		}
	}
	
	public class Mat4x4 {
		public float[] values;
		public void read(InputStreamAccessor in) throws IOException {
			values = new float[16];
			for (int i = 0; i < 16; i++) {
				values[i] = in.readLEFloat();
			}
		}
		public void write(OutputStreamAccessor out) throws IOException {
			out.writeLEFloats(values);
		}
		public void print() {
			System.out.println("\t["+values[0]+", "+values[1]+", "+values[2]+", "+values[3]+"] ["+values[4]+", "+values[5]+", "+values[6]+", "+values[7]+"] ["
		+values[8]+", "+values[9]+", "+values[10]+", "+values[11]+"] ["+values[12]+", "+values[13]+", "+values[14]+", "+values[15]+"]");
		}
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
