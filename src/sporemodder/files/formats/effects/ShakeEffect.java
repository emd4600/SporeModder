package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class ShakeEffect extends EffectComponent {
	
	public static final int TYPE = 0x06;
	public static final int MIN_VERSION = 2;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "shake";
	
	// probably?
	private static final ArgScriptEnum ENUM_TABLE = new ArgScriptEnum(new String[] {
			"random", "sineY"
	}, new int[] {
			0, 1
	}, 0, "UnknownTableType");
	
	private int flags;  // 0x0C // & 1
	
	private float lifeTime;  // 0x10
	private float fadeTime = 1;  // 0x14
	
	private float[] strength = new float[0];  // 0x18
	private float[] frequency = new float[0];  // 0x2C
	
	private float aspectRatio = 1;  // 0x40
	private int baseTableType;  // 0x44 // byte
	private float falloff;  // 0x48

	public ShakeEffect(int type, int version) {
		super(type, version);
	}

	public ShakeEffect(ShakeEffect effect) {
		super(effect);

		flags = effect.flags;
		lifeTime = effect.lifeTime;
		fadeTime = effect.fadeTime;
		strength = EffectComponent.copyArray(effect.strength);
		frequency = EffectComponent.copyArray(effect.frequency);
		aspectRatio = effect.aspectRatio;
		baseTableType = effect.baseTableType;
		falloff = effect.falloff;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		lifeTime = in.readFloat();
		fadeTime = in.readFloat();
		
		strength = new float[in.readInt()];
		for (int i = 0; i < strength.length; i++) strength[i] = in.readFloat();
		
		frequency = new float[in.readInt()];
		for (int i = 0; i < frequency.length; i++) frequency[i] = in.readFloat();
		
		aspectRatio = in.readFloat();
		baseTableType = in.readByte();
		falloff = in.readFloat();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		
		out.writeFloat(lifeTime);
		out.writeFloat(fadeTime);
		
		out.writeInt(strength.length);
		for (float f : strength) out.writeFloat(f);
		
		out.writeInt(frequency.length);
		for (float f : frequency) out.writeFloat(f);
		
		out.writeFloat(aspectRatio);
		out.writeByte(baseTableType);
		out.writeFloat(falloff);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptCommand c = null;
		if ((c = block.getCommand("flags")) != null)
		{
			flags = Hasher.decodeInt(c.getSingleArgument());
		}
		if ((c = block.getCommand("length")) != null) 
		{
			lifeTime = Float.parseFloat(c.getSingleArgument());
			String arg = c.getOptionArg("fade");
			if (arg != null) {
				fadeTime = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("amplitude")) != null)
		{
			strength = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("frequency")) != null)
		{
			frequency = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("shakeAspect")) != null)
		{
			aspectRatio = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("table")) != null)
		{
			baseTableType = ENUM_TABLE.getValue(c.getSingleArgument());
		}
		if ((c = block.getCommand("falloff")) != null)
		{
			falloff = Float.parseFloat(c.getSingleArgument());
		}
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		if (flags != 0) block.putCommand(new ArgScriptCommand("flags", "0x" + Integer.toHexString(flags)));
		{
			ArgScriptCommand c = new ArgScriptCommand("length", Float.toString(lifeTime));
			if (fadeTime != 1) c.putOption(new ArgScriptOption("fade", Float.toString(fadeTime)));
			block.putCommand(c);
		}
		block.putCommand(new ArgScriptCommand("amplitude", ArgScript.floatsToStrings(strength)));
		block.putCommand(new ArgScriptCommand("frequency", ArgScript.floatsToStrings(frequency)));
		if (aspectRatio != 1) block.putCommand(new ArgScriptCommand("shakeAspect", Float.toString(aspectRatio)));
		block.putCommand(new ArgScriptCommand("table", ENUM_TABLE.getKey(baseTableType)));
		block.putCommand(new ArgScriptCommand("falloff", Float.toString(falloff)));
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline shake effect is not supported.");
	}

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"random", "sineY"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"fade"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"length", "amplitude", "frequency", "shakeAspect", "table", "falloff", "flags"
		};
	}
}
