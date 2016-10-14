package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4ModelHandle extends RW4Section {
	public static final int type_code = 0xff0000;
	public static final int alignment = 4;
	public int handleId;
	public float unk1, unk2, unk3; 
	public float unk4, unk5, unk6, unk7, unk8, unk9, unk10, unk11, unk12, unk13; 
	public float defaultValue;
	public int secNum;
	public RW4Anim anim;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		handleId = in.readLEInt();
		unk1 = in.readLEFloat();
		unk2 = in.readLEFloat();
		unk3 = in.readLEFloat(); //x ?
		unk4 = in.readLEFloat();
		unk5 = in.readLEFloat(); // y ?
		unk6 = in.readLEFloat();// z ?
		unk7 = in.readLEFloat();
		unk8 = in.readLEFloat();
		unk9 = in.readLEFloat();
		unk10 = in.readLEFloat();
		unk11 = in.readLEFloat();
		unk12 = in.readLEFloat();
		unk13 = in.readLEFloat();
		defaultValue = in.readLEFloat();
		secNum = in.readLEInt();
		anim = (RW4Anim) sections.get(secNum);
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		secNum = sections.indexOf(anim);
		out.writeLEInt(handleId);
		out.writeLEFloats(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9, unk10, unk11, unk12, unk13);
		out.writeLEFloat(defaultValue);
		out.writeLEInt(secNum);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\thandle id: " + Hasher.getFileName(handleId));
		System.out.println("\tanim section: " + secNum);
		System.out.println("\tunk1: " + unk1 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk1)));
		System.out.println("\tunk2: " + unk2 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk2)));
		System.out.println("\tunk3: " + unk3 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk3)));
		System.out.println("\tunk4: " + unk4 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk4)));
		System.out.println("\tunk5: " + unk5 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk5)));
		System.out.println("\tunk6: " + unk6 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk6)));
		System.out.println("\tunk7: " + unk7 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk7)));
		System.out.println("\tunk8: " + unk8 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk8)));
		System.out.println("\tunk9: " + unk9 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk9)));
		System.out.println("\tunk10: " + unk10 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk10)));
		System.out.println("\tunk11: " + unk11 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk11)));
		System.out.println("\tunk12: " + unk12 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk12)));
		System.out.println("\tunk13: " + unk13 + "\t" + Integer.toHexString(Float.floatToRawIntBits(unk13)));
		System.out.println("\tdefaultValue: " + defaultValue + "\t" + Integer.toHexString(Float.floatToRawIntBits(defaultValue)));
		System.out.println("\t[" + unk3 + ", " + unk5 + ", " + unk7 + "]\t(" + Integer.toHexString(Float.floatToRawIntBits(unk2)) 
				+ ", " + Integer.toHexString(Float.floatToRawIntBits(unk4)) + ", " + Integer.toHexString(Float.floatToRawIntBits(unk6)) + ")");
		System.out.println("\t[" + unk9 + ", " + unk11 + ", " + unk13 + "]\t(" + Integer.toHexString(Float.floatToRawIntBits(unk8)) 
				+ ", " + Integer.toHexString(Float.floatToRawIntBits(unk10)) + ", " + Integer.toHexString(Float.floatToRawIntBits(unk12)) + ")");
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
