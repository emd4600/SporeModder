package sporemodder.files.formats.pctp;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class PCTPDeformSpec extends FileStructure {
	
	private String identifier;  // four bytes
	private int[] anims;
	private float[][] ranges;  // probably not ranges, since one is not present in version 3
	private int[] unks;
	
	public void read(InputStreamAccessor in, int version) throws IOException {
		identifier = PCTPMain.getIdentifierString(in.readLEInt());
		
		int count = in.readInt();
		anims = new int[count];
		ranges = new float[count][2];
		unks = new int[count];
		
		for (int i = 0; i < count; i++) {
			
			anims[i] = in.readInt();
			ranges[i] = new float[] {in.readFloat(), version >= 3 ? in.readFloat() : 1.0f};
			unks[i] = in.readInt();
		}
	}
	
	public void write(OutputStreamAccessor out, int version) throws IOException {
		PCTPMain.writeIdentifier(out, identifier);
		out.writeInt(anims.length);
		
		for (int i = 0; i < anims.length; i++) 
		{
			out.writeInt(anims[i]);
			out.writeFloat(ranges[i][0]);
			if (version > 3) {
				out.writeFloat(ranges[i][1]);
			}
			out.writeInt(unks[i]);
		}
	}
	
	public ArgScriptCommand toCommand(int version) {
		String[] lists = new String[anims.length];
		
		for (int i = 0; i < lists.length; i++) {
			if (version > 3) {
				lists[i] = ArgScript.createList(Hasher.getFileName(anims[i]), Float.toString(ranges[i][0]), Float.toString(ranges[i][1]), 
						Integer.toString(unks[i]));
			}
			else {
				lists[i] = ArgScript.createList(Hasher.getFileName(anims[i]), Float.toString(ranges[i][0]), Integer.toString(unks[i]));
			}
		}
		
		return new ArgScriptCommand(identifier, lists);
	}
	
	public void fromCommand(ArgScriptCommand command, int version) throws ArgScriptException, IOException {
		List<String> lists = command.getArguments(0, Integer.MAX_VALUE);
		identifier = command.getKeyword();
		anims = new int[lists.size()];
		ranges = new float[anims.length][2];
		unks = new int[anims.length];
		
		for (int i = 0; i < anims.length; i++) 
		{
			String[] list = ArgScript.parseList(lists.get(i));
			if (version > 3 && list.length != 4) {
				new ArgScriptException(String.format("Expecting exactly %d arguments in list %s", 4, lists.get(i)));
			}
			else if (list.length != 3) {
				new ArgScriptException(String.format("Expecting exactly %d arguments in list %s", 3, lists.get(i)));
			}
			
			anims[i] = Hasher.getFileHash(list[0]);
			ranges[i] = new float[] {Float.parseFloat(list[1]), version > 3 ? Float.parseFloat(list[2]) : 1.0f};
			unks[i] = Integer.parseInt(list[version > 3 ? 3 : 2]);
		}

	}

}
