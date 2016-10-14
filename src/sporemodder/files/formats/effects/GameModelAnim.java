package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class GameModelAnim {
	
	private static final int FLAG_SUSTAIN = 1;
	private static final int FLAG_SINGLE = 2;
	private static final int FLAG_LOOP = 0;

	private final float[] lengthRange = new float[2];
	private float[] curve = new float[0];
	private float curveVary;
	private float speedScale;
	private int channelId;
	private int mode;  // byte
	
	public GameModelAnim() {
		
	}
	
	public GameModelAnim(GameModelAnim other) {
		lengthRange[0] = other.lengthRange[0];
		lengthRange[1] = other.lengthRange[1];
		curve = new float[other.curve.length];
		System.arraycopy(other.curve, 0, curve, 0, other.curve.length);
		curveVary = other.curveVary;
		speedScale = other.speedScale;
		channelId = other.channelId;
		mode = other.mode;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		lengthRange[0] = in.readLEFloat();
		lengthRange[1] = in.readLEFloat();
		curve = new float[in.readInt()];
		for (int f = 0; f < curve.length; f++) {
			curve[f] = in.readFloat();
		}
		curveVary = in.readFloat();
		speedScale = in.readFloat();
		channelId = in.readInt();
		mode = in.readByte();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(lengthRange[0]);
		out.writeLEFloat(lengthRange[1]);
		out.writeInt(curve.length);
		out.writeFloats(curve);
		out.writeFloat(curveVary);
		out.writeFloat(speedScale);
		out.writeInt(channelId);
		out.writeByte(mode);
	}
	
	public ArgScriptCommand toCommand() {
		ArgScriptCommand command = new ArgScriptCommand("animate", ArgScript.floatsToStrings(curve));
		
		if (curveVary != 0) command.putOption(new ArgScriptOption("vary", Float.toString(curveVary)));
		if (speedScale != 0) command.putOption(new ArgScriptOption("speedScale", Float.toString(speedScale)));
		if (channelId != 0) command.putOption(new ArgScriptOption("channel", Hasher.getFileName(channelId)));
		
		if (lengthRange[0] != 0 || lengthRange[1] != 0) {
			//TODO Spore does something different here
			command.putOption(new ArgScriptOption("length", Float.toString(lengthRange[0]), Float.toString(lengthRange[1])));
		}
		
		if (mode == FLAG_SUSTAIN) command.putFlag("sustain");
		else if (mode == FLAG_SINGLE) command.putFlag("single");
		else if (mode == FLAG_LOOP) command.putFlag("loop");
		else command.putOption(new ArgScriptOption("mode", Integer.toString(mode)));
		
		return command;
	}
	
	public void parse(ArgScriptCommand command) throws ArgScriptException {
		List<String> args = command.getArguments();
		curve = new float[args.size()];
		for (int i = 0; i < curve.length; i++) curve[i] = Float.parseFloat(args.get(i));
		
		{ ArgScriptOption option = command.getOption("vary"); if (option != null) curveVary = Float.parseFloat(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("speedScale"); if (option != null) speedScale = Float.parseFloat(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("channel"); if (option != null) channelId = Hasher.getFileHash(option.getSingleArgument()); }
		if (command.hasFlag("sustain")) mode = FLAG_SUSTAIN;
		else if (command.hasFlag("single")) mode = FLAG_SINGLE;
		else if (command.hasFlag("loop")) mode = FLAG_LOOP;
		
		{
			ArgScriptOption option = command.getOption("length");
			if (option != null) {
				//TODO Spore does something different here
				List<String> optionArgs = option.getArguments(2);
				lengthRange[0] = Float.parseFloat(optionArgs.get(0));
				lengthRange[1] = Float.parseFloat(optionArgs.get(1));
			}
		}
	}
	
	// For Syntax Highlighting
		public static String[] getEnumTags() {
			return new String[] {};
		}
		
		public static String[] getBlockTags() {
			return new String[] {};
		}
		
		public static String[] getOptionTags() {
			return new String[] {
				"vary", "speedScale", "channel", "sustain", "single", "loop", "length", "speedScale"
			};
		}
		
		public static String[] getCommandTags() {
			return new String[] {
				"animate"
			};
		}
}
