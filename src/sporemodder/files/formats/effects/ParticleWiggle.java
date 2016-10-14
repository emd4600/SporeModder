package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class ParticleWiggle {
	protected float timeRate;
	protected final float[] rateDirection = new float[3];
	protected final float[] wiggleDirection = new float[3];
	
	public ParticleWiggle() {}
	public ParticleWiggle(ParticleWiggle other) {
		timeRate = other.timeRate;
		rateDirection[0] = other.rateDirection[0];
		rateDirection[1] = other.rateDirection[1];
		rateDirection[2] = other.rateDirection[2];
		wiggleDirection[0] = other.wiggleDirection[0];
		wiggleDirection[1] = other.wiggleDirection[1];
		wiggleDirection[2] = other.wiggleDirection[2];
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		timeRate = in.readFloat();
		rateDirection[0] = in.readLEFloat();
		rateDirection[1] = in.readLEFloat();
		rateDirection[2] = in.readLEFloat();
		wiggleDirection[0] = in.readLEFloat();
		wiggleDirection[1] = in.readLEFloat();
		wiggleDirection[2] = in.readLEFloat();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeFloat(timeRate);
		out.writeLEFloat(rateDirection[0]);
		out.writeLEFloat(rateDirection[1]);
		out.writeLEFloat(rateDirection[2]);
		out.writeLEFloat(wiggleDirection[0]);
		out.writeLEFloat(wiggleDirection[1]);
		out.writeLEFloat(wiggleDirection[2]);
	}
}
