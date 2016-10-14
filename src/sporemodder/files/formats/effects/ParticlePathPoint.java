package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class ParticlePathPoint {
	private final float[] position = new float[3];
	private final float[] velocity = new float[3];
	private float time;
	
	public ParticlePathPoint() {}
	
	public ParticlePathPoint(ParticlePathPoint other) {
		position[0] = other.position[0];
		position[1] = other.position[1];
		position[2] = other.position[2];
		velocity[0] = other.velocity[0];
		velocity[1] = other.velocity[1];
		velocity[2] = other.velocity[2];
		time = other.time;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		position[0] = in.readLEFloat();
		position[1] = in.readLEFloat();
		position[2] = in.readLEFloat();
		velocity[0] = in.readLEFloat();
		velocity[1] = in.readLEFloat();
		velocity[2] = in.readLEFloat();
		time = in.readFloat();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(position[0]);
		out.writeLEFloat(position[1]);
		out.writeLEFloat(position[2]);
		out.writeLEFloat(velocity[0]);
		out.writeLEFloat(velocity[1]);
		out.writeLEFloat(velocity[2]);
		out.writeFloat(time);
	}
	
	// Spore uses blocks for this, but since there are no original effects and paths aren't really big, we can put them in a command
	public void parse(ArgScriptCommand c, List<ParticlePathPoint> pathPoints, int index) throws ArgScriptException {
		String arg = null;
		if ((arg = c.getOptionArg("p")) != null || (arg = c.getOptionArg("position")) != null) {
			ArgScript.parseFloatList(arg, position);
		}
		if ((arg = c.getOptionArg("dp")) != null || (arg = c.getOptionArg("deltaPosition")) != null) {
			float[] list = ArgScript.parseFloatList(arg, 3);
			float[] previous = null;
			int previousIndex = index - 1;
			if (previousIndex >= 0 && pathPoints.size() > previousIndex) {
				previous = pathPoints.get(previousIndex).position;
			} else {
				previous = new float[3];
			}
			position[0] = previous[0] + list[0];
			position[1] = previous[1] + list[1];
			position[2] = previous[2] + list[2];
		}
		if ((arg = c.getOptionArg("v")) != null || (arg = c.getOptionArg("velocity")) != null) {
			ArgScript.parseFloatList(arg, velocity);
		}
		if ((arg = c.getOptionArg("s")) != null || (arg = c.getOptionArg("speed")) != null) {
			// Warning: Spore might modify something else!
			float value = Float.parseFloat(arg);
			velocity[0] = value;
			velocity[1] = value;
			velocity[2] = value;
		}
		if ((arg = c.getOptionArg("t")) != null || (arg = c.getOptionArg("time")) != null) {
			time = Float.parseFloat(arg);
		}
		if ((arg = c.getOptionArg("dt")) != null || (arg = c.getOptionArg("deltaTime")) != null) {
			float value = Float.parseFloat(arg);
			float previous = 0;
			int previousIndex = index - 1;
			if (previousIndex >= 0 && pathPoints.size() > previousIndex) {
				previous = pathPoints.get(previousIndex).time;
			}
			time = previous + value;
		}
	}
	
	public ArgScriptCommand toCommand() {
		ArgScriptCommand c = new ArgScriptCommand("path");
		
		c.putOption(new ArgScriptOption("p", ArgScript.createFloatList(position)));
		if (velocity[0] == velocity[1] && velocity[1] == velocity[2]) {
			c.putOption(new ArgScriptOption("s", Float.toString(velocity[0])));
		}
		else {
			c.putOption(new ArgScriptOption("v", ArgScript.createFloatList(velocity)));
		}
		c.putOption(new ArgScriptOption("t", Float.toString(time)));
		
		return c;
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
			"p", "position", "dp", "deltaPosition", "v", "velocity", "s", "speed", "t", "time", "dt", "deltaTime"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
		};
	}
}
