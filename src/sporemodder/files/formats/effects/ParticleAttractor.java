package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class ParticleAttractor {
	protected float[] attractorStrength = new float[0];
	protected float range = 1;
	protected float killRange;
	
	public ParticleAttractor() {}
	public ParticleAttractor(ParticleAttractor other) {
		copy(other);
	}
	
	public void copy(ParticleAttractor other) {
		attractorStrength = EffectComponent.copyArray(other.attractorStrength);
		range = other.range;
		killRange = other.killRange;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		attractorStrength = new float[in.readInt()];
		in.readFloats(attractorStrength);
		range = in.readFloat();
		killRange = in.readFloat();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(attractorStrength.length);
		out.writeFloats(attractorStrength);
		out.writeFloat(range);
		out.writeFloat(killRange);
	}
}
