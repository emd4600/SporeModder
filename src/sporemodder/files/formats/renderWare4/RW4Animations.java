package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4Animations extends RW4Section {
	public static final int type_code = 0xff0001;
	public static final int alignment = 4;
	public int numAnims;
	public int unk1;
	public RW4Anim[] anims;
	public int[] animNames;
	public int[] animSections;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		unk1 = in.readLEInt();
		numAnims = in.readLEInt();
		animNames = new int[numAnims];
		animSections = new int[numAnims];
		for (int i = 0; i < numAnims; i++) {
			animNames[i] = in.readLEInt();
			animSections[i] = in.readLEInt();
		}
		anims = new RW4Anim[numAnims];
		for (int i = 0; i < numAnims; i++) {
			anims[i] = (RW4Anim) sections.get(animSections[i]);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeLEInt(unk1);
		out.writeLEInt(animNames.length);
		for (int i = 0; i < animNames.length; i++) {
			animSections[i] = sections.indexOf(anims[i]);
			out.writeLEInt(animNames[i]);
			out.writeLEInt(animSections[i]);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tunk1: " + unk1 + "\t" + Hasher.getFileName(unk1));
		System.out.println("\tanim count: " + numAnims);
		for (int i = 0; i < numAnims; i++) {
			System.out.println("\t  " + Hasher.getFileName(animNames[i]) + "  section " + animSections[i]);
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
