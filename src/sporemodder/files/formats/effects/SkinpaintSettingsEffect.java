package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class SkinpaintSettingsEffect extends EffectComponent {
	
	public static final int TYPE = 0x0022;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 3;
	//TODO it can be SPSkinPaintClear too
	public static final String KEYWORD = "SPSkinPaintSettings";
	public static final String KEYWORD_CLEAR = "SPSkinPaintClear";
	
	private static final int FLAG_DIFFUSE = 1;
	private static final int FLAG_SPEC = 2;
	private static final int FLAG_BUMP = 4;
	private static final int FLAG_SPECEXP = 8;
	private static final int FLAG_SPECBUMP = 0xE;
	private static final int FLAG_PARTBUMPSCALE = 0x10;
	private static final int FLAG_PARTSPECSCALE = 0x20;
	private static final int FLAG_HAIR = 0x40;
	private static final int FLAG_HAIRTEXTURE = 0x40;
	private static final int FLAG_HAIRPRINTGEOM = 0x100;
	private static final int FLAG_GLOSS = 0x200;
	private static final int FLAG_PHONG = 0x400;
	
	private int diffuseColorIndex;  // 0x08
	private EffectColor diffuseColor = EffectColor.BLACK;  // 0x0C
	private final float[] specBump = new float[3];  // 0x18  // spec, specExp, bump
	private float gloss;  // 0x24  // only in version >= 2
	private float phong;  // 0x28  // only in version >= 3
	private float partBumpScale;  // 0x2C
	private float partSpecScale;  // 0x30
	//TODO all these are related with 'hair'
	private float field_34;  // 0x34  // an angle in radians
	private float field_38;  // 0x38
	private float field_3C;  // 0x3C
	private float field_40;  // 0x40
	private float field_44;  // 0x44
	private float field_48;  // 0x48
	private float field_4C;  // 0x4C
	private float field_50;  // 0x50
	private boolean field_54;  // 0x54
	////////////////////
	private final ResourceID hairTexture = new ResourceID();  // 0x58
	private final ResourceID hairPrintGeom = new ResourceID();  // 0x60
	private int flags;  // 0x68

	public SkinpaintSettingsEffect(int type, int version) {
		super(type, version);
	}

	public SkinpaintSettingsEffect(SkinpaintSettingsEffect other) {
		super(other);
		
		diffuseColorIndex = other.diffuseColorIndex;
		diffuseColor = new EffectColor(other.diffuseColor);
		specBump[0] = other.specBump[0];
		specBump[1] = other.specBump[1];
		specBump[2] = other.specBump[2];
		gloss = other.gloss;
		phong = other.phong;
		partBumpScale = other.partBumpScale;
		partSpecScale = other.partSpecScale;
		field_34 = other.field_34;
		field_38 = other.field_38;
		field_3C = other.field_3C;
		field_40 = other.field_40;
		field_44 = other.field_44;
		field_48 = other.field_48;
		field_4C = other.field_4C;
		field_50 = other.field_50;
		field_54 = other.field_54;
		hairTexture.copy(other.hairTexture);
		hairPrintGeom .copy(other.hairPrintGeom);
		flags = other.flags;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		diffuseColorIndex = in.readInt();
		diffuseColor.readLE(in);
		in.readLEFloats(specBump);
		
		if (version >= 2) {
			gloss = in.readFloat();
		}
		if (version >= 3) {
			phong = in.readFloat();
		}
		partBumpScale = in.readFloat();
		partSpecScale = in.readFloat();
		field_34 = in.readFloat();
		field_38 = in.readFloat();
		field_3C = in.readFloat();
		field_40 = in.readFloat();
		field_44 = in.readFloat();
		field_48 = in.readFloat();
		field_4C = in.readFloat();
		field_50 = in.readFloat();
		field_54 = in.readBoolean();
		hairTexture.read(in);
		hairPrintGeom.read(in);
		flags = in.readInt();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(diffuseColorIndex);
		diffuseColor.writeLE(out);
		out.writeLEFloats(specBump);
		if (version >= 2) out.writeLEFloat(gloss);
		if (version >= 3) out.writeLEFloat(phong);
		out.writeFloat(partBumpScale);
		out.writeFloat(partSpecScale);
		out.writeFloat(field_34);
		out.writeFloat(field_38);
		out.writeFloat(field_3C);
		out.writeFloat(field_40);
		out.writeFloat(field_44);
		out.writeFloat(field_48);
		out.writeFloat(field_4C);
		out.writeFloat(field_50);
		out.writeBoolean(field_54);
		hairTexture.write(out);
		hairPrintGeom.write(out);
		out.writeInt(flags);
		
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

		boolean specCleared = false;
		boolean bumpCleared = false;
		
		{ 
			ArgScriptOption o = command.getOption("rgb");
			if (o == null) o = command.getOption("diffuse");
			if (o != null) {
				flags |= FLAG_DIFFUSE;
				
				String arg = o.getSingleArgument();
				if (arg.startsWith("color")) {
					diffuseColorIndex = Integer.parseInt(arg.substring(5)) - 1;
				}
				else {
					diffuseColorIndex = -1;
					float[] list = ArgScript.parseFloatList(arg, 3);
					diffuseColor.setR(list[0]);
					diffuseColor.setG(list[1]);
					diffuseColor.setB(list[2]);
				}
			}
		}
		{
			ArgScriptOption o = command.getOption("specBump");
			if (o != null) {
				flags |= FLAG_SPECBUMP;
				float[] list = ArgScript.parseFloatList(o.getSingleArgument(), 3);
				specBump[0] = list[0];
				specBump[1] = list[1];
				specBump[2] = list[2];
				
				specBump[1] = Math.min(Math.max(specBump[1] * 255, 1.0f), 60.0f) / 60.0f;
			}
		}
		{
			ArgScriptOption o = command.getOption("spec");
			if (o != null) {
				flags |= FLAG_SPEC;
				specBump[0] = Float.parseFloat(o.getSingleArgument());
				specBump[0] = Math.min(Math.max(specBump[0], 0), 1);
				specCleared = true;
			}
		}
		{
			ArgScriptOption o = command.getOption("bump");
			if (o != null) {
				flags |= FLAG_BUMP;
				specBump[2] = Float.parseFloat(o.getSingleArgument());
				specBump[2] = Math.min(Math.max(specBump[2], 0), 1);
				bumpCleared = true;
			}
		}
		{
			ArgScriptOption o = command.getOption("specExp");
			if (o == null) o = command.getOption("exponent");
			if (o != null) {
				flags |= FLAG_SPECEXP;
				specBump[1] = Float.parseFloat(o.getSingleArgument());
				specBump[1] = Math.min(Math.max(specBump[1], 1.0f), 60.0f) / 60.0f;
			}
		}
		{
			ArgScriptOption o = command.getOption("gloss");
			if (o != null) {
				flags |= FLAG_GLOSS;
				gloss = Float.parseFloat(o.getSingleArgument());
			}
		}
		{
			ArgScriptOption o = command.getOption("phong");
			if (o != null) {
				flags |= FLAG_PHONG;
				gloss = Float.parseFloat(o.getSingleArgument());
			}
		}
		{
			ArgScriptOption o = command.getOption("partBumpScale");
			if (o != null) {
				flags |= FLAG_PARTBUMPSCALE;
				partBumpScale = Float.parseFloat(o.getSingleArgument());
			}
		}
		{
			ArgScriptOption o = command.getOption("partSpecScale");
			if (o != null) {
				flags |= FLAG_PARTSPECSCALE;
				partSpecScale = Float.parseFloat(o.getSingleArgument());
			}
		}
		{
			ArgScriptOption o = command.getOption("hair");
			if (o != null) {
				flags |= FLAG_HAIR;
				List<String> args = o.getArguments(9);
				field_38 = Float.parseFloat(args.get(0));
				field_34 = (float) Math.toRadians(Float.parseFloat(args.get(1)));  // * (PI / 180.0f)
				field_3C = Float.parseFloat(args.get(2));
				field_40 = Float.parseFloat(args.get(3));
				field_44 = Float.parseFloat(args.get(4));
				field_48 = Float.parseFloat(args.get(5));
				field_4C = Float.parseFloat(args.get(6));
				field_50 = Float.parseFloat(args.get(7));
				field_54 = Integer.parseInt(args.get(8)) == 0 ? false : true;
			}
		}
		{ 
			ArgScriptOption o = command.getOption("hairTexture");
			if (o != null) {
				flags |= FLAG_HAIRTEXTURE;
				hairTexture.parse(o.getSingleArgument());
			}
		}
		{ 
			ArgScriptOption o = command.getOption("hairPrintGeom");
			if (o != null) {
				flags |= FLAG_HAIRPRINTGEOM;
				hairPrintGeom.parse(o.getSingleArgument());
			}
		}
		
		if (flags == 0) {
			throw new ArgScriptException(KEYWORD + ": Must specify at least one option.");
		}
		if (bumpCleared != specCleared) {
			throw new ArgScriptException(KEYWORD + ": Must clear bump and spec channels simultaneously.");
		}
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, new String[0]);
		
		if ((flags & FLAG_DIFFUSE) == FLAG_DIFFUSE) {
			if (diffuseColorIndex != -1) {
				c.putOption(new ArgScriptOption("diffuse", "color" + Integer.toString(diffuseColorIndex + 1)));
			}
			else {
				c.putOption(new ArgScriptOption("diffuse", diffuseColor.toString()));
			}
		}
		if ((flags & FLAG_SPECBUMP) == FLAG_SPECBUMP) {
			c.putOption(new ArgScriptOption("specBump", ArgScript.createFloatList(specBump[0], specBump[1] * 60f, specBump[2])));
		}
		else {
			if ((flags & FLAG_SPEC) == FLAG_SPEC) c.putOption(new ArgScriptOption("spec", Float.toString(specBump[0])));
			if ((flags & FLAG_BUMP) == FLAG_BUMP) c.putOption(new ArgScriptOption("bump", Float.toString(specBump[2])));
			if ((flags & FLAG_SPECEXP) == FLAG_SPECEXP) c.putOption(new ArgScriptOption("exponent", Float.toString(specBump[1] * 60f)));
		}
		if ((flags & FLAG_GLOSS) == FLAG_GLOSS) c.putOption(new ArgScriptOption("gloss", Float.toString(gloss)));
		if ((flags & FLAG_PHONG) == FLAG_PHONG) c.putOption(new ArgScriptOption("phong", Float.toString(phong)));
		if ((flags & FLAG_PARTBUMPSCALE) == FLAG_PARTBUMPSCALE) c.putOption(new ArgScriptOption("partBumpScale", Float.toString(partBumpScale)));
		if ((flags & FLAG_PARTSPECSCALE) == FLAG_PARTSPECSCALE) c.putOption(new ArgScriptOption("partSpecScale", Float.toString(partSpecScale)));
		if ((flags & FLAG_HAIR) == FLAG_HAIR) {
			c.putOption(new ArgScriptOption("hair", Float.toString(field_38), Float.toString((float) Math.toDegrees(field_34)), 
					Float.toString(field_3C), Float.toString(field_40), Float.toString(field_44), Float.toString(field_48),
					Float.toString(field_4C), Float.toString(field_50), Integer.toString(field_54 ? 1 : 0)));
		}
		if ((flags & FLAG_HAIRTEXTURE) == FLAG_HAIRTEXTURE) c.putOption(new ArgScriptOption("hairTexture", hairTexture.toString()));
		if ((flags & FLAG_HAIRPRINTGEOM) == FLAG_HAIRPRINTGEOM) c.putOption(new ArgScriptOption("hairPrintGeom", hairPrintGeom.toString()));
		
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
			"rgb", "diffuse", "specBump", "spec", "bump", "specExp", "exponent", "gloss", "phong", 
			"partBumpScale", "partSpecScale", "hair", "hairTexture", "hairPrintGeom"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			KEYWORD,
			// KEYWORD_CLEAR // this one isn't supported in SporeModder
		};
	}
}
