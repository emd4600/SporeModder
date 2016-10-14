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

public class CameraEffect extends EffectComponent {
	
	public static final int TYPE = 0x07;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "camera";
	
//TODO
//	camera CaptureCubeTest
//    life 2 -sustain
//    cubemapSnapshot -onStart -res 1024 #-fromEffect
//	end
//	camera shoppingCrossFadeCamera
//	  life 1.5 -sustain
//	  snapshot -onStart 
//	end
//	camera yoinking_camera
//	   control -target -orient
//	   heading .25
//	    pitch .01 -.01 .01 -.01 .01 -.01 .01 -.01 .01 -.01 .01
//	   roll 0 .1 0
//	   life 20 # how long the camera runs. Should be synched up to the life of the metaparticle below.
//	         # the curves for heading pitch and roll are played over life.
//	   nearClip .1
//	   farClip 10000
//	end

	private int flags;  // control -target -orient  ?
	private int viewFlags;  //short
	private float lifeTime;
	
	private float[] yaw = new float[0];  // heading 
	private float[] pitch = new float[0];
	private float[] roll = new float[0];
	private float[] distance = new float[0];
	private float[] fov = new float[0];
	private float[] nearClip = new float[0];
	private float[] farClip = new float[0];
	
	private final ResourceID cameraId = new ResourceID();
	private int cubemapResource;  //short
	
	public CameraEffect(int type, int version) {
		super(type, version);
	}
	
	public CameraEffect(CameraEffect effect) {
		super(effect);

		flags = effect.flags;
		viewFlags = effect.viewFlags;
		lifeTime = effect.lifeTime;
		
		yaw = new float[effect.yaw.length];
		for (int i = 0; i < yaw.length; i++) yaw[i] = effect.yaw[i];
		
		pitch = new float[effect.pitch.length];
		for (int i = 0; i < pitch.length; i++) pitch[i] = effect.pitch[i];
		
		roll = new float[effect.roll.length];
		for (int i = 0; i < roll.length; i++) roll[i] = effect.roll[i];
		
		distance = new float[effect.distance.length];
		for (int i = 0; i < distance.length; i++) distance[i] = effect.distance[i];
		
		fov = new float[effect.fov.length];
		for (int i = 0; i < fov.length; i++) fov[i] = effect.fov[i];
		
		nearClip = new float[effect.nearClip.length];
		for (int i = 0; i < nearClip.length; i++) nearClip[i] = effect.nearClip[i];
		
		farClip = new float[effect.farClip.length];
		for (int i = 0; i < farClip.length; i++) farClip[i] = effect.farClip[i];
		
		cameraId.copy(effect.cameraId);
		cubemapResource = effect.cubemapResource;
	}
	
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		viewFlags = in.readShort();
		lifeTime = in.readFloat();
		
		yaw = new float[in.readInt()];
		in.readFloats(yaw);
		
		pitch = new float[in.readInt()];
		in.readFloats(pitch);
		
		roll = new float[in.readInt()];
		in.readFloats(roll);
		
		distance = new float[in.readInt()];
		in.readFloats(distance);
		
		fov = new float[in.readInt()];
		in.readFloats(fov);
		
		nearClip = new float[in.readInt()];
		in.readFloats(nearClip);
		
		farClip = new float[in.readInt()];
		in.readFloats(farClip);
		
		cameraId.read(in);
		cubemapResource = in.readShort();
		
		return true;
	}
	
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeShort(viewFlags);
		out.writeFloat(lifeTime);
		
		out.writeInt(yaw.length);
		out.writeFloats(yaw);
		
		out.writeInt(pitch.length);
		out.writeFloats(pitch);
		
		out.writeInt(roll.length);
		out.writeFloats(roll);
		
		out.writeInt(distance.length);
		out.writeFloats(distance);
		
		out.writeInt(fov.length);
		out.writeFloats(fov);
		
		out.writeInt(nearClip.length);
		out.writeFloats(nearClip);
		
		out.writeInt(farClip.length);
		out.writeFloats(farClip);
		
		cameraId.write(out);
		out.writeShort(cubemapResource);
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		
		if (flags != 0 || viewFlags != 0) {
			ArgScriptCommand cFlags = new ArgScriptCommand("flags", Hasher.hashToHex(flags, "0x"));
			if (viewFlags != 0) {
				cFlags.putOption(new ArgScriptOption("view", Integer.toString(viewFlags)));
			}
			block.putCommand(cFlags);
		}
		
		block.putCommand(new ArgScriptCommand("life", Float.toString(lifeTime)));
		
		if (yaw.length != 0) block.putCommand(new ArgScriptCommand("yaw", ArgScript.floatsToStrings(yaw)));
		if (pitch.length != 0) block.putCommand(new ArgScriptCommand("pitch", ArgScript.floatsToStrings(pitch)));
		if (roll.length != 0) block.putCommand(new ArgScriptCommand("roll", ArgScript.floatsToStrings(roll)));
		if (distance.length != 0) block.putCommand(new ArgScriptCommand("distance", ArgScript.floatsToStrings(distance)));
		if (fov.length != 0) block.putCommand(new ArgScriptCommand("fov", ArgScript.floatsToStrings(fov)));
		if (nearClip.length != 0) block.putCommand(new ArgScriptCommand("nearClip", ArgScript.floatsToStrings(nearClip)));
		if (farClip.length != 0) block.putCommand(new ArgScriptCommand("farClip", ArgScript.floatsToStrings(farClip)));
		
		if (cameraId != null && !cameraId.isDefault()) block.putCommand(new ArgScriptCommand("cameraId", cameraId.toString()));
		if (cubemapResource != 0) block.putCommand(new ArgScriptCommand("cubemapResource", Integer.toString(cubemapResource)));
		
		return true;
	}
	
	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		
		{ 
			ArgScriptCommand c = block.getCommand("flags"); 
			if (c != null) {
				flags = Hasher.decodeInt(c.getSingleArgument());
				ArgScriptOption o = c.getOption("view");
				if (o != null) {
					viewFlags = Hasher.decodeInt(o.getSingleArgument());
				}
			}
		}
		{ ArgScriptCommand c = block.getCommand("life"); if (c != null) lifeTime = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("yaw"); if (c != null) yaw = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("pitch"); if (c != null) pitch = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("roll"); if (c != null) roll = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("distance"); if (c != null) distance = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("fov"); if (c != null) fov = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("nearClip"); if (c != null) nearClip = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("farClip"); if (c != null) farClip = ArgScript.stringsToFloats(c.getArguments()); }
		{ ArgScriptCommand c = block.getCommand("cameraId"); if (c != null) cameraId.parse(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("cubemapResource"); if (c != null) cubemapResource = Integer.parseInt(c.getSingleArgument()); }


		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline camera effect is not supported.");
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
			"view"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"flags", "life", "yaw", "pitch", "roll", "distance", "fov", "nearClip",
			"farClip", "cameraId", "cubemapResource"
		};
	}
}
