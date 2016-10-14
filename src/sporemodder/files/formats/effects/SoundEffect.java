package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class SoundEffect extends EffectComponent {
	
	public static final int TYPE = 0x0005;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "sound";
	
	// SIMS 3:
	/*
	 *  DWORD  Flags //0xF mask?
		QWORD  IID64 of an Audio Tuner
		FLOAT  Location Update Delta
		FLOAT  Play Time
		FLOAT  Volume
		if (Version >= 2) BYTE 
	 */
	
	private int flags;  // 0x08
	private final ResourceID soundID = new ResourceID();  // 0x10
	private int field_18;  // 0x18
	private float field_1C = 0.05f;  // 0x1C
	private float field_20;  // 0x20
	private float field_24;  // 0x24

	public SoundEffect(int type, int version) {
		super(type, version);
	}

	public SoundEffect(SoundEffect effect) {
		super(effect);

		flags = effect.flags;
		soundID.copy(effect.soundID);
		field_18 = effect.field_18;
		field_1C = effect.field_1C;
		field_20 = effect.field_20;
		field_24 = effect.field_24;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();  // & 0x1F
		soundID.read(in);
		field_18 = in.readInt();
		field_1C = in.readFloat();
		field_20 = in.readFloat();
		field_24 = in.readFloat();
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		soundID.write(out);
		out.writeInt(field_18);
		out.writeFloat(field_1C);
		out.writeFloat(field_20);
		out.writeFloat(field_24);
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}
	
	@Override
	public boolean supportsBlock() {
		return false;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		String arg;
		
		if ((arg = command.getOptionArg("name")) != null)
		{
			soundID.parse(arg);
		}
		if ((arg = command.getOptionArg("field_18")) != null)
		{
			field_18 = Hasher.getFileHash(arg);
		}
		if ((arg = command.getOptionArg("soundFlags")) != null)
		{
			flags = Hasher.decodeInt(arg);
		}
		if ((arg = command.getOptionArg("field_1C")) != null)
		{
			field_1C = Float.parseFloat(arg);
		}
		if ((arg = command.getOptionArg("field_20")) != null)
		{
			field_20 = Float.parseFloat(arg);
		}
		if ((arg = command.getOptionArg("field_24")) != null)
		{
			field_24 = Float.parseFloat(arg);
		}
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, new String[0]);
		
		c.putOption(new ArgScriptOption("name", soundID.toString()));
		if (flags != 0) c.putOption(new ArgScriptOption("soundFlags", "0x" + Integer.toHexString(flags)));
		if (field_18 != 0) c.putOption(new ArgScriptOption("field_18", Hasher.getFileName(field_18)));
		if (field_1C != 0) c.putOption(new ArgScriptOption("field_1C", Float.toString(field_1C)));
		if (field_20 != 0) c.putOption(new ArgScriptOption("field_20", Float.toString(field_20)));
		if (field_24 != 0) c.putOption(new ArgScriptOption("field_24", Float.toString(field_24)));
		
		return c;
	}

	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"name", "soundFlags", "field_18", "field_1C", "field_20", "field_24"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			KEYWORD,
		};
	}
}
