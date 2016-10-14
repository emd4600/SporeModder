package sporemodder.files.formats.gait;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class GAITStepLengthKey extends FileStructure {
	private float speedi;
	private float stepLength;
	
	public void read(InputStreamAccessor in) throws IOException {
		speedi = in.readLEFloat();
		stepLength = in.readLEFloat();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(speedi);
		out.writeLEFloat(stepLength);
	}
	
	public ArgScriptBlock toBlock() {
		
		ArgScriptBlock block = new ArgScriptBlock("steplengthkey");
		block.putCommand(new ArgScriptCommand("steplength", Float.toString(stepLength)));
		block.putCommand(new ArgScriptCommand("speedi", Float.toString(speedi)));
		
		return block;
	}
	
	public void parse(ArgScriptBlock block) throws IOException, NumberFormatException, ArgScriptException {
		
		ArgScriptCommand cStepLength = block.getCommand("steplength");
		if (cStepLength != null) {
			stepLength = Float.parseFloat(cStepLength.getSingleArgument());
		}
		
		ArgScriptCommand cSpeedi = block.getCommand("speedi");
		if (cSpeedi != null) {
			speedi = Float.parseFloat(cSpeedi.getSingleArgument());
		}
	}
}
