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

public class LightEffect extends EffectComponent {
	
	public static final int TYPE = 0x0A;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "light";
	
	private static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum(new String[] {
			"ambient", "dir", "directional", "point", "spot", "area"
	}, new int[] {
			0, 1, 1, 2, 3, 4
	}); 
	private static final int FLAG_SUSTAIN = 8;
	
	private int flags;  // ?
	private int type;  // byte
	private float life;
	private int loop;  // short
	private EffectColor[] color = new EffectColor[] {EffectColor.WHITE};
	private float[] strength = new float[] {1};
	private float[] size = new float[] {1};  // also spotWidth
	private float spotWidthPenumbra;

	public LightEffect(int type, int version) {
		super(type, version);
	}
	
	public LightEffect(LightEffect effect) {
		super(effect);
		
		flags = effect.flags;
		type = effect.type;
		life = effect.life;
		loop = effect.loop;
		color = new EffectColor[effect.color.length];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor(effect.color[i]);
		}
		strength = new float[effect.strength.length];
		for (int i = 0; i < strength.length; i++) {
			strength[i] = effect.strength[i];
		}
		size = new float[effect.size.length];
		for (int i = 0; i < size.length; i++) {
			size[i] = effect.size[i];
		}
		spotWidthPenumbra = effect.spotWidthPenumbra;
	}
	

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		
		flags = in.readInt();  // & 0xF ?
		type = in.readByte();
		
		if (version > 1) 
		{
			life = in.readFloat();
			loop = in.readShort();
		}
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		strength = new float[in.readInt()];
		in.readFloats(strength);
		
		size = new float[in.readInt()];
		in.readFloats(size);
		
		spotWidthPenumbra = in.readFloat();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		
		out.writeInt(flags);
		out.writeByte(type);
		
		out.writeFloat(life);
		out.writeShort(loop);
		
		out.writeInt(color.length);
		for (EffectColor f : color) f.writeLE(out);
		
		out.writeInt(strength.length);
		out.writeFloats(strength);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		
		out.writeFloat(spotWidthPenumbra);		
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		
		{
			ArgScriptCommand cType = block.getCommand("type");
			if (cType != null) type = ENUM_TYPE.getValue(cType.getSingleArgument());
		}
		{
			ArgScriptCommand cLife = block.getCommand("life");
			if (cLife != null) {
				life = Float.parseFloat(cLife.getSingleArgument());
				if (cLife.hasFlag("single")) loop = 1;
				else {
					ArgScriptOption oLoop = cLife.getOption("loop");
					if (oLoop != null) {
						if (oLoop.getArgumentCount() == 0) loop = 0;
						else loop = Integer.parseInt(oLoop.getSingleArgument());
					}
					ArgScriptOption oSustain = cLife.getOption("sustain");
					if (oSustain != null) {
						if (oSustain.getArgumentCount() == 0) loop = 1;
						else loop = Integer.parseInt(oSustain.getSingleArgument());
					}
				}
			}
		}
		{
			ArgScriptCommand cColor = block.getCommand("color");
			if (cColor != null) color = ArgScript.stringsToColors(cColor.getArguments());
		}
		{
			ArgScriptCommand cStrength = block.getCommand("strength");
			if (cStrength != null) strength = ArgScript.stringsToFloats(cStrength.getArguments());
		}
		{
			ArgScriptCommand cSize = block.getCommand("size");
			if (cSize != null) size = ArgScript.stringsToFloats(cSize.getArguments());
			
			ArgScriptCommand cSpotWidth = block.getCommand("spotWidth");
			if (cSpotWidth != null) {
				size = ArgScript.stringsToFloats(cSize.getArguments());
				ArgScriptOption oPenumbra = cSpotWidth.getOption("penumbra");
				if (oPenumbra != null) spotWidthPenumbra = Float.parseFloat(oPenumbra.getSingleArgument());
			}
		}
		
		{ ArgScriptCommand c = block.getCommand("flags"); if (c != null) flags = Hasher.decodeInt(c.getSingleArgument()); }
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		
		block.putCommand(new ArgScriptCommand("type", ENUM_TYPE.getKey(type)));
		
		ArgScriptCommand cLife = new ArgScriptCommand("life", Float.toString(life));
		if (loop == 1 && (flags & FLAG_SUSTAIN) != FLAG_SUSTAIN) cLife.putFlag("single");
		else if ((flags & FLAG_SUSTAIN) == 0) cLife.putOption(new ArgScriptOption("loop", Integer.toString(loop)));
		else cLife.putOption(new ArgScriptOption("sustain", Integer.toString(loop)));
		block.putCommand(cLife);
		
		block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		block.putCommand(new ArgScriptCommand("strength", ArgScript.floatsToStrings(strength)));
		
		if (spotWidthPenumbra != 0) {
			ArgScriptCommand cSize = new ArgScriptCommand("spotWidth", ArgScript.floatsToStrings(size));
			cSize.putOption(new ArgScriptOption("penumbra", Float.toString(spotWidthPenumbra)));
			block.putCommand(cSize);
		}
		else {
			block.putCommand(new ArgScriptCommand("size", ArgScript.floatsToStrings(size)));
		}
		if (flags != 0) {
			block.putCommand(new ArgScriptCommand("flags", "0x" + Integer.toHexString(flags)));
		}
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline light effect is not supported.");
	}

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"ambient", "dir", "directional", "point", "spot", "area"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"single", "loop", "sustain", "penumbra"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"type", "life", "color", "strength", "spotWidth", "size"
		};
	}
}
