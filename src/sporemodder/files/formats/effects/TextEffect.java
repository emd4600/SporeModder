package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class TextEffect extends EffectComponent {
	
	public static final int TYPE = 0x2F;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "text";
	
	private static final int FLAG_SUSTAIN = 2;
	
	private int flags;  // 0x08
	private String string;  // 0x0C
	private final ResourceID id = new ResourceID();  // 0x20
	private float fontSize;  // 0x28
	private final ResourceID font = new ResourceID();  // 0x30
	private final float[] offset = new float[2];  // 0x38
	private float life;  // 0x40
	private EffectColor[] color = new EffectColor[] { EffectColor.WHITE };  // 0x44
	private float[] alpha = new float[] { 1 };  // 0x58
	private float[] size = new float[] { 1 };  // 0x6C
	
	public TextEffect(int type, int version) {
		super(type, version);
	}
	
	public TextEffect(TextEffect effect) {
		super(effect);
		
		flags = effect.flags;
		string = new String(effect.string);
		id.copy(effect.id);
		fontSize = effect.fontSize;
		font.copy(effect.font);
		offset[0] = effect.offset[0];
		offset[1] = effect.offset[1];
		life = effect.life;
		color = EffectComponent.copyArray(effect.color);
		alpha = EffectComponent.copyArray(effect.alpha);
		size = EffectComponent.copyArray(effect.size);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();  // & 0xF;
		string = in.readString16(in.readInt());
		
		id.read(in);
		fontSize = in.readFloat();
		font.read(in);
		
		offset[0] = in.readLEFloat();
		offset[1] = in.readLEFloat();
		life = in.readFloat();
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		alpha = new float[in.readInt()];
		in.readFloats(alpha);
		
		size = new float[in.readInt()];
		in.readFloats(size);
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeInt(string.length());
		out.writeString16(string);
		
		id.write(out);
		out.writeFloat(fontSize);
		font.write(out);
		
		out.writeLEFloat(offset[0]);
		out.writeLEFloat(offset[1]);
		out.writeFloat(life);
		
		out.writeInt(color.length);
		for (EffectColor c : color) c.writeLE(out);
		
		out.writeInt(alpha.length);
		out.writeFloats(alpha);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		ArgScriptCommand c;
		
		if ((c = block.getCommand("string")) != null) 
		{
			String arg;
			
			string = c.getSingleArgument();
			
			if ((arg = c.getOptionArg("id")) != null) {
				id.parse(arg);
			}
			if ((arg = c.getOptionArg("size")) != null) {
				fontSize = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				ArgScript.parseFloatList(arg, offset);
			}
		}
		if ((c = block.getCommand("font")) != null || (c = block.getCommand("style")) != null)
		{
			font.parse(c.getSingleArgument());
			String arg;
			if ((arg = c.getOptionArg("size")) != null) {
				fontSize = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("life")) != null)
		{
			float value = Float.parseFloat(c.getSingleArgument());
			if (value > 0.000001) {
				life = 1 / value;
			}
			else {
				life = 0;
			}
			if (c.hasFlag("sustain")) flags |= FLAG_SUSTAIN;
		}
		if ((c = block.getCommand("color")) != null)
		{
			color = ArgScript.stringsToColors(c.getArguments());
		}
		if ((c = block.getCommand("alpha")) != null)
		{
			alpha = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("size")) != null)
		{
			size = ArgScript.stringsToFloats(c.getArguments());
		}
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {

		boolean fontIsDefault = font.isDefault();
		{
			ArgScriptCommand c = new ArgScriptCommand("string", string);
			block.putCommand(c);
			
			if (!id.isDefault()) c.putOption(new ArgScriptOption("id", id.toString()));
			// if we write the 'font' command, better put size there
			if (fontIsDefault) c.putOption(new ArgScriptOption("size", Float.toString(fontSize)));
			if (offset[0] != 0 || offset[1] != 0) c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(offset)));
		}
		if (!fontIsDefault) {
			ArgScriptCommand c = new ArgScriptCommand("font", font.toString());
			c.putOption(new ArgScriptOption("size", Float.toString(fontSize)));
			block.putCommand(c);
		}
		if (life != 0) {
			ArgScriptCommand c = new ArgScriptCommand("life", Float.toString(1 / life));
			if ((flags & FLAG_SUSTAIN) == FLAG_SUSTAIN) c.putFlag("sustain");
			block.putCommand(c);
		}
		
		block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		block.putCommand(new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha)));
		block.putCommand(new ArgScriptCommand("size", ArgScript.floatsToStrings(size)));
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline text effect is not supported.");
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"size", "offset", "sustain", "id"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"string", "font", "life", "color", "alpha", "size", "style"
		};
	}

}
