package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class FastParticleEffect extends EffectComponent {
	
	public static final int TYPE = 0x0C;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "fastParticles";
	
	private static final ArgScriptEnum ENUM_ALIGNMENT = new ArgScriptEnum(new String[] {
			"camera", "ground", "source", "dirX", "dirY", "dirZ", "zPole", "sunPole", "cameraLocation"
	}, new int[] {
			0, 1, 5, 2, 3, 4, 6, 7, 8
	});
	
	private static final int SOURCE_ROUND = 0x10;
	
	private static final int EMIT_BASE = 8;  // are we sure ?
	
	private static final int FLAG_SUSTAIN = 4;
	private static final int FLAG_INJECT = 1;
	private static final int FLAG_MANTAIN = 2;
	private static final int RATE_SIZESCALE = 0x100;  // are we sure ?
	private static final int RATE_AREASCALE = 0x200;  // are we sure ?
	private static final int RATE_VOLUMESCALE = 0x400;  // are we sure ?
	
	private int flags;  // & 7FF
	private final float[] life = new float[2];
	private float prerollTime;
	private final float[] emitDelay = {-1, -1};
	private final float[] emitRetrigger = {-1, -1};
	// emit -dir (0, 0, 1) 0.3 -speed 4 --> [-0.3, -0.3, 0.7] [0.3, 0.3, 1.3]
	// so, (0, 0, 1) is the center of the direction bb, and 0.3 the size
	private final float[] emitDirectionBBMin = {0, 0, 1}; //default value
	private final float[] emitDirectionBBMax = {0, 0, 1};
	private final float[] emitSpeed = new float[2];
	// I think it's the source of the particle emit. Both values are the same if it's a point
	private final float[] emitVolumeBBMin = new float[3];
	private final float[] emitVolumeBBMax = new float[3];
	
	private float[] rate = new float[0];
	private float rateLoop; //In secs, the time the main effect lasts and starts again
	private int rateCurveCycles; //short
	private float rateSpeedScale;
	
	private float[] size = new float[] {1};
	private EffectColor[] color = new EffectColor[] {EffectColor.WHITE};
	private float[] alpha = new float[] {1};
	
	private final TextureSlot texture = new TextureSlot();
	
	private int alignMode;  // byte
	private final float[] directionalForcesSum = new float[3];
	private float windStrength;
	private float gravityStrength;
	private float radialForce;
	private final float[] radialForceLocation = new float[3];
	private float drag;

	public FastParticleEffect(int type, int version) {
		super(type, version);
	}

	public FastParticleEffect(FastParticleEffect effect) {
		super(effect);
		
		flags = effect.flags;
		EffectComponent.copyArray(life, effect.life);
		prerollTime = effect.prerollTime;
		EffectComponent.copyArray(emitDelay, effect.emitDelay);
		EffectComponent.copyArray(emitRetrigger, effect.emitRetrigger);
		EffectComponent.copyArray(emitDirectionBBMin, effect.emitDirectionBBMin);
		EffectComponent.copyArray(emitDirectionBBMax, effect.emitDirectionBBMax);
		EffectComponent.copyArray(emitSpeed, effect.emitSpeed);
		EffectComponent.copyArray(emitVolumeBBMin, effect.emitVolumeBBMin);
		EffectComponent.copyArray(emitVolumeBBMax, effect.emitVolumeBBMax);
		
		rate = EffectComponent.copyArray(effect.rate);
		rateLoop = effect.rateLoop;
		rateCurveCycles = effect.rateCurveCycles;
		rateSpeedScale = effect.rateSpeedScale;
		
		size = EffectComponent.copyArray(effect.size);
		alpha = EffectComponent.copyArray(effect.alpha);
		color = EffectComponent.copyArray(effect.color);
		
		texture.copy(effect.texture);
		
		alignMode = effect.alignMode;
		
		EffectComponent.copyArray(directionalForcesSum, effect.directionalForcesSum);
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		radialForce = effect.radialForce;
		EffectComponent.copyArray(radialForceLocation, effect.radialForceLocation);
		drag = effect.drag;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		
		life[0] = in.readLEFloat(); 
		life[1] = in.readLEFloat();
		prerollTime = in.readFloat();
		emitDelay[0] = in.readLEFloat(); 
		emitDelay[1] = in.readLEFloat();
		emitRetrigger[0] = in.readLEFloat(); 
		emitRetrigger[1] = in.readLEFloat();
		emitDirectionBBMin[0] = in.readLEFloat(); 
		emitDirectionBBMin[1] = in.readLEFloat(); 
		emitDirectionBBMin[2] = in.readLEFloat();
		emitDirectionBBMax[0] = in.readLEFloat(); 
		emitDirectionBBMax[1] = in.readLEFloat(); 
		emitDirectionBBMax[2] = in.readLEFloat();
		emitSpeed[0] = in.readLEFloat(); 
		emitSpeed[1] = in.readLEFloat();
		emitVolumeBBMin[0] = in.readLEFloat(); 
		emitVolumeBBMin[1] = in.readLEFloat(); 
		emitVolumeBBMin[2] = in.readLEFloat();
		emitVolumeBBMax[0] = in.readLEFloat(); 
		emitVolumeBBMax[1] = in.readLEFloat(); 
		emitVolumeBBMax[2] = in.readLEFloat();
		
		rate = new float[in.readInt()];
		in.readFloats(rate);
		rateLoop = in.readFloat();
		rateCurveCycles = in.readShort();
		rateSpeedScale = in.readFloat();
		
		size = new float[in.readInt()];
		in.readFloats(size);
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		alpha = new float[in.readInt()];
		in.readFloats(alpha);
		
		texture.read(in);
		
		alignMode = in.readUByte();
		directionalForcesSum[0] = in.readLEFloat();
		directionalForcesSum[1] = in.readLEFloat();
		directionalForcesSum[2] = in.readLEFloat();
		windStrength = in.readFloat();
		gravityStrength = in.readFloat();
		radialForce = in.readFloat();
		radialForceLocation[0] = in.readLEFloat();
		radialForceLocation[1] = in.readLEFloat();
		radialForceLocation[2] = in.readLEFloat();
		drag = in.readFloat();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeLEFloats(life);
		out.writeFloat(prerollTime);
		out.writeLEFloats(emitDelay);
		out.writeLEFloats(emitRetrigger);
		out.writeLEFloats(emitDirectionBBMin);
		out.writeLEFloats(emitDirectionBBMax);
		out.writeLEFloats(emitSpeed);
		out.writeLEFloats(emitVolumeBBMin);
		out.writeLEFloats(emitVolumeBBMax);
		
		out.writeInt(rate.length);
		out.writeFloats(rate);
		out.writeFloat(rateLoop);
		out.writeShort(rateCurveCycles);
		out.writeFloat(rateSpeedScale);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		
		out.writeInt(color.length);
		for (EffectColor c : color) c.writeLE(out);
		
		out.writeInt(alpha.length);
		out.writeFloats(alpha);
		
		texture.write(out);

		out.writeUByte(alignMode);
		out.writeLEFloats(directionalForcesSum);
		out.writeFloat(windStrength);
		out.writeFloat(gravityStrength);
		out.writeFloat(radialForce);
		out.writeLEFloats(radialForceLocation);
		out.writeFloat(drag);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		ArgScriptCommand c = null;
		
		if ((c = block.getCommand("color")) != null) {
			color = ArgScript.stringsToColors(c.getArguments());
		}
		if ((c = block.getCommand("alpha")) != null) {
			alpha = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("size")) != null) {
			size = ArgScript.stringsToFloats(c.getArguments());
		}
		
		parseSource(block);
		parseEmit(block);
		parseForce(block);
		parseLife(block);
		parseRate(block);
		parseMantain(block);
		parseInject(block);
		if ((c = block.getCommand("texture")) != null) {
			texture.parse(c);
		}
		if ((c = block.getCommand("material")) != null) {
			texture.parse(c);
			texture.setDrawMode(TextureSlot.DRAWMODE_NONE);
		}
		if ((c = block.getCommand("align")) != null) {
			alignMode = ENUM_ALIGNMENT.getValue(c.getSingleArgument());
		}
		
		return true;
	}
	
	private void parseSource(ArgScriptBlock block) throws ArgScriptException {
		//TODO Warning, Spore does some weird checks for most of these
		ArgScriptCommand c = null;
		if ((c = block.getCommand("source")) != null) {
			// disable round source
			flags &= ~SOURCE_ROUND;
			float min_x = 0;
			float min_y = 0;
			float min_z = 0;
			float max_x = 0;
			float max_y = 0;
			float max_z = 0;
			String arg = null;
			List<String> args = null;
			
			if (!c.hasFlag("point")) {
				
				if ((arg = c.getOptionArg("square")) != null)
				{
					float value = Float.parseFloat(arg);
					min_x -= value;
					min_y -= value;
					max_x += value;
					max_y += value;
				}
				else if ((arg = c.getOptionArg("quad")) != null)
				{
					float[] list = ArgScript.parseFloatList(arg, 2);
					min_x -= list[0];
					min_y -= list[1];
					max_x += list[0];
					max_y += list[1];
				}
				else if ((arg = c.getOptionArg("cube")) != null)
				{
					float value = Float.parseFloat(arg);
					min_x -= value;
					min_y -= value;
					min_z -= value;
					max_x += value;
					max_y += value;
					max_z += value;
				}
				else if ((arg = c.getOptionArg("box")) != null)
				{
					float[] list = ArgScript.parseFloatList(arg, 3);
					min_x -= list[0];
					min_y -= list[1];
					min_z -= list[2];
					max_x += list[0];
					max_y += list[1];
					max_z += list[2];
				}
				else if ((arg = c.getOptionArg("circle")) != null)
				{
					flags |= SOURCE_ROUND;
					float value = Float.parseFloat(arg);
					min_x -= value;
					min_y -= value;
					max_x += value;
					max_z += value;
				}
				else if ((arg = c.getOptionArg("sphere")) != null)
				{
					flags |= SOURCE_ROUND;
					float value = Float.parseFloat(arg);
					min_x -= value;
					min_y -= value;
					min_z -= value;
					max_x += value;
					max_y += value;
					max_z += value;
				}
				else if ((arg = c.getOptionArg("ellipse")) != null || (arg = c.getOptionArg("ellipsoid")) != null)
				{
					flags |= SOURCE_ROUND;
					float[] list = ArgScript.parseFloatList(arg, 3);
					min_x -= list[0];
					min_y -= list[1];
					min_z -= list[2];
					max_x += list[0];
					max_y += list[1];
					max_z += list[2];
				}
//				else if ((args = c.getOptionArgs("ring", 2)) != null)
//				{
//					float value = Float.parseFloat(args.get(0));
//					torusWidth = ArgScript.parseRangedFloat(args.get(1), 0, 1);
//					min_x -= value;
//					min_y -= value;
//					max_x += value;
//					max_y += value;
//				}
//				else if ((args = c.getOptionArgs("torus", 2)) != null)
//				{
//					float[] list = ArgScript.parseFloatList(args.get(0), 3);
//					torusWidth = ArgScript.parseRangedFloat(args.get(1), 0, 1);
//					min_x -= list[0];
//					min_y -= list[1];
//					min_z -= list[2];
//					max_x += list[0];
//					max_y += list[1];
//					max_z += list[2];
//				}
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				float[] list = ArgScript.parseFloatList(arg, 3);
				min_x += list[0];
				max_x += list[0];
				min_y += list[1];
				max_y += list[1];
				min_z += list[2];
				max_z += list[2];
			}
			
			emitVolumeBBMin[0] = min_x;
			emitVolumeBBMin[1] = min_y;
			emitVolumeBBMin[2] = min_z;
			emitVolumeBBMax[0] = max_x;
			emitVolumeBBMax[1] = max_y;
			emitVolumeBBMax[2] = max_z;
			
		}
	}
	
	private void writeSource(ArgScriptBlock block) {
		ArgScriptCommand c = new ArgScriptCommand("source");
		boolean shouldWrite = false;
		
		// get the offset
		// just divide per 2 since when not using offset, values are symmetric
//		float[] offset = new float[3];
//		offset[0] = (-emitVolumeBBMin[0] + emitVolumeBBMax[0]) / 2;
//		offset[1] = (-emitVolumeBBMin[1] + emitVolumeBBMax[1]) / 2;
//		offset[2] = (-emitVolumeBBMin[2] + emitVolumeBBMax[2]) / 2;
//		float min_x = emitVolumeBBMin[0] - offset[0];
//		float min_y = emitVolumeBBMin[1] - offset[1];
//		float min_z = emitVolumeBBMin[2] - offset[2];
//		float max_x = emitVolumeBBMax[0] - offset[0];
//		float max_y = emitVolumeBBMax[1] - offset[1];
//		float max_z = emitVolumeBBMax[2] - offset[2];
		
		float[] offset = new float[3];
		float min_x = (emitVolumeBBMin[0] - emitVolumeBBMax[0]) / 2;
		float min_y = (emitVolumeBBMin[1] - emitVolumeBBMax[1]) / 2;
		float min_z = (emitVolumeBBMin[2] - emitVolumeBBMax[2]) / 2;
		float max_x = -min_x;
		float max_y = -min_y;
		float max_z = -min_z;
		offset[0] = emitVolumeBBMin[0] - min_x;
		offset[1] = emitVolumeBBMin[1] - min_y;
		offset[2] = emitVolumeBBMin[2] - min_z;
		
		if (emitVolumeBBMin[0] != 0 || emitVolumeBBMin[1] != 0 || emitVolumeBBMin[2] != 0 
				|| emitVolumeBBMax[0] != 0 || emitVolumeBBMax[1] != 0 || emitVolumeBBMax[2] != 0) {
			if ((flags & SOURCE_ROUND) == SOURCE_ROUND)
			{
				if (max_x == max_y && max_y == max_z) {
					c.putOption(new ArgScriptOption("sphere", Float.toString(max_x)));
				}
				else {
					c.putOption(new ArgScriptOption("ellipse", ArgScript.createFloatList(max_x, max_y, max_z)));
				}
			}
			else 
			{
//				if (torusWidth != -1) {
//					if (min_z != 0 || max_z != 0) {
//						// we use the max because we want the added value (even if it's negative)
//						c.putOption(new ArgScriptOption("torus", ArgScript.createFloatList(max_x, max_y, max_z),
//								Float.toString(torusWidth)));
//					}
//					else {
//						c.putOption(new ArgScriptOption("ring", Float.toString(max_x),
//								Float.toString(torusWidth)));
//					}
//				}
//				else {
					if (min_z != 0 || max_z != 0) {
						if (max_x == max_y && max_y == max_z) {
							c.putOption(new ArgScriptOption("cube", Float.toString(max_x)));
						}
						else {
							c.putOption(new ArgScriptOption("box", ArgScript.createFloatList(max_x, max_y, max_z)));
						}
					}
					else {
						if (max_x == max_y) {
							c.putOption(new ArgScriptOption("square", Float.toString(max_x)));
						}
						else {
							c.putOption(new ArgScriptOption("quad", ArgScript.createFloatList(max_x, max_y)));
						}
					}
//				}
			}
			if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) {
				c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(offset)));
			}
			
			shouldWrite = true;
		}
		
		if (shouldWrite) block.putCommand(c);
	}

	private void parseEmit(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("emit");
		if (c != null) {
			List<String> args = null;
			if ((args = c.getOptionArgs("speed", 1, 2)) != null)
			{
				float value = Float.parseFloat(args.get(0));
				float vary = args.size() > 1 ? Float.parseFloat(args.get(1)) : 0;
				emitSpeed[0] = value - vary;
				emitSpeed[1] = value + vary;
			}
			if ((args = c.getOptionArgs("dir", 1, 2)) != null)
			{
				float[] list = ArgScript.parseFloatList(args.get(0), 3);
				float[] vary = args.size() > 1 ? ArgScript.parseFloatList(args.get(1), 3) : new float[3];
				
				float value = (float) Math.sqrt(list[0]*list[0] + list[1]*list[1] + list[2]*list[2]);
				emitDirectionBBMin[0] = list[0] * value - vary[0];
				emitDirectionBBMin[1] = list[1] * value - vary[1];
				emitDirectionBBMin[2] = list[2] * value - vary[2];
				emitDirectionBBMax[0] = list[0] * value + vary[0];
				emitDirectionBBMax[1] = list[1] * value + vary[1];
				emitDirectionBBMax[2] = list[2] * value + vary[2];
			}
			
			if (c.hasFlag("base")) flags |= EMIT_BASE;
		}
	}
	
	private void writeEmit(ArgScriptBlock block) {
		ArgScriptCommand c = new ArgScriptCommand("emit");
		boolean shouldWrite = false;
		
		if (emitSpeed[0] != 0 || emitSpeed[1] != 0) {
			shouldWrite = true;
			float value = (emitSpeed[0] - emitSpeed[1]) / 2;
			if (value == 0) {
				c.putOption(new ArgScriptOption("speed", Float.toString(emitSpeed[0])));
			}
			else {
				c.putOption(new ArgScriptOption("speed", Float.toString(emitSpeed[0] + value), Float.toString(value)));
			}
		}
		
		if (emitDirectionBBMin[0] != 0 || emitDirectionBBMin[1] != 0 || emitDirectionBBMin[2] != 1
				|| emitDirectionBBMax[0] != 0 || emitDirectionBBMax[1] != 0 || emitDirectionBBMax[2] != 1) {
			shouldWrite = true;
			float[] dir = new float[3];
			float[] vary = new float[3];
			vary[0] = (emitDirectionBBMax[0] - emitDirectionBBMin[0]) / 2;
			vary[1] = (emitDirectionBBMax[1] - emitDirectionBBMin[1]) / 2;
			vary[2] = (emitDirectionBBMax[2] - emitDirectionBBMin[2]) / 2;
			dir[0] = emitDirectionBBMax[0] - vary[0];
			dir[1] = emitDirectionBBMax[1] - vary[1];
			dir[2] = emitDirectionBBMax[2] - vary[2];
			ArgScriptOption o = new ArgScriptOption("dir", ArgScript.createFloatList(dir));
			if (vary[0] != 0 || vary[1] != 0 || vary[2] != 0) o.addArgument(ArgScript.createFloatList(vary));
		}
		
		if ((flags & EMIT_BASE) == EMIT_BASE) {
			shouldWrite = true;
			c.putFlag("base");
		}
		
		if (shouldWrite) block.putCommand(c);
	}
	
	private void writeResource(ArgScriptBlock block) {
		ArgScriptCommand c = null;
		if (texture.getDrawMode() == TextureSlot.DRAWMODE_NONE) {
			c = new ArgScriptCommand("material", texture.getResource().toString());
		}
		else {
			c = new ArgScriptCommand("texture", texture.getResource().toString());
		}
		
		if (c != null) {
			texture.toCommand(c);
			block.putCommand(c);
		}
	}
	
	private void parseRateMain(ArgScriptCommand c) throws ArgScriptException {
		List<String> args = null;
		if ((args = c.getOptionArgs("loop", 1, 2)) != null) {
			rateLoop = Float.parseFloat(args.get(0));
			rateCurveCycles = args.size() > 1 ? Hasher.decodeShort(args.get(1)) : 0;
		}
		else if ((args = c.getOptionArgs("single", 0, 1)) != null) {
			rateLoop = args.size() > 0 ? Float.parseFloat(args.get(0)) : 0.1f;
			rateCurveCycles = 1;
		}
		else if ((args = c.getOptionArgs("sustain", 1, 2)) != null) {
			flags |= FLAG_SUSTAIN;
			rateLoop = Float.parseFloat(args.get(0));
			rateCurveCycles = args.size() > 1 ? Hasher.decodeShort(args.get(1)) : 1;
		}
		if (c.hasFlag("sizeScale")) flags |= RATE_SIZESCALE;
		if (c.hasFlag("areaScale")) flags |= RATE_AREASCALE;
		if (c.hasFlag("volumeScale")) flags |= RATE_VOLUMESCALE;
		String arg = c.getOptionArg("speedScale");
		if (arg != null) {
			rateSpeedScale = Float.parseFloat(arg);
		}
		if ((args = c.getOptionArgs("delay", 1, 2)) != null) {
			emitDelay[0] = Float.parseFloat(args.get(0));
			emitDelay[1] = args.size() > 1 ? Float.parseFloat(args.get(0)) : emitDelay[1];
		}
		if ((args = c.getOptionArgs("trigger", 1, 2)) != null) {
			emitRetrigger[0] = Float.parseFloat(args.get(0));
			emitRetrigger[1] = args.size() > 1 ? Float.parseFloat(args.get(0)) : emitRetrigger[1];
		}
	}
	private void parseRate(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("rate");
		if (c != null) {
			rate = ArgScript.stringsToFloats(c.getArguments());
			parseRateMain(c);
		}
	}
	
	private void parseInject(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("inject");
		if (c != null) {
			rate = new float[1];
			rate[0] = Float.parseFloat(c.getSingleArgument());
			flags |= FLAG_INJECT;
			rateLoop = 0.01f;
			rateCurveCycles = 1;
			parseRateMain(c);
		}
	}
	
	private void parseMantain(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("mantain");
		if (c != null) {
			rate = new float[1];
			rate[0] = Float.parseFloat(c.getSingleArgument());
			flags |= FLAG_MANTAIN;
			
			List<String> args = null;
			if ((args = c.getOptionArgs("delay", 1, 2)) != null) {
				emitDelay[0] = Float.parseFloat(args.get(0));
				emitDelay[1] = args.size() > 1 ? Float.parseFloat(args.get(0)) : emitDelay[1];
			}
		}
	}
	
	// writes 'rate', 'inject' or 'mantain'
	private void writeRate(ArgScriptBlock block) {
		if ((flags & FLAG_INJECT) == FLAG_INJECT) {
			ArgScriptCommand c = new ArgScriptCommand("inject", Float.toString(rate[0]));
			writeRateMain(c, true);
			block.putCommand(c);
		}
		else if ((flags & FLAG_MANTAIN) == FLAG_MANTAIN) {
			ArgScriptCommand c = new ArgScriptCommand("mantain", Float.toString(rate[0]));
			if (emitDelay[0] != 0 || emitDelay[1] != 0) {
				ArgScriptOption o = new ArgScriptOption("delay", Float.toString(emitDelay[0]));
				if (emitDelay[1] != emitDelay[0]) o.addArgument(Float.toString(emitDelay[1]));
				c.putOption(o);
			}
			block.putCommand(c);
		}
		else {
			ArgScriptCommand c = new ArgScriptCommand("rate", ArgScript.floatsToStrings(rate));
			writeRateMain(c, false);
			block.putCommand(c);
		}
	}
	private void writeRateMain(ArgScriptCommand c, boolean isInject) {
		if ((flags & FLAG_SUSTAIN) == FLAG_SUSTAIN) {
			ArgScriptOption o = new ArgScriptOption("sustain", Float.toString(rateLoop));
			if (rateCurveCycles != 1) o.addArgument(Integer.toString(rateCurveCycles));
			c.putOption(o);
		}
		else if (rateLoop != 0.1f && rateCurveCycles == 1) {
			c.putOption(new ArgScriptOption("single", Float.toString(rateLoop)));
		}
		else {
			ArgScriptOption o = new ArgScriptOption("loop", Float.toString(rateLoop));
			if (rateCurveCycles != 0) o.addArgument(Integer.toString(rateCurveCycles));
			c.putOption(o);
		}
	}
	
	private void parseLife(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("life");
		if (c != null) {
			List<String> args = c.getArguments(1, 2);
			float value = Float.parseFloat(args.get(0));
			float vary = args.size() > 1 ? Float.parseFloat(args.get(1)) : 0;
			life[0] = value - vary;
			life[1] = value + vary;
			String arg = c.getOptionArg("preroll");
			if (arg != null) {
				prerollTime = Float.parseFloat(arg);
			} else {
				prerollTime = value > 0.5f ? value : 0.5f;
			}
		}
	}
	
	private void writeLife(ArgScriptBlock block) {
		ArgScriptCommand c = new ArgScriptCommand("life");
		
		float vary = (life[1] - life[0]) / 2;
		float value = life[0] + vary;
		c.addArgument(Float.toString(value));
		if (vary != 0) c.addArgument(Float.toString(vary));
		
		if (prerollTime != 0.5f) {
			c.putOption(new ArgScriptOption("preroll", Float.toString(prerollTime)));
		}
		
		block.putCommand(c);
	}

	private void parseForce(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("force");
		if (c != null) {
			if (c.hasFlag("reset")) {
				directionalForcesSum[0] = 0;
				directionalForcesSum[1] = 0;
				directionalForcesSum[2] = 0;
			}
			String arg;
			List<String> args;
			
			if ((arg = c.getOptionArg("gravity")) != null) {
				directionalForcesSum[2] -= Float.parseFloat(arg);
			}
			if ((args = c.getOptionArgs("wind", 1, 2)) != null) {
				// make it a unit vector
				float[] vec = ArgScript.parseFloatList(args.get(0), 3);
				float invMod = (float) (1 / Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]));
				if (args.size() > 1) {
					invMod *= Float.parseFloat(args.get(1));
				}
				directionalForcesSum[0] = vec[0] * invMod;
				directionalForcesSum[1] = vec[1] * invMod;
				directionalForcesSum[2] = vec[2] * invMod;
			}
			if ((arg = c.getOptionArg("worldWind")) != null) {
				windStrength = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("worldGravity")) != null) {
				gravityStrength -= Float.parseFloat(arg);
			}
			if ((args = c.getOptionArgs("bomb", 1, 2)) != null) {
				radialForce = Float.parseFloat(args.get(0));
				if (args.size() > 1) {
					ArgScript.parseFloatList(args.get(1), radialForceLocation);
				}
			}
			if ((arg = c.getOptionArg("drag")) != null) {
				drag = Float.parseFloat(arg);
			}
		} 
	}
	
	private void writeForce(ArgScriptBlock block) {
		if (directionalForcesSum[0] != 0 || directionalForcesSum[1] != 0 || directionalForcesSum[0] != 0 || windStrength != 0 || gravityStrength != 0
				|| drag != 0 || radialForce != 0) {
			ArgScriptCommand c = new ArgScriptCommand("force");
			
			if (directionalForcesSum[0] == 0 && directionalForcesSum[1] == 0 && directionalForcesSum[2] != 0) {
				c.putOption(new ArgScriptOption("gravity", Float.toString(-directionalForcesSum[2])));
			}
			else if (directionalForcesSum[0] != 0 || directionalForcesSum[1] != 0 || directionalForcesSum[2] != 0) {
				c.putOption(new ArgScriptOption("wind", ArgScript.createFloatList(directionalForcesSum)));
			}
			
			if (windStrength != 0) c.putOption(new ArgScriptOption("worldWind", Float.toString(windStrength)));
			if (gravityStrength != 0) c.putOption(new ArgScriptOption("worldGravity", Float.toString(gravityStrength)));
			
			if (drag != 0) c.putOption(new ArgScriptOption("drag", Float.toString(drag)));
			
			if (radialForce != 0) {
				ArgScriptOption o = new ArgScriptOption("bomb", Float.toString(radialForce));
				if (radialForceLocation[0] != 0 || radialForceLocation[1] != 0 || radialForceLocation[2] != 0) {
					o.addArgument(ArgScript.createFloatList(radialForceLocation));
				}
				c.putOption(o);
			}
			
			block.putCommand(c);
		}
	}
	
	@Override
	public boolean toBlock(ArgScriptBlock block) {
		block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		block.putCommand(new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha)));
		block.putCommand(new ArgScriptCommand("size", ArgScript.floatsToStrings(size)));
		writeSource(block);
		writeEmit(block);
		writeForce(block);
		writeLife(block);
		writeRate(block);
		writeResource(block);
		if (alignMode != 0) block.putCommand(new ArgScriptCommand("align", ENUM_ALIGNMENT.getKey(alignMode)));
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		throw new UnsupportedOperationException("Inline fastParticle effect is not supported.");
	}

	// For Syntax Highlighting
	// all words are included in ParticleEffect.class
	
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
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
		};
	}
}
