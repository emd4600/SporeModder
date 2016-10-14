package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.files.formats.argscript.ArgScript;

public class Surface {
	
	private static final int FLAG_PIN = 2;
	private static final int FLAG_PIN_MOVE = 4;
	private static final int FLAG_PIN_EMIT = 8;
	private static final int FLAG_ALIGN = 0x20;
	private static final int FLAG_SOURCE_SPACE = 0x40;
	private static final int FLAG_WORLD_SPACE = 0x80;
	
	private int flags;
	private final ResourceID surfaceMapID = new ResourceID();
	private float bounce;
	private float slide;
	private float collisionRadius;
	private float deathProbability;
	private float pinOffset;
	private int collisionEffectIndex = -1;
	private int deathEffectIndex = -1;
	private float[][] surfacePoints = new float[0][3];
	
	public Surface() {}
	public Surface(Surface other) {
		flags = other.flags;
		surfaceMapID.copy(other.surfaceMapID);
		bounce = other.bounce;
		slide = other.slide;
		collisionRadius = other.collisionRadius;
		deathProbability = other.deathProbability;
		pinOffset = other.pinOffset;
		collisionEffectIndex = other.collisionEffectIndex;
		deathEffectIndex = other.deathEffectIndex;
		surfacePoints = new float[other.surfacePoints.length][3];
		for (int i = 0; i < surfacePoints.length; i++) {
			surfacePoints[i] = new float[3];
			surfacePoints[i][0] = other.surfacePoints[i][0];
			surfacePoints[i][1] = other.surfacePoints[i][1];
			surfacePoints[i][2] = other.surfacePoints[i][2];
		}
	}

	public boolean read(InputStreamAccessor in) throws IOException 
	{
		flags = in.readInt(); // & 0x3FFF
		surfaceMapID.read(in);
		bounce = in.readFloat();
		slide = in.readFloat();
		collisionRadius = in.readFloat();
		deathProbability = in.readFloat();
		pinOffset = in.readFloat();
		collisionEffectIndex = in.readInt();
		deathEffectIndex = in.readInt();
		// ?
		surfacePoints = new float[in.readInt()][3];
		for (int s = 0; s < surfacePoints.length; s++) {
			surfacePoints[s] = new float[3];
			in.readLEFloats(surfacePoints[s]);
		}
		
		return true;
	}
	
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		surfaceMapID.write(out);
		out.writeFloat(bounce);
		out.writeFloat(slide);
		out.writeFloat(collisionRadius);
		out.writeFloat(deathProbability);
		out.writeFloat(pinOffset);
		out.writeInt(collisionEffectIndex);
		out.writeInt(deathEffectIndex);
		out.writeInt(surfacePoints.length);
		for (float[] f : surfacePoints) out.writeLEFloats(f);
		
