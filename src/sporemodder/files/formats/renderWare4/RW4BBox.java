package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4BBox extends RW4Section {
	public static final int type_code = 0x80005;
	public static final int alignment = 16;
	public float[] min = new float[3];
	public float[] max = new float[3];
	public int unk1, unk2;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		min[0] = in.readLEFloat();
		min[1] = in.readLEFloat();
		min[2] = in.readLEFloat();
		unk1 = in.readLEInt(); // MAYBE: xyradius, the circular radius in the X-Y plane
		max[0] = in.readLEFloat();
		max[1] = in.readLEFloat();
		max[2] = in.readLEFloat();
		unk2 = in.readLEInt(); // MAYBE: radius, the spherical radius
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeLEFloats(min);
		out.writeLEInt(unk1);
		out.writeLEFloats(max);
		out.writeLEInt(unk2);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tmin: [" + min[0]+", "+min[1]+", "+min[2]+"]");
		System.out.println("\tmax: [" + max[0]+", "+max[1]+", "+max[2]+"]");
		System.out.println("\tunk1: " + unk1 + "\t" + Hasher.getFileName(unk1));
		System.out.println("\tunk2: " + unk2 + "\t" + Hasher.getFileName(unk2));
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
