package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Matrices4x3 extends RW4Section {
	public static final int type_code = 0x7000f;
	public static final int alignment = 16;
	public Mat4x3[] matrices;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		int p1 = in.readLEInt();
		int count = in.readLEInt();
		expect(in.readInt(), 0, "RW4-MAT001", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-MAT002", in.getFilePointer());
		expect(in.getFilePointer(), p1, "RW4-MAT003", in.getFilePointer());
		matrices = new Mat4x3[count];
		for (int i = 0; i < count; i++) {
			matrices[i] = new Mat4x3();
			matrices[i].read(in);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeLEInt(out.getFilePointer() + 16);
		out.writeLEInt(matrices.length);
		out.writeInt(0);
		out.writeInt(0);
		for (Mat4x3 matrix : matrices) {
			matrix.write(out);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		for (Mat4x3 mt : matrices) {
			mt.print();
		}
	}
	
	public class Mat4x3 {
		public float[] values = new float[9];
		public void read(InputStreamAccessor in) throws IOException {
			values[0] = in.readLEFloat(); values[1] = in.readLEFloat(); values[2] = in.readLEFloat();
			expect(in.readInt(), 0, "RW4-MAT004", in.getFilePointer());
			values[3] = in.readLEFloat(); values[4] = in.readLEFloat(); values[5] = in.readLEFloat();
			expect(in.readInt(), 0, "RW4-MAT004", in.getFilePointer());
			values[6] = in.readLEFloat(); values[7] = in.readLEFloat(); values[8] = in.readLEFloat();
			expect(in.readInt(), 0, "RW4-MAT004", in.getFilePointer());
		}
		public void write(OutputStreamAccessor out) throws IOException {
			out.writeLEFloat(values[0]); out.writeLEFloat(values[1]); out.writeLEFloat(values[2]); out.writeLEInt(0);
			out.writeLEFloat(values[3]); out.writeLEFloat(values[4]); out.writeLEFloat(values[5]); out.writeLEInt(0);
			out.writeLEFloat(values[6]); out.writeLEFloat(values[7]); out.writeLEFloat(values[8]); out.writeLEInt(0);
		}
		public void print() {
			System.out.println("\t["+values[0]+", "+values[1]+", "+values[2]+"] ["+values[3]+", "+values[4]+", "+values[5]+"] ["+values[6]+", "+values[7]+", "
		+values[8]+"]");
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