		return true;
	}
	
	public void fixEffectIndices(int baseIndex) {
		if (collisionEffectIndex != -1) {
			collisionEffectIndex += baseIndex;
		}
		if (deathEffectIndex != -1) {
			deathEffectIndex += baseIndex;
		}
	}
	
	public boolean parse(ArgScriptCommand command, EffectMain parent) throws ArgScriptException, IOException {
		
		List<String> args = command.getArguments(0, 1);
		if (args.size() > 0) {
			surfaceMapID.parseSpecial(args.get(0));
		}
		
		if (command.hasFlag("pin")) flags |= FLAG_PIN;
		if (command.hasFlag("pinMove")) flags |= FLAG_PIN_MOVE;
		if (command.hasFlag("pinEmit")) {
			flags |= FLAG_PIN_EMIT;
			ArgScriptOption oSurfaceOffset = command.getArgOption("surfaceOffset");
			if (oSurfaceOffset != null) {
				pinOffset = Float.parseFloat(oSurfaceOffset.getSingleArgument());
			}
		}
		if (command.hasFlag("align")) flags |= FLAG_ALIGN;
		if (command.hasFlag("sourceSpace")) flags |= FLAG_SOURCE_SPACE;
		if (command.hasFlag("worldSpace")) flags |= FLAG_WORLD_SPACE;
		
		ArgScriptOption oBounce = command.getArgOption("bounce");
		if (oBounce != null) bounce = Float.parseFloat(oBounce.getSingleArgument());
		
		ArgScriptOption oSlide = command.getArgOption("slide");
		if (oSlide != null) slide = Float.parseFloat(oSlide.getSingleArgument());
		
		ArgScriptOption oRadius = command.getArgOption("radius");
		if (oRadius != null) collisionRadius = Float.parseFloat(oRadius.getSingleArgument());
		
		ArgScriptOption oDeath = command.getArgOption("death");
		if (oDeath != null) deathProbability = Float.parseFloat(oDeath.getSingleArgument());
		
		ArgScriptOption oCollideEffect = command.getArgOption("collideEffect");
		if (oCollideEffect != null) collisionEffectIndex = EffectMain.getEffectIndex(parent.getEffectMap(), parent.getImports(), VisualEffect.TYPE, oCollideEffect.getSingleArgument());
		
		ArgScriptOption oDeathEffect = command.getArgOption("deathEffect");
		if (oDeathEffect != null) deathEffectIndex = EffectMain.getEffectIndex(parent.getEffectMap(), parent.getImports(), VisualEffect.TYPE, oDeathEffect.getSingleArgument());
		
		//TODO what's the difference between basis and rect?
		ArgScriptOption oBasis = command.getArgOption("basis");
		if (oBasis != null) {
			surfacePoints = new float[oBasis.getArgumentCount()][3];
			args = oBasis.getArguments();
			for (int i = 0; i < surfacePoints.length; i++) {
				surfacePoints[i] = ArgScript.parseFloatList(args.get(i));
			}
		}
		
		return true;
	}
	
	public ArgScriptCommand toCommand(ArgScriptCommand command, EffectMain parent) {
		
		if (!surfaceMapID.isDefault()) {
			command.addArgument(surfaceMapID.toString());
		}
		
		if ((flags & FLAG_PIN) == FLAG_PIN) command.putFlag("pin");
		if ((flags & FLAG_PIN_MOVE) == FLAG_PIN_MOVE) command.putFlag("pinMove");
		if ((flags & FLAG_PIN_EMIT) == FLAG_PIN_EMIT) {
			command.putFlag("pin");
			if (pinOffset != 0) command.putOption(new ArgScriptOption("surfaceOffset", Float.toString(pinOffset)));
		}
		if ((flags & FLAG_ALIGN) == FLAG_ALIGN) command.putFlag("align");
		if ((flags & FLAG_SOURCE_SPACE) == FLAG_SOURCE_SPACE) command.putFlag("sourceSpace");
		if ((flags & FLAG_WORLD_SPACE) == FLAG_WORLD_SPACE) command.putFlag("worldSpace");
		
		if (bounce != 0) command.putOption(new ArgScriptOption("bounce", Float.toString(bounce)));
		if (slide != 0) command.putOption(new ArgScriptOption("slide", Float.toString(slide)));
		if (collisionRadius != 0) command.putOption(new ArgScriptOption("radius", Float.toString(collisionRadius)));
		if (deathProbability != 0) command.putOption(new ArgScriptOption("death", Float.toString(deathProbability)));
		
		if (collisionEffectIndex != -1) {
			Effect collideEffect = getCollisionEffect(parent);
			command.putOption(new ArgScriptOption("collideEffect", collideEffect.getName()));
		}
		if (deathEffectIndex != -1) {
			Effect deathEffect = getDeathEffect(parent);
			command.putOption(new ArgScriptOption("deathEffect", deathEffect.getName()));
		}
		
		if (surfacePoints.length > 0) {
			//TODO what's the difference between basis and rect?
			ArgScriptOption oSurfacePoints = new ArgScriptOption("basis");
			for (float[] f : surfacePoints) {
				oSurfacePoints.addArgument(ArgScript.createFloatList(f));
			}
			command.putOption(oSurfacePoints);
		}
		
		return command;
	}
	public int getCollisionEffectIndex() {
		return collisionEffectIndex;
	}
	public int getDeathEffectIndex() {
		return deathEffectIndex;
	}
	
	public Effect getCollisionEffect(EffectMain parent) {
		if (collisionEffectIndex != -1) {
			if ((collisionEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
				return new ImportedEffect(parent.getImports().get(collisionEffectIndex & ~EffectMain.IMPORT_MASK));
			}
			else {
				return EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, collisionEffectIndex);
			}
		}
		return null;
	}
	
	public Effect getDeathEffect(EffectMain parent) {
		if (deathEffectIndex != -1) {
			if ((deathEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
				return new ImportedEffect(parent.getImports().get(deathEffectIndex & ~EffectMain.IMPORT_MASK));
			}
			else {
				return EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, deathEffectIndex);
			}
		}
		return null;
	}
	
	public Resource getSurfaceMap(EffectMain parent) {
		return parent.getResource(MaterialResource.MASKED_TYPE, surfaceMapID);
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"pin", "pinMove", "pinEmit", "surfaceOffset", "align", "worldSpace", "sourceSpace",
			"bounce", "slide", "radius", "death", "collideEffect", "deathEffect", "basis"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
		};
	}
}
