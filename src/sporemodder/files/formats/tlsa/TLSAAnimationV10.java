package sporemodder.files.formats.tlsa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class TLSAAnimationV10 extends FileStructure {
	private static class TLSAAnim {
		private float unk1, unk2;  // priority, duration?
		private int id;
		private String description = "";
		
		private void read(InputStreamAccessor in) throws IOException {
			unk1 = in.readFloat();
			unk2 = in.readFloat();
			
			id = in.readInt();
			
			description = in.readLEString16(in.readInt());
		}
		
		private void write(OutputStreamAccessor out) throws IOException {
			out.writeFloat(unk1);
			out.writeFloat(unk2);
			
			out.writeInt(id);
			
			out.writeInt(description.length());
			out.writeLEString16(description);
		}
		
		private void parse(ArgScriptCommand command) throws IOException, ArgScriptException {
			id = Hasher.getFileHash(command.getKeyword());
			
			List<String> args = command.getArguments(0, 1);
			
			// priority ?
			ArgScriptOption oUnk1 = command.getOption("unk1");
			if (oUnk1 != null) unk1 = Float.parseFloat(oUnk1.getSingleArgument());
			
			// duration ?
			ArgScriptOption oUnk2 = command.getOption("unk2");
			if (oUnk2 != null) unk2 = Float.parseFloat(oUnk2.getSingleArgument());
			
			if (args.size() == 1) {
				description = args.get(0);
			}
		}
		
		private ArgScriptCommand toCommand() throws IOException {
			ArgScriptCommand command = new ArgScriptCommand(Hasher.getFileName(id), description);
			if (unk1 != 1.0f) command.putOption(new ArgScriptOption("unk1", Float.toString(unk1)));
			if (unk2 != -1.0f) command.putOption(new ArgScriptOption("unk2", Float.toString(unk2)));
			
			return command;
		}
	}
	
	private float priorityOverride;  // ?
	
	private List<TLSAAnim> childs = new ArrayList<TLSAAnim>();
	
	public void read(InputStreamAccessor in) throws IOException {
		
		priorityOverride = in.readFloat();
		
		int childCount = in.readInt();
		
		for (int i = 0; i < childCount; i++) {
			TLSAAnim anim = new TLSAAnim();
			anim.read(in);
			childs.add(anim);
		}
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeFloat(priorityOverride);
		out.writeInt(childs.size());
		
		for (TLSAAnim anim : childs) {
			anim.write(out);
		}
	}
	
	public void parse(ArgScriptBlock block) throws IOException, ArgScriptException {
		if (!block.getKeyword().equals("anim")) throw new ArgScriptException("Unexpected animation formatting in keyword: " + block.getKeyword());
		
		priorityOverride = Float.parseFloat(block.getSingleArgument());
		
		childs = new ArrayList<TLSAAnim>();
		
		for (ArgScriptCommand command : block.getAllCommands()) {
			TLSAAnim anim = new TLSAAnim();
			anim.parse(command);
		}
	}
	
	public ArgScriptBlock toBlock() throws IOException {
		ArgScriptBlock block = new ArgScriptBlock("anim", Float.toString(priorityOverride));
		for (TLSAAnim anim : childs) {
			block.putCommand(anim.toCommand());
		}
		return block;
	}
}
