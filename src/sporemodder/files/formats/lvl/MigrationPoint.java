package sporemodder.files.formats.lvl;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.utilities.Hasher;

public class MigrationPoint extends FileStructure implements GameplayMarker {
	protected static final int HASH = 0xC012AE1F;
	
	private enum MigrationPointTypeEnum {NORMAL, AVATAR_JOURNEY, PATROL_PATH, AVATAR_SPECIES_JOURNEY, CREATURE_PATH};
	
	private int name;
	private int number;
	private float radiusMultiplier = 1;
	private int nestLevel; //?
	private MigrationPointTypeEnum type = MigrationPointTypeEnum.NORMAL;
	
	public void read(InputStreamAccessor in) throws IOException {
		name = in.readLEInt();
		nestLevel = in.readLEInt();
		number = in.readLEInt();
		radiusMultiplier = in.readLEFloat();
		type = MigrationPointTypeEnum.values()[in.readLEInt()];
		
		for (int i = 0; i < 29; i++) expect(in.readInt(), 0, "LVL-MP-0", in.getFilePointer());
	}
	
	public void print() {
		System.out.println("name: " + Hasher.getFileName(name));
		System.out.println("number: " + number);
		System.out.println("radiusMultiplier: " + radiusMultiplier);
		System.out.println("type: " + type.toString());
		System.out.println("nestLevel: " + nestLevel);
	}

	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(name);
		out.writeLEInt(nestLevel);
		out.writeLEInt(number);
		out.writeLEFloat(radiusMultiplier);
		out.write(new byte[29*4]);
	}
}
