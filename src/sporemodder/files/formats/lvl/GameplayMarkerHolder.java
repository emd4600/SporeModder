package sporemodder.files.formats.lvl;

import java.io.IOException;
import java.util.Arrays;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class GameplayMarkerHolder {
	public float[] position = new float[3];
	public float[] unk = new float[4]; //TODO Rotation?
	public int[] id = new int[3];
	private int pos;
	public GameplayMarker gameplayMarker;
	
	public void read(InputStreamAccessor in) throws IOException {
		pos = in.getFilePointer();
		position[0] = in.readFloat();
		position[1] = in.readFloat();
		position[2] = in.readFloat();
		unk[0] = in.readFloat();
		unk[1] = in.readFloat();
		unk[2] = in.readFloat();
		unk[3] = in.readFloat();
		id[0] = in.readInt();
		id[1] = in.readInt();
		id[2] = in.readInt();
		
		if (id[0] == CreatureArchetype.HASH) {
			gameplayMarker = new CreatureArchetype();
			
		} else if (id[0] == MigrationPoint.HASH) {
			gameplayMarker = new MigrationPoint();
			
		} else if (id[0] == InteractiveOrnament.HASH) {
			gameplayMarker = new InteractiveOrnament();
		}
		
		gameplayMarker.read(in);
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeFloat(position[0]);
		out.writeFloat(position[1]);
		out.writeFloat(position[2]);
		out.writeFloat(unk[0]);
		out.writeFloat(unk[1]);
		out.writeFloat(unk[2]);
		out.writeFloat(unk[3]);
		out.writeInt(id[0]);
		out.writeInt(id[1]);
		out.writeInt(id[2]);
		
		gameplayMarker.write(out);
	}
	
	public void print() {
		System.out.println("#####################################");
		System.out.println("pos: " + pos);
		System.out.println(Arrays.toString(position));
		System.out.println(Arrays.toString(unk));
		System.out.println(Hasher.getFileName(id[0]) + "\t" + Hasher.getFileName(id[1]) + "\t" + Hasher.getFileName(id[2]));
		System.out.println();
		gameplayMarker.print();
		System.out.println("#####################################");
		System.out.println();
	}
}
