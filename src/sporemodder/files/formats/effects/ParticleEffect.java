package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class ParticleEffect extends EffectComponent {
	
	public static final int TYPE = 0x01;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "particles";
	
	private static final ArgScriptEnum ENUM_ALIGNMENT = new ArgScriptEnum(new String[] {
			"camera", "ground", "source", "dirX", "dirY", "dirZ", "zPole", "sunPole", "cameraLocation"
	}, new int[] {
			0, 1, 5, 2, 3, 4, 6, 7, 8
	});
	
	private static final ArgScriptEnum ENUM_PHYSICS = new ArgScriptEnum(new String[] {
			"user1", "user2", "user3", "user4", "normal", "unk1", "unk2" // ??
	}, new int[] {
			3, 4, 5, 6, 0, 1, 2
	});
	
	private static final int SOURCE_ROUND = 0x10;
	private static final int SOURCE_SCALEPARTICLES = 0x800;
	private static final int SOURCE_RESETINCOMING = 0x4000000;
	
	private static final int EMIT_SCALEEXISTING = 0x2000000;
	private static final int EMIT_BASE = 8;
	
	private static final int WRAP_SPIRAL = 0x20000000;
	
	private static final int ATTRACTOR_LOCATION = 0x1000000;
	
	private static final int FLAG_RANDOMWALK = 0x80000;
	private static final int RANDOMWALK_WAIT = 0x100000;
	
	private static final int LIFE_PROPAGATEALWAYS = 1;
	private static final int LIFE_PROPAGATEIFKILLED = 2;
	
	private static final int FLAG_SUSTAIN = 4;
	private static final int FLAG_HOLD = 0x10000000;
	private static final int FLAG_KILL = 0x8000000;
	private static final int FLAG_INJECT = 1;
	private static final int FLAG_MANTAIN = 2;
	private static final int RATE_SIZESCALE = 0x100;
	private static final int RATE_AREASCALE = 0x200;
	private static final int RATE_VOLUMESCALE = 0x400;
	
	private static final int FLAG_MODEL = 0x200000;
	private static final int FLAG_ACCEPTCOMPOSITE = 0x400000;
	
	private static final int FLAG_LOOPBOX = 0x40000000;
	
	private static final int EMITMAP_PINTOSURFACE = 0x20;
	private static final int EMITMAP_HEIGHT = 0x40;
	private static final int EMITMAP_DENSITY = 0x80;
	
	private static final int FLAG_COLLIDEMAP = 0x2000;
	private static final int COLLIDE_PINTOMAP = 0x40000;
	private static final int FLAG_KILLOUTSIDEMAP = 0x20000;
	private static final int FLAG_REPULSEMAP = 0x4000;
	private static final int FLAG_ADVECTMAP = 0x8000;
	private static final int FLAG_FORCEMAP = 0x10000;
	
	private int unkFlags;
	private int flags;
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
	private float torusWidth = -1;
	
	private float[] rate = new float[0];
	private float rateLoop; //In secs, the time the main effect lasts and starts again
	private int rateCurveCycles; //short
	private float rateSpeedScale;
	
	private float[] size = new float[] {1};
	private float sizeVary;
	
	private float[] aspectRatio = new float[] {1};
	private float aspectRatioVary;
	
	private float rotationVary;
	private float rotationOffset;
	private float[] rotate = new float[] {0};
	
	private float[] alpha = new float[] {1};
	private float alphaVary;
	
	private EffectColor[] color = new EffectColor[] {EffectColor.WHITE};
	private EffectColor colorVary = EffectColor.BLACK;
	
	private final TextureSlot texture = new TextureSlot();
	
	private int physicsType; //byte
	private int overrideSet; //byte
	private final int[] tileCount = {1, 1}; //byte, byte
	private int alignMode;  // byte
	private int frameStart;  // byte
	private int frameCount;  // byte
	private int frameRandom;  // byte
	private float frameSpeed;
	
	private final float[] directionalForcesSum = new float[3];
	private float windStrength;
	private float gravityStrength;
	private float radialForce;
	private final float[] radialForceLocation = new float[3];
	private float drag;
	private float velocityStretch;
	private float screwRate;
	
	private List<ParticleWiggle> wiggles = new ArrayList<ParticleWiggle>();
	
	private int screenBloomAlphaRate;
	private int screenBloomAlphaBase = 255;
	private int screenBloomScaleRate;
	private int screenBloomScaleBase = 255;
	
	private EffectColor[] loopBoxColor = new EffectColor[0];
	private float[] loopBoxAlpha = new float[0];
	
	private List<Surface> surfaces = new ArrayList<Surface>();
	
	private float mapBounce = 1;
	private float mapRepulseHeight;
	private float mapRepulseStrength;
	private float mapRepulseScoutDistance;
	private float mapRepulseVertical;
	private float mapRepulseKillHeight = -1000000000;
	private float probabilityDeath;
	private final float[] altitudeRange = {-10000, 10000};
	
	private final ResourceID mapForce = new ResourceID();
	private final ResourceID mapEmit = new ResourceID();
	private final ResourceID mapEmitColor = new ResourceID();
	
	private final ParticleRandomWalk randomWalk = new ParticleRandomWalk();
	
	private final float[] attractorOrigin = new float[3];
	private final ParticleAttractor attractor = new ParticleAttractor();
	
	private List<ParticlePathPoint> pathPoints = new ArrayList<ParticlePathPoint>();

	public ParticleEffect(int type, int version) {
		super(type, version);
	}
	
	public ParticleEffect(ParticleEffect effect) {
		super(effect);
		
		unkFlags = effect.unkFlags;
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
		torusWidth = effect.torusWidth;
		
		rate = EffectComponent.copyArray(effect.rate);
		rateLoop = effect.rateLoop;
		rateCurveCycles = effect.rateCurveCycles;
		rateSpeedScale = effect.rateSpeedScale;
		
		size = EffectComponent.copyArray(effect.size);
		sizeVary = effect.sizeVary;
		aspectRatio = EffectComponent.copyArray(effect.aspectRatio);
		aspectRatioVary = effect.aspectRatioVary;
		rotate = EffectComponent.copyArray(effect.rotate);
		rotationVary = effect.rotationVary;
		rotationOffset = effect.rotationOffset;
		alpha = EffectComponent.copyArray(effect.alpha);
		alphaVary = effect.alphaVary;
		color = EffectComponent.copyArray(effect.color);
		colorVary = new EffectColor(effect.colorVary);
		
		texture.copy(effect.texture);
		
		physicsType = effect.physicsType;
		overrideSet = effect.overrideSet;
		tileCount[0] = effect.tileCount[0];
		tileCount[1] = effect.tileCount[1];
		alignMode = effect.alignMode;
		frameStart = effect.frameStart;
		frameCount = effect.frameCount;
		frameSpeed = effect.frameSpeed;
		frameRandom = effect.frameRandom;
		
		EffectComponent.copyArray(directionalForcesSum, effect.directionalForcesSum);
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		radialForce = effect.radialForce;
		EffectComponent.copyArray(radialForceLocation, effect.radialForceLocation);
		drag = effect.drag;
		velocityStretch = effect.velocityStretch;
		screwRate = effect.screwRate;
		
		for (int i = 0; i < effect.wiggles.size(); i++) wiggles.add(new ParticleWiggle(effect.wiggles.get(i)));
		
		screenBloomAlphaRate = effect.screenBloomAlphaRate;
		screenBloomAlphaBase = effect.screenBloomAlphaBase;
		screenBloomScaleRate = effect.screenBloomScaleRate;
		screenBloomScaleBase = effect.screenBloomScaleBase;
		
		loopBoxColor = EffectComponent.copyArray(effect.loopBoxColor);
		loopBoxAlpha = EffectComponent.copyArray(loopBoxAlpha);
		
		for (int i = 0; i < effect.surfaces.size(); i++) surfaces.add(new Surface(effect.surfaces.get(i)));
		
		mapBounce = effect.mapBounce;
		mapRepulseHeight = effect.mapRepulseHeight;
		mapRepulseStrength = effect.mapRepulseStrength;
		mapRepulseScoutDistance = effect.mapRepulseScoutDistance;
		mapRepulseVertical = effect.mapRepulseVertical;
		mapRepulseKillHeight = effect.mapRepulseKillHeight;
		probabilityDeath = effect.probabilityDeath;
		altitudeRange[0] = effect.altitudeRange[0];
		altitudeRange[1] = effect.altitudeRange[1];
		
		mapForce.copy(effect.mapForce);
		mapEmit.copy(effect.mapEmit);
		mapEmitColor.copy(effect.mapEmitColor);
		
		randomWalk.copy(effect.randomWalk);
		
		EffectComponent.copyArray(attractorOrigin, effect.attractorOrigin);
		attractor.copy(effect.attractor);
		
		for (int i = 0; i < effect.pathPoints.size(); i++) pathPoints.add(new ParticlePathPoint(effect.pathPoints.get(i)));
	}
	
	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		unkFlags = in.readInt();
		return readMain(in);
	}

	public boolean readMain(InputStreamAccessor in) throws IOException {
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
		torusWidth = in.readFloat();
		
		rate = new float[in.readInt()];
		in.readFloats(rate);
		rateLoop = in.readFloat();
		rateCurveCycles = in.readShort();
		rateSpeedScale = in.readFloat();
		
		size = new float[in.readInt()];
		in.readFloats(size);
		sizeVary = in.readFloat();
		
		aspectRatio = new float[in.readInt()];
		in.readFloats(aspectRatio);
		aspectRatioVary = in.readFloat();
		
		rotationVary = in.readFloat();
		rotationOffset = in.readFloat();
		rotate = new float[in.readInt()];
		in.readFloats(rotate);
		
		alpha = new float[in.readInt()];
		in.readFloats(alpha);
		alphaVary = in.readFloat();
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		colorVary.readLE(in);
		
		texture.read(in);
		
		physicsType = in.readUByte();
		overrideSet = in.readUByte();
		tileCount[0] = in.readUByte();
		tileCount[1] = in.readUByte();
		alignMode = in.readUByte();
		
		frameStart = in.readUByte();
		frameCount = in.readByte();
		frameRandom = in.readByte();
		frameSpeed = in.readFloat();
		
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
		velocityStretch = in.readFloat();
		screwRate = in.readFloat();
		
		int wiggleCount = in.readInt();
		for (int i = 0; i < wiggleCount; i++) {
			ParticleWiggle wiggle = new ParticleWiggle();
			wiggle.read(in);
			wiggles.add(wiggle);
		}
		
		screenBloomAlphaRate = in.readUByte();
		screenBloomAlphaBase = in.readUByte();
		screenBloomScaleRate = in.readUByte();
		screenBloomScaleBase = in.readUByte();
		
		loopBoxColor = new EffectColor[in.readInt()];
		for (int i = 0; i < loopBoxColor.length; i++) {
			loopBoxColor[i] = new EffectColor();
			loopBoxColor[i].readLE(in);
		}
		loopBoxAlpha = new float[in.readInt()];
		in.readFloats(loopBoxAlpha);
		
		int surfaceCount = in.readInt();
		for (int i = 0; i < surfaceCount; i++) {
			Surface s = new Surface();
			s.read(in);
			surfaces.add(s);
		}
		
		mapBounce = in.readFloat();
		mapRepulseHeight = in.readFloat();
		mapRepulseStrength = in.readFloat();
		mapRepulseScoutDistance = in.readFloat();
		mapRepulseVertical = in.readFloat();
		mapRepulseKillHeight = in.readFloat();
		probabilityDeath = in.readFloat();
		altitudeRange[0] = in.readLEFloat(); 
		altitudeRange[1] = in.readLEFloat();
		
		mapForce.read(in);
		mapEmit.read(in);
		mapEmitColor.read(in);
		
		randomWalk.read(in);
		
		attractorOrigin[0] = in.readLEFloat();
		attractorOrigin[1] = in.readLEFloat();
		attractorOrigin[2] = in.readLEFloat();
		
		attractor.read(in);
		
		int pathPointCount = in.readInt();
		for (int i = 0; i <pathPointCount; i++) {
			ParticlePathPoint pathPoint = new ParticlePathPoint();
			pathPoint.read(in);
			pathPoints.add(pathPoint);
		}
		
		return true;
	}
	
	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(unkFlags);
		return writeMain(out);
	}

	public boolean writeMain(OutputStreamAccessor out) throws IOException {
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
		out.writeFloat(torusWidth);
		
		out.writeInt(rate.length);
		out.writeFloats(rate);
		out.writeFloat(rateLoop);
		out.writeShort(rateCurveCycles);
		out.writeFloat(rateSpeedScale);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		out.writeFloat(sizeVary);
		
		out.writeInt(aspectRatio.length);
		out.writeFloats(aspectRatio);
		out.writeFloat(aspectRatioVary);
		
		out.writeFloat(rotationVary);
		out.writeFloat(rotationOffset);
		out.writeInt(rotate.length);
		out.writeFloats(rotate);
		
		out.writeInt(alpha.length);
		out.writeFloats(alpha);
		out.writeFloat(alphaVary);
		
		out.writeInt(color.length);
		for (EffectColor c : color) c.writeLE(out);
		colorVary.writeLE(out);
		
		texture.write(out);
		
		out.writeUByte(physicsType);
		out.writeUByte(overrideSet);
		out.writeUByte(tileCount[0]);
		out.writeUByte(tileCount[1]);
		out.writeUByte(alignMode);
		
		out.writeUByte(frameStart);
		out.writeByte(frameCount);
		out.writeByte(frameRandom);
		out.writeFloat(frameSpeed);
		
		out.writeLEFloats(directionalForcesSum);
		out.writeFloat(windStrength);
		out.writeFloat(gravityStrength);
		out.writeFloat(radialForce);
		out.writeLEFloats(radialForceLocation);
		out.writeFloat(drag);
		out.writeFloat(velocityStretch);
		out.writeFloat(screwRate);
		
		out.writeInt(wiggles.size());
		for (ParticleWiggle w : wiggles) w.write(out);
		
		out.writeUByte(screenBloomAlphaRate);
		out.writeUByte(screenBloomAlphaBase);
		out.writeUByte(screenBloomScaleRate);
		out.writeUByte(screenBloomScaleBase);
		
		out.writeInt(loopBoxColor.length);
		for (EffectColor c : loopBoxColor) c.writeLE(out);
		out.writeInt(loopBoxAlpha.length);
		out.writeFloats(loopBoxAlpha);
		
		out.writeInt(surfaces.size());
		for (Surface s : surfaces) s.write(out);
		
		out.writeFloat(mapBounce);
		out.writeFloat(mapRepulseHeight);
		out.writeFloat(mapRepulseStrength);
		out.writeFloat(mapRepulseScoutDistance);
		out.writeFloat(mapRepulseVertical);
		out.writeFloat(mapRepulseKillHeight);
		out.writeFloat(probabilityDeath);
		out.writeLEFloats(altitudeRange);
		
		mapForce.write(out);
		mapEmit.write(out);
		mapEmitColor.write(out);
		
		randomWalk.write(out);
		
		out.writeLEFloats(attractorOrigin);
		attractor.write(out);
		
		out.writeInt(pathPoints.size());
		for (ParticlePathPoint p : pathPoints) p.write(out);
		
		return true;
	}
	
	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		ArgScriptCommand c = null;
		
		if ((c = block.getCommand("color")) != null) {
			color = ArgScript.stringsToColors(c.getArguments());
			String arg = null;
			if ((arg = c.getOptionArg("vary")) != null) {
				colorVary.parse(arg);
			}
		}
		if ((c = block.getCommand("alpha")) != null) {
			alpha = ArgScript.stringsToFloats(c.getArguments());
			String arg = null;
			if ((arg = c.getOptionArg("vary")) != null) {
				alphaVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("size")) != null) {
			size = ArgScript.stringsToFloats(c.getArguments());
			String arg = null;
			if ((arg = c.getOptionArg("vary")) != null) {
				sizeVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("aspect")) != null) {
			aspectRatio = ArgScript.stringsToFloats(c.getArguments());
			String arg = null;
			if ((arg = c.getOptionArg("vary")) != null) {
				aspectRatioVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("rotate")) != null) {
			rotate = ArgScript.stringsToFloats(c.getArguments());
			String arg = null;
			if ((arg = c.getOptionArg("vary")) != null) {
				rotationVary = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				rotationOffset = Float.parseFloat(arg);
			}
		}
		parseSource(block);
		parseEmit(block);
		parseForce(block);
		parseWrap(block);
		parseWiggles(commands);
		parseWalk(block);
		if ((c = block.getCommand("stretch")) != null) {
			velocityStretch = Float.parseFloat(c.getSingleArgument());
		}
		parseLife(block);
		parseRate(block);
		parseInject(block);
		parseMantain(block);
		parseMaterial(block);
		parseTexture(block);
		parseModel(block);
		parseFrames(block);
		if ((c = block.getCommand("align")) != null) {
			alignMode = ENUM_ALIGNMENT.getValue(c.getSingleArgument());
		}
		if ((c = block.getCommand("loopBoxColor")) != null) {
			flags |= FLAG_LOOPBOX;
			loopBoxColor = ArgScript.stringsToColors(c.getArguments());
			// Spore parses the flag "orient", but it doesn't use it
		}
		if ((c = block.getCommand("loopBoxAlpha")) != null) {
			flags |= FLAG_LOOPBOX;
			loopBoxAlpha = ArgScript.stringsToFloats(c.getArguments());
			// Spore parses the flag "orient", but it doesn't use it
		}
		for (ArgScriptCommand command : commands) {
			if (command.getKeyword().equals("surface")) {
				if (command.hasFlag("reset")) {
					surfaces.clear();
				}
				Surface s = new Surface();
				s.parse(command, parent);
				surfaces.add(s);
			}
		}
		parseEmitMap(block);
		if ((c = block.getCommand("mapEmitColor")) != null) {
			mapEmitColor.parseSpecial(c.getSingleArgument());
		}
		parseCollideMap(block);
		parseRepelMap(block);
		parseAdvectMap(block);
		parseForceMap(block);
		if ((c = block.getCommand("physics")) != null) {
			physicsType = ENUM_PHYSICS.getValue(c.getSingleArgument());
		}
		
		int pathPointIndex = 0;
		for (ArgScriptCommand command : commands) {
			if (command.getKeyword().equals("path")) {
				ParticlePathPoint p = new ParticlePathPoint();
				p.parse(command, pathPoints, pathPointIndex++);
				pathPoints.add(p);
			}
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
				else if ((args = c.getOptionArgs("ring", 2)) != null)
				{
					float value = Float.parseFloat(args.get(0));
					torusWidth = ArgScript.parseRangedFloat(args.get(1), 0, 1);
					min_x -= value;
					min_y -= value;
					max_x += value;
					max_y += value;
				}
				else if ((args = c.getOptionArgs("torus", 2)) != null)
				{
					float[] list = ArgScript.parseFloatList(args.get(0), 3);
					torusWidth = ArgScript.parseRangedFloat(args.get(1), 0, 1);
					min_x -= list[0];
					min_y -= list[1];
					min_z -= list[2];
					max_x += list[0];
					max_y += list[1];
					max_z += list[2];
				}
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
			
			if (c.hasFlag("scaleParticles")) {
				flags |= SOURCE_SCALEPARTICLES;
			}
			if (c.hasFlag("resetIncoming")) {
				flags |= SOURCE_RESETINCOMING;
			}
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
				|| emitVolumeBBMax[0] != 0 || emitVolumeBBMax[1] != 0 || emitVolumeBBMax[2] != 0
				|| torusWidth != -1) {
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
				if (torusWidth != -1) {
					if (min_z != 0 || max_z != 0) {
						// we use the max because we want the added value (even if it's negative)
						c.putOption(new ArgScriptOption("torus", ArgScript.createFloatList(max_x, max_y, max_z),
								Float.toString(torusWidth)));
					}
					else {
						c.putOption(new ArgScriptOption("ring", Float.toString(max_x),
								Float.toString(torusWidth)));
					}
				}
				else {
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
				}
			}
			if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) {
				c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(offset)));
			}
			
			shouldWrite = true;
		}
		if ((flags & SOURCE_SCALEPARTICLES) == SOURCE_SCALEPARTICLES) {
			c.putFlag("scaleParticles");
			shouldWrite = true;
		}
		if ((flags & SOURCE_RESETINCOMING) == SOURCE_RESETINCOMING) {
			c.putFlag("resetIncoming");
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
			if (c.hasFlag("scaleExisting")) flags |= EMIT_SCALEEXISTING;
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
			block.putOption(o);
		}
		
		if ((flags & EMIT_BASE) == EMIT_BASE) {
			shouldWrite = true;
			c.putFlag("base");
		}
		if ((flags & EMIT_SCALEEXISTING) == EMIT_SCALEEXISTING) {
			shouldWrite = true;
			c.putFlag("scaleExisting");
		}
		
		if (shouldWrite) block.putCommand(c);
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
			// Attractor
			int index = 0;
			boolean hasLocation = false;
			if ((args = c.getOptionArgs("attractor", 3, Integer.MAX_VALUE)) != null) {
				ArgScript.parseFloatList(args.get(index++), attractorOrigin);
				hasLocation = true;
			}
			else {
				args = c.getOptionArgs("presetAttractor", 2, Integer.MAX_VALUE);
			}
			if (args != null) {
				attractor.range = Float.parseFloat(args.get(index++));
				attractor.attractorStrength = new float[args.size() - index];
				for (int i = 0; i < attractor.attractorStrength.length; i++) {
					attractor.attractorStrength[i] = Float.parseFloat(args.get(index++));
				}
				if ((arg = c.getOptionArg("killRange")) != null) {
					attractor.killRange = Float.parseFloat(arg);
				}
				if (hasLocation) {
					flags |= ATTRACTOR_LOCATION;
				}
				else {
					flags &= ~ATTRACTOR_LOCATION;
				}
			}
		} 
	}
	
	private void writeForce(ArgScriptBlock block) {
		if (directionalForcesSum[0] != 0 || directionalForcesSum[1] != 0 || directionalForcesSum[0] != 0 || windStrength != 0 || gravityStrength != 0
				|| drag != 0 || radialForce != 0 || attractor.attractorStrength.length != 0) {
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
			
			if (attractor.attractorStrength.length != 0) {
				ArgScriptOption o = null;
				if ((flags & ATTRACTOR_LOCATION) == ATTRACTOR_LOCATION) {
					o = new ArgScriptOption("attractor", ArgScript.createFloatList(attractorOrigin));
				}
				else {
					o = new ArgScriptOption("presetAttractor");
				}
				o.addArgument(Float.toString(attractor.range));
				for (float f : attractor.attractorStrength) {
					o.addArgument(Float.toString(f));
				}
				if (attractor.killRange != 0) o.addArgument(Float.toString(attractor.killRange));
				c.putOption(o);
			}
			
			block.putCommand(c);
		}
	}
	
	private void parseWrap(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("wrap");
		if (c != null) {
			String arg = null;
			if ((arg = c.getOptionArg("screw")) != null) {
				screwRate = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("spiral")) != null) {
				screwRate = Float.parseFloat(arg);
				flags |= WRAP_SPIRAL;
			}
			List<String> args = null;
			//TODO Spore does this completely differently!
//			if ((args = c.getOptionArgs("wiggle", 4)) != null) {
//				ParticleWiggle wiggle = new ParticleWiggle();
//				
//				wiggle.rateDirection[2] = Float.parseFloat(args.get(0));
//				wiggle.timeRate = Float.parseFloat(args.get(1));
//				float value = Float.parseFloat(args.get(2));
//				float vary = ArgScript.parseRangedFloat(args.get(3), 0, 1);
//				
//				// same as just vary * 16 ?
//				//double as = 2 * Math.PI * vary * (16 / (2 * Math.PI));
//				
//				wiggles.add(wiggle);
//			}
			
			if ((args = c.getOptionArgs("wiggle", 4)) != null) {
				ParticleWiggle wiggle = new ParticleWiggle();
				wiggle.timeRate = Float.parseFloat(args.get(0));
				ArgScript.parseFloatList(args.get(1), wiggle.wiggleDirection);
				if (args.size() > 2) ArgScript.parseFloatList(args.get(2), wiggle.rateDirection);
				
				wiggles.add(wiggle);
			}
			
			if ((args = c.getOptionArgs("wiggleDir", 2, 3)) != null) {
				ParticleWiggle wiggle = new ParticleWiggle();
				wiggle.timeRate = Float.parseFloat(args.get(0));
				ArgScript.parseFloatList(args.get(1), wiggle.wiggleDirection);
				if (args.size() > 2) ArgScript.parseFloatList(args.get(2), wiggle.rateDirection);
				
				wiggles.add(wiggle);
			}
			
			if ((args = c.getOptionArgs("bloomAlpha", 2)) != null) {
				screenBloomAlphaBase = 
						(int) Math.min(Math.max(Float.parseFloat(args.get(0)), 0) * 255, 255);
				// this goes from 0 to 16
				screenBloomAlphaRate = 
						(int) Math.min(Math.max(Float.parseFloat(args.get(1)) * 0.0625, 0) * 255, 255);
			}
			if ((args = c.getOptionArgs("bloomSize", 2)) != null) {
				screenBloomScaleBase = 
						(int) Math.min(Math.max(Float.parseFloat(args.get(0)), 0) * 255, 255);
				screenBloomScaleRate = 
						(int) Math.min(Math.max(Float.parseFloat(args.get(1)) * 0.0625, 0) * 255, 255);
			}
		}
	}
	
	//TODO Spore doesn't do it this way!
	private void parseWiggles(Collection<ArgScriptCommand> commands) throws ArgScriptException {
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("wiggle")) {
				List<String> args = c.getArguments(2, 3);
				
				ParticleWiggle wiggle = new ParticleWiggle();
				wiggle.timeRate = Float.parseFloat(args.get(0));
				ArgScript.parseFloatList(args.get(1), wiggle.wiggleDirection);
				if (args.size() > 2) ArgScript.parseFloatList(args.get(2), wiggle.rateDirection);
				
				wiggles.add(wiggle);
			}
		}
	}
	private void writeWiggles(ArgScriptBlock block) {
		for (ParticleWiggle w : wiggles) {
			ArgScriptCommand c = new ArgScriptCommand("wiggleDir", Float.toString(w.timeRate), ArgScript.createFloatList(w.wiggleDirection));
			if (w.rateDirection[0] != 0 || w.rateDirection[1] != 0 || w.rateDirection[2] != 0) {
				c.addArgument(ArgScript.createFloatList(w.rateDirection));
			}
			block.putCommand(c);
		}
	}
	
	private void writeWrap(ArgScriptBlock block) {
		ArgScriptCommand c = new ArgScriptCommand("wrap");
		boolean shouldWrite = false;
		
		if (screwRate != 0) {
			if ((flags & WRAP_SPIRAL) == WRAP_SPIRAL) {
				c.putOption(new ArgScriptOption("spiral", Float.toString(screwRate)));
			}
			else {
				c.putOption(new ArgScriptOption("screw", Float.toString(screwRate)));
			}
			shouldWrite = true;
		}
//		if (wiggles.size() > 0) {
//			ParticleWiggle w = wiggles.get(0);
//			ArgScriptOption o = new ArgScriptOption("wiggle", Float.toString(w.timeRate), ArgScript.createFloatList(w.wiggleDirection));
//			if (w.rateDirection[0] != 0 || w.rateDirection[1] != 0 || w.rateDirection[2] != 0) {
//				o.addArgument(ArgScript.createFloatList(w.rateDirection));
//			}
//			c.putOption(o);
//			shouldWrite = true;
//		}
//		if (wiggles.size() > 1) {
//			ParticleWiggle w = wiggles.get(1);
//			ArgScriptOption o = new ArgScriptOption("wiggleDir", Float.toString(w.timeRate), ArgScript.createFloatList(w.wiggleDirection));
//			if (w.rateDirection[0] != 0 || w.rateDirection[1] != 0 || w.rateDirection[2] != 0) {
//				o.addArgument(ArgScript.createFloatList(w.rateDirection));
//			}
//			c.putOption(o);
//			shouldWrite = true;
//		}
//		if (wiggles.size() > 2) {
//			throw new UnsupportedOperationException(KEYWORD + ": Unsupported number of wiggles.");
//		}
		
		if (screenBloomAlphaRate != 0 || screenBloomAlphaBase != 255) {
			c.putOption(new ArgScriptOption("bloomAlpha", Float.toString(screenBloomAlphaBase / 255.0f), 
					Float.toString((screenBloomAlphaRate / 255.0f) / 0.0625f)));
			shouldWrite = true;
		}
		if (screenBloomScaleRate != 0 || screenBloomScaleBase != 255) {
			c.putOption(new ArgScriptOption("bloomSize", Float.toString(screenBloomScaleBase / 255.0f), 
					Float.toString((screenBloomScaleRate / 255.0f) / 0.0625f)));
			shouldWrite = true;
		}
		
		if (shouldWrite) block.putCommand(c);
	}
	
	private void parseWalk(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("directedWalk");
		boolean directedWalk = c != null;
		if (c == null) {
			c = block.getCommand("randomWalk");
			directedWalk = false;
		}
		if (c != null) {
			List<String> args = c.getArguments();
			randomWalk.turnOffsetCurve = new float[args.size()];
			for (int i = 0; i < randomWalk.turnOffsetCurve.length; i++) {
				// randomWalk.turnOffsetCurve[i] = ArgScript.parseRangedFloat(args.get(i), -1, 1);
				
				randomWalk.turnOffsetCurve[i] = Float.parseFloat(args.get(i));
			}
			flags |= FLAG_RANDOMWALK;
			
			if ((args = c.getOptionArgs("delay", 1, 2)) != null) {
				float value = Float.parseFloat(args.get(0));
				float vary = args.size() > 1 ? Float.parseFloat(args.get(0)) : 0;
				randomWalk.time[0] = value - vary;
				randomWalk.time[1] = value + vary;
			}
			if ((args = c.getOptionArgs("strength", 1, 2)) != null) {
				float value = Float.parseFloat(args.get(0));
				float vary = args.size() > 1 ? Float.parseFloat(args.get(0)) : 0;
				randomWalk.strength[0] = value - vary;
				randomWalk.strength[1] = value + vary;
			}
			if ((args = c.getOptionArgs(directedWalk ? "randomTurn" : "turn", 1, 2)) != null) {
				randomWalk.turnRange = ArgScript.parseRangedFloat(args.get(0), 0, 1);
				randomWalk.turnOffset = args.size() > 1 ? ArgScript.parseRangedFloat(args.get(1), 0, 1) : 0;
			}
			randomWalk.loopType = 2;
			if (c.hasFlag("sustain")) randomWalk.loopType = 1;
			if (c.hasFlag("loop")) randomWalk.loopType = 0;
			String arg = c.getOptionArg("mix");
			if (arg != null) {
				randomWalk.mix = ArgScript.parseRangedFloat(arg, 0, 1);
			}
			if (c.hasFlag("wait")) flags |= RANDOMWALK_WAIT;
		}
	}
	
	private void writeWalk(ArgScriptBlock block) {
		if ((flags & FLAG_RANDOMWALK) == FLAG_RANDOMWALK) {
			ArgScriptCommand c = new ArgScriptCommand("randomWalk", ArgScript.floatsToStrings(randomWalk.turnOffsetCurve));
			{
				float vary = (randomWalk.time[1] - randomWalk.time[0]) / 2;
				float value = randomWalk.time[0] + vary;
				ArgScriptOption o = new ArgScriptOption("delay", Float.toString(value));
				if (vary != 0) c.addArgument(Float.toString(vary));
				c.putOption(o);
			}
			{
				float vary = (randomWalk.strength[1] - randomWalk.strength[0]) / 2;
				float value = randomWalk.strength[0] + vary;
				ArgScriptOption o = new ArgScriptOption("strength", Float.toString(value));
				if (vary != 0) c.addArgument(Float.toString(vary));
				c.putOption(o);
			}
			if (randomWalk.turnRange != 0 || randomWalk.turnOffset != 0) {
				ArgScriptOption o = new ArgScriptOption("turn", Float.toString(randomWalk.turnRange));
				if (randomWalk.turnOffset != 0) o.addArgument(Float.toString(randomWalk.turnOffset));
				c.putOption(o);
			}
			if (randomWalk.loopType == 1) c.putFlag("sustain");
			if (randomWalk.loopType == 0) c.putFlag("loop");
			if (randomWalk.mix != 0) c.putOption(new ArgScriptOption("mix", Float.toString(randomWalk.mix)));
			if ((flags & RANDOMWALK_WAIT) == RANDOMWALK_WAIT) c.putFlag("wait");
			
			block.putCommand(c);
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
			
			if (c.hasFlag("propagateAlways")) unkFlags |= LIFE_PROPAGATEALWAYS;
			if (c.hasFlag("propagateIfKilled")) unkFlags |= LIFE_PROPAGATEIFKILLED;
		}
	}
	
	private void writeLife(ArgScriptBlock block) {
		ArgScriptCommand c = new ArgScriptCommand("life");
		
		float vary = (life[1] - life[0]) / 2;
		float value = life[0] + vary;
		c.addArgument(Float.toString(value));
		if (vary != 0) c.addArgument(Float.toString(vary));
		
		if (prerollTime != 0.5f && prerollTime != value) {
			c.putOption(new ArgScriptOption("preroll", Float.toString(prerollTime)));
		}
		
		if ((unkFlags & LIFE_PROPAGATEALWAYS) == LIFE_PROPAGATEALWAYS) c.putFlag("propagateAlways");
		if ((unkFlags & LIFE_PROPAGATEIFKILLED) == LIFE_PROPAGATEIFKILLED) c.putFlag("propagateIfKilled");
		
		block.putCommand(c);
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
		else if ((args = c.getOptionArgs("hold", 1, 2)) != null) {
			flags |= FLAG_HOLD;
			rateLoop = Float.parseFloat(args.get(0));
			rateCurveCycles = args.size() > 1 ? Hasher.decodeShort(args.get(1)) : 1;
		}
		else if ((args = c.getOptionArgs("kill", 1, 2)) != null) {
			flags |= FLAG_KILL;
			unkFlags |= LIFE_PROPAGATEALWAYS;
			rateLoop = args.size() > 0 ? Float.parseFloat(args.get(0)) : 0.1f;
			rateCurveCycles = 1;
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
			if ((args = c.getOptionArgs("hold", 1, 2)) != null) {
				flags |= FLAG_HOLD;
				rateLoop = Float.parseFloat(args.get(0));
				rateCurveCycles = args.size() > 1 ? Hasher.decodeShort(args.get(1)) : 1;
			}
			else if ((args = c.getOptionArgs("kill", 1, 2)) != null) {
				flags |= FLAG_KILL;
				unkFlags |= LIFE_PROPAGATEALWAYS;
				rateLoop = Float.parseFloat(args.get(0));
				rateCurveCycles = 1;
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
			if (emitDelay[0] != -1 || emitDelay[1] != -1) {
				ArgScriptOption o = new ArgScriptOption("delay", Float.toString(emitDelay[0]));
				if (emitDelay[1] != emitDelay[0]) o.addArgument(Float.toString(emitDelay[1]));
				c.putOption(o);
			}
			if ((flags & FLAG_HOLD) == FLAG_HOLD) {
				ArgScriptOption o = new ArgScriptOption("hold", Float.toString(rateLoop));
				if (rateCurveCycles != 1) o.addArgument(Integer.toString(rateCurveCycles));
				c.putOption(o);
			} 
			else if ((flags & FLAG_KILL) == FLAG_KILL) {
				ArgScriptOption o = new ArgScriptOption("hold", Float.toString(rateLoop));
				if (rateCurveCycles != 1) o.addArgument(Integer.toString(rateCurveCycles));
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
		else if ((flags & FLAG_HOLD) == FLAG_HOLD) {
			ArgScriptOption o = new ArgScriptOption("hold", Float.toString(rateLoop));
			if (rateCurveCycles != 1) o.addArgument(Integer.toString(rateCurveCycles));
			c.putOption(o);
		}
		else if ((flags & FLAG_KILL) == FLAG_KILL) {
			ArgScriptOption o = new ArgScriptOption("kill", Float.toString(rateLoop));
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
	
	private void parseMaterial(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("material");
		if (c != null) {
			flags &= ~FLAG_MODEL;
			texture.setDrawMode(0xF);
			texture.parse(c);
			String arg = c.getOptionArg("overrideSet");
			if (arg != null) {
				overrideSet = Hasher.decodeByte(arg);
			}
		}
	}
	
	private void parseTexture(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("texture");
		if (c != null) {
			flags &= ~FLAG_MODEL;
			texture.parse(c);
			List<String> args = c.getOptionArgs("tile", 1, 2);
			if (args != null) {
				tileCount[0] = Hasher.decodeByte(args.get(0));
				tileCount[1] = args.size() > 1 ? Hasher.decodeByte(args.get(1)) : 1;
			}
			
			String arg = c.getOptionArg("overrideSet");
			if (arg != null) {
				overrideSet = Hasher.decodeByte(arg);
			}
			
			if (c.hasFlag("acceptComposite")) flags |= FLAG_ACCEPTCOMPOSITE;
		}
	}
	
	private void parseModel(ArgScriptBlock block) throws IOException, ArgScriptException {
		ArgScriptCommand c = block.getCommand("model");
		if (c != null) {
			flags |= FLAG_MODEL;
			texture.setDrawMode(0);
			List<String> args = c.getArguments(0, 1);
			if (args.size() > 0) {
				texture.setResource(new ResourceID(args.get(0)));
			}
			String arg = c.getOptionArg("material");
			if (arg != null) {
				texture.setResource2(new ResourceID(arg));
				texture.setDrawMode(TextureSlot.DRAWMODE_NONE);
			}
			if ((arg = c.getOptionArg("overrideSet")) != null) {
				overrideSet = Hasher.decodeByte(arg);
			}
			texture.setDrawFlags(texture.getDrawFlags() | 8);
			texture.parse(c);
		}
	}
	
	private void writeResource(ArgScriptBlock block) {
		ArgScriptCommand c = null;
		if ((flags & FLAG_MODEL) == FLAG_MODEL) {
			c = new ArgScriptCommand("model");
			if (!texture.getResource().isDefault()) {
				c.addArgument(texture.getResource().toString());
			}
			if (!texture.getResource2().isDefault()) {
				c.putOption(new ArgScriptOption("material", texture.getResource2().toString()));
			}
		}
		else {
			if (texture.getDrawMode() == TextureSlot.DRAWMODE_NONE) {
				c = new ArgScriptCommand("material", texture.getResource().toString());
			}
			else {
				c = new ArgScriptCommand("texture", texture.getResource().toString());
				if (tileCount[0] != 1 || tileCount[1] != 1) {
					ArgScriptOption o = new ArgScriptOption("tile", Integer.toString(tileCount[0]));
					if (tileCount[1] != 1) {
						o.addArgument(Integer.toString(tileCount[1]));
					}
					c.putOption(o);
				}
			}
		}
		
		if (c != null) {
			if (overrideSet != 0) {
				c.putOption(new ArgScriptOption("overrideSet", Integer.toString(overrideSet)));
			}
			texture.toCommand(c);
			block.putCommand(c);
		}
	}
	
	private void parseFrames(ArgScriptBlock block) throws ArgScriptException {
		ArgScriptCommand c = block.getCommand("frames");
		if (c != null) {
			String arg = null;
			if ((arg = c.getOptionArg("speed")) != null) {
				frameSpeed = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("count")) != null) {
				frameCount = Hasher.decodeByte(arg);
			}
			if ((arg = c.getOptionArg("start")) != null) {
				frameStart = Hasher.decodeByte(arg);
			}
			if ((arg = c.getOptionArg("random")) != null) {
				frameRandom = Hasher.decodeByte(arg);
			}
		}
	}
	
	private void writeFrames(ArgScriptBlock block) {
		if (frameCount != 0) {
			ArgScriptCommand c = new ArgScriptCommand("frames");
			c.putOption(new ArgScriptOption("speed", Float.toString(frameSpeed)));
			c.putOption(new ArgScriptOption("count", Integer.toString(frameCount)));
			if (frameStart != 0) c.putOption(new ArgScriptOption("start", Integer.toString(frameStart)));
			if (frameRandom != 0) c.putOption(new ArgScriptOption("random", Integer.toString(frameRandom)));
			block.putCommand(c);
		}
	}
	
	private void parseEmitMap(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("mapEmit");
		if (c != null) {
			mapEmit.parseSpecial(c.getSingleArgument());
			String arg = null;
			List<String> args = null;
			if (c.hasFlag("pinToSurface")) flags |= EMITMAP_PINTOSURFACE;
			if ((arg = c.getOptionArg("belowHeight")) != null) {
				flags |= EMITMAP_HEIGHT;
				altitudeRange[1] = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("aboveHeight")) != null) {
				flags |= EMITMAP_HEIGHT;
				altitudeRange[0] = Float.parseFloat(arg);
			}
			if ((args = c.getOptionArgs("heightRange", 2)) != null) {
				flags |= EMITMAP_HEIGHT;
				altitudeRange[0] = Float.parseFloat(args.get(0));
				altitudeRange[1] = Float.parseFloat(args.get(1));
			}
			if (c.hasFlag("density")) flags |= EMITMAP_DENSITY;
		}
	}
	
	private void writeEmitMap(ArgScriptBlock block) {
		if (!mapEmit.isDefault()) {
			ArgScriptCommand c = new ArgScriptCommand("mapEmit", mapEmit.toString());
			if ((flags & EMITMAP_PINTOSURFACE) == EMITMAP_PINTOSURFACE) c.putFlag("pinToSurface");
			if ((flags & EMITMAP_HEIGHT) == EMITMAP_HEIGHT) {
				boolean hasOption = false;
				if (altitudeRange[1] != 10000) {
					c.putOption(new ArgScriptOption("belowHeight", Float.toString(altitudeRange[1])));
					hasOption = true;
				}
				if (altitudeRange[0] != -10000) {
					c.putOption(new ArgScriptOption("aboveHeight", Float.toString(altitudeRange[0])));
					hasOption = true;
				}
				if (!hasOption) {
					c.putOption(new ArgScriptOption("heightRange", Float.toString(altitudeRange[0]), Float.toString(altitudeRange[1])));
				}
			}
			if ((flags & EMITMAP_DENSITY) == EMITMAP_DENSITY) c.putFlag("density");
			
			block.putCommand(c);
		}
	}
	
	private void parseCollideMap(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("mapCollide");
		if (c != null) {
			List<String> args = c.getArguments(0, 1);
			if (args.size() > 0) {
				mapForce.parseSpecial(args.get(0));
			} else {
				mapForce.setGroupID(0);
				mapForce.setNameID(0);
			}
			flags |= FLAG_COLLIDEMAP;
			if (c.hasFlag("pinToMap")) {
				flags |= COLLIDE_PINTOMAP;
				mapBounce = 0;
			} else {
				String arg = c.getOptionArg("bounce");
				if (arg != null) {
					mapBounce = Float.parseFloat(arg);
				}
			}
			if (c.hasFlag("killOutsideMap")) {
				flags |= FLAG_KILLOUTSIDEMAP;
			}
			String arg = c.getOptionArg("death");
			if (arg != null) {
				probabilityDeath = ArgScript.parseRangedFloat(arg, 0, 1);
			}
		}
	}
	
	private void writeCollideMap(ArgScriptBlock block) {
		if ((flags & FLAG_COLLIDEMAP) == FLAG_COLLIDEMAP) {
			ArgScriptCommand c = new ArgScriptCommand("mapCollide");
			if (!mapForce.isZero()) c.addArgument(mapForce.toString());
			
			if ((flags & COLLIDE_PINTOMAP) == COLLIDE_PINTOMAP) {
				c.putFlag("pinToMap");
			} else {
				if (mapBounce != 1.0f) {
					c.putOption(new ArgScriptOption("bounce", Float.toString(mapBounce)));
				}
			}
			if ((flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP) {
				c.putFlag("killOutsideMap");
			}
			if (probabilityDeath != 0) {
				c.putOption(new ArgScriptOption("death", Float.toString(probabilityDeath)));
			}
			block.putCommand(c);
		}
	}
	
	private void parseRepelMap(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("mapRepel");
		if (c != null) {
			List<String> args = c.getArguments(2, 3);
			int ind = 0;
			if (args.size() == 2) {
				mapForce.setGroupID(0);
				mapForce.setNameID(0);
				mapRepulseHeight = Float.parseFloat(args.get(ind++));
			} else {
				mapForce.parseSpecial(args.get(ind++));
				mapRepulseHeight = Float.parseFloat(args.get(ind++));
			}
			mapRepulseStrength = Float.parseFloat(args.get(ind));
			flags |= FLAG_COLLIDEMAP;
			flags |= FLAG_REPULSEMAP;
			String arg = null;
			if ((arg = c.getOptionArg("scout")) != null) {
				mapRepulseScoutDistance = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("vertical")) != null) {
				mapRepulseVertical = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("killHeight")) != null) {
				mapRepulseKillHeight = Float.parseFloat(arg);
			}
			if (c.hasFlag("killOutsideMap")) {
				flags |= FLAG_KILLOUTSIDEMAP;
			}
		}
	}
	
	private void writeRepelMap(ArgScriptBlock block) {
		if ((flags & FLAG_REPULSEMAP) == FLAG_REPULSEMAP) {
			ArgScriptCommand c = new ArgScriptCommand("mapRepel");
			if (!mapForce.isZero()) c.addArgument(mapForce.toString());
			c.addArgument(Float.toString(mapRepulseHeight));
			c.addArgument(Float.toString(mapRepulseStrength));
			if (mapRepulseScoutDistance != 0) {
				c.putOption(new ArgScriptOption("scout", Float.toString(mapRepulseScoutDistance)));
			}
			if (mapRepulseVertical != 0) {
				c.putOption(new ArgScriptOption("vertical", Float.toString(mapRepulseVertical)));
			}
			if (mapRepulseKillHeight != -1000000000.0f) {
				c.putOption(new ArgScriptOption("killHeight", Float.toString(mapRepulseKillHeight)));
			}
			if ((flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP) {
				c.putFlag("killOutsideMap");
			}
			block.putCommand(c);
		}
	}
	
	private void parseAdvectMap(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("mapAdvect");
		if (c != null) {
			mapForce.parseSpecial(c.getSingleArgument());
			flags |= FLAG_COLLIDEMAP;
			flags |= FLAG_ADVECTMAP;
			String arg = c.getOptionArg("strength");
			if (arg != null) {
				mapRepulseStrength = Float.parseFloat(arg);
			}
			if (c.hasFlag("killOutsideMap")) flags |= FLAG_KILLOUTSIDEMAP;
		}
	}
	
	private void writeAdvectMap(ArgScriptBlock block) {
		if ((flags & FLAG_ADVECTMAP) == FLAG_ADVECTMAP) {
			ArgScriptCommand c = new ArgScriptCommand("mapAdvect", mapForce.toString());
			if (mapRepulseStrength != 0) c.putOption(new ArgScriptOption("strength", Float.toString(mapRepulseStrength)));
			if ((flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP) c.putFlag("killOutsideMap");
			block.putCommand(c);
		}
	}
	
	private void parseForceMap(ArgScriptBlock block) throws ArgScriptException, IOException {
		ArgScriptCommand c = block.getCommand("mapForce");
		if (c != null) {
			mapForce.parseSpecial(c.getSingleArgument());
			flags |= FLAG_COLLIDEMAP;
			flags |= FLAG_FORCEMAP;
			String arg = c.getOptionArg("strength");
			if (arg != null) {
				mapRepulseStrength = Float.parseFloat(arg);
			}
			if (c.hasFlag("killOutsideMap")) flags |= FLAG_KILLOUTSIDEMAP;
		}
	}
	
	private void writeForceMap(ArgScriptBlock block) {
		if ((flags & FLAG_FORCEMAP) == FLAG_FORCEMAP) {
			ArgScriptCommand c = new ArgScriptCommand("mapForce", mapForce.toString());
			if (mapRepulseStrength != 0) c.putOption(new ArgScriptOption("strength", Float.toString(mapRepulseStrength)));
			if ((flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP) c.putFlag("killOutsideMap");
			block.putCommand(c);
		}
	}
	
	@Override
	public boolean toBlock(ArgScriptBlock block) {
		{
			ArgScriptCommand c = new ArgScriptCommand("color", ArgScript.colorsToStrings(color));
			if (!colorVary.equals(EffectColor.BLACK)) c.putOption(new ArgScriptOption("vary", colorVary.toString()));
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha));
			if (alphaVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(alphaVary)));
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("size", ArgScript.floatsToStrings(size));
			if (sizeVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(sizeVary)));
			block.putCommand(c);
		}
		if (aspectRatio.length != 0 && (aspectRatio.length != 1 || aspectRatio[0] != 1.0f)) {
			ArgScriptCommand c = new ArgScriptCommand("aspect", ArgScript.floatsToStrings(aspectRatio));
			if (aspectRatioVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(aspectRatioVary)));
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("rotate", ArgScript.floatsToStrings(rotate));
			if (rotationVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(rotationVary)));
			if (rotationOffset != 0) c.putOption(new ArgScriptOption("offset", Float.toString(rotationOffset)));
			block.putCommand(c);
		}
		writeSource(block);
		writeEmit(block);
		writeForce(block);
		writeWrap(block);
		writeWiggles(block);
		writeWalk(block);
		if (velocityStretch != 0) {
			block.putCommand(new ArgScriptCommand("stretch", Float.toString(velocityStretch)));
		}
		writeLife(block);
		writeRate(block);
		writeResource(block);
		writeFrames(block);
		if (alignMode != 0) block.putCommand(new ArgScriptCommand("align", ENUM_ALIGNMENT.getKey(alignMode)));
		if ((flags & FLAG_LOOPBOX) == FLAG_LOOPBOX) {
			if (loopBoxColor.length > 0) {
				block.putCommand(new ArgScriptCommand("loopBoxColor", ArgScript.colorsToStrings(loopBoxColor)));
			}
			if (loopBoxAlpha.length > 0) {
				block.putCommand(new ArgScriptCommand("loopBoxAlpha", ArgScript.floatsToStrings(loopBoxAlpha)));
			}
		}
		for (Surface s : surfaces) {
			ArgScriptCommand c = new ArgScriptCommand("surface");
			block.putCommand(s.toCommand(c, parent));
		}
		writeEmitMap(block);
		if (!mapEmitColor.isDefault()) {
			block.putCommand(new ArgScriptCommand("mapEmitColor", mapEmitColor.toString()));
		}
		writeCollideMap(block);
		writeRepelMap(block);
		writeAdvectMap(block);
		writeForceMap(block);
		if (physicsType != 0) {
			block.putCommand(new ArgScriptCommand("physics", ENUM_PHYSICS.getKey(physicsType)));
		}
		for (ParticlePathPoint pathPoint : pathPoints) {
			block.putCommand(pathPoint.toCommand());
		}
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline particle effect is not supported.");
	}
	
	
	@Override
	public Effect[] getEffects() {
		List<Effect> effects = new ArrayList<Effect>();
		for (Surface s : surfaces) {
			int collisionEffectIndex = s.getCollisionEffectIndex();
			int deathEffectIndex = s.getDeathEffectIndex();
			
			if (collisionEffectIndex != -1) {
				if ((collisionEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					effects.add(new ImportedEffect(parent.getImports().get(collisionEffectIndex & ~EffectMain.IMPORT_MASK)));
				}
				else {
					effects.add(EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, collisionEffectIndex));
				}
			}
			if (deathEffectIndex != -1) {
				if ((deathEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					effects.add(new ImportedEffect(parent.getImports().get(deathEffectIndex & ~EffectMain.IMPORT_MASK)));
				}
				else {
					effects.add(EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, deathEffectIndex));
				}
			}
			effects.add(s.getSurfaceMap(parent));
		}
		
		effects.add(parent.getResource(MapResource.MASKED_TYPE, mapEmitColor));
		effects.add(parent.getResource(MapResource.MASKED_TYPE, mapForce));
		effects.add(parent.getResource(MapResource.MASKED_TYPE, mapEmit));
		
		if (texture.getDrawMode() == TextureSlot.DRAWMODE_NONE) {
			if ((flags & FLAG_MODEL) == FLAG_MODEL) {
				effects.add(parent.getResource(MaterialResource.MASKED_TYPE, texture.getResource2()));
			} else {
				effects.add(parent.getResource(MaterialResource.MASKED_TYPE, texture.getResource()));
			}
		}
		
		return (Effect[]) effects.toArray(new Effect[effects.size()]);
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		Integer index = baseIndices.get(VisualEffect.TYPE);
		if (index != null && index != -1) {
			for (Surface s : surfaces) {
				s.fixEffectIndices(index);
			}
		}
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"camera", "ground", "source", "dirX", "dirY", "dirZ", "zPole", "sunPole", "cameraLocation",
			"user1", "user2", "user3", "user4", "normal"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"vary", "offset", "reset", "point", "square", "quad", "cube", "box", "circle", "circle", "sphere",
			"ellipse", "ellipsoid", "ring", "torus", "scaleParticles", "resetIncoming", "speed", "dir", "base",
			"scaleExisting", "gravity", "wind", "worldWind", "worldGravity", "bomb", "drag", "attractor", 
			"presetAttractor", "killRange", "screw", "spiral", "wiggle", "wiggleDir", "bloomAlpha",
			"bloomSize", "delay", "strength", "randomTurn", "turn", "sustain", "loop", "wait", "mix",
			"preroll", "propagateAlways", "propagateIfKilled", "loop", "single", "sustain", "hold", "kill",
			"sizeScale", "areaScale", "volumeScale", "speedScale", "delay", "trigger", "overrideSet",
			"material", "tile", "speed", "count", "start", "random", "frames", "pinToSurface", "aboveHeight",
			"belowHeight", "heightRange", "density", "pinToMap", "bounce", "killOutsideMap", "death", "scout",
			"vertical", "killHeight", "strength", 
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"color", "alpha", "size", "aspect", "rotate", "source", "emit", "force", "wrap", "directedWalk",
			"randomWalk", "stretch", "life", "rate", "inject", "mantain", "material", "texture", "model", "frames",
			"align", "loopBoxColor", "loopBoxAlpha", "surface", "mapEmit", "mapEmitColor", "mapCollide", 
			"mapRepel", "mapAdvect", "mapForce", "physics", "path"
		};
	}

}
