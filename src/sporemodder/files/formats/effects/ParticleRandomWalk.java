package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class ParticleRandomWalk {
	protected final float[] time = {5, 5};
	protected final float[] strength = new float[2];
	protected float turnRange = 0.25f;
	protected float turnOffset;
	protected float mix;
	protected float[] turnOffsetCurve = new float[0];
	protected int loopType = 2; //byte, might not exist
	
	public ParticleRandomWalk() {}
	public ParticleRandomWalk(ParticleRandomWalk other) {
		copy(other);
	}
	
	public void copy(ParticleRandomWalk other) {
		time[0] = other.time[0];
		time[1] = other.time[1];
		strength[0] = other.strength[0];
		strength[1] = other.strength[1];
		turnRange = other.turnRange;
		turnOffset = other.turnOffset;
		mix = other.mix;
		turnOffsetCurve = EffectComponent.copyArray(other.turnOffsetCurve);
		loopType = other.loopType;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		time[0] = in.readLEFloat();  // 5
		time[1] = in.readLEFloat();  // 5
		strength[0] = in.readLEFloat();
		strength[1] = in.readLEFloat();
		turnRange = in.readFloat();  // 0.25
		turnOffset = in.readFloat();
		mix = in.readFloat();
		turnOffsetCurve = new float[in.readInt()];
		in.readFloats(turnOffsetCurve);
		loopType = in.readByte();  // 2
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(time[0]);
		out.writeLEFloat(time[1]);
		out.writeLEFloat(strength[0]);
		out.writeLEFloat(strength[1]);
		out.writeFloat(turnRange);
		out.writeFloat(turnOffset);
		out.writeFloat(mix);
		out.writeInt(turnOffsetCurve.length);
		out.writeFloats(turnOffsetCurve);
		out.writeByte(loopType);
	}
}
