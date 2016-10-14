package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptException;

public class EffectColor {
	
	public static final EffectColor WHITE = new EffectColor(1.0f, 1.0f, 1.0f);
	public static final EffectColor BLACK = new EffectColor(0.0f, 0.0f, 0.0f);
	
	private float r;
	private float g;
	private float b;
	
	public EffectColor(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public EffectColor(int r, int g, int b) {
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
	}
	
	public EffectColor(EffectColor color) {
		copy(color);
	}
	
	public void copy(EffectColor color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
	}
	
	public EffectColor() {};
	
	public void parse(String str) throws ArgScriptException {
		String[] strings = ArgScript.parseList(str, 3);
		r = Float.parseFloat(strings[0]);
		g = Float.parseFloat(strings[1]);
		b = Float.parseFloat(strings[2]);
	}
	
	@Override
	public String toString() {
		return "(" + r + ", " + g + ", " + b + ")";
	}
	
	public float getR() {
		return r;
	}

	public float getG() {
		return g;
	}

	public float getB() {
		return b;
	}

	public void setR(float r) {
		this.r = r;
	}

	public void setG(float g) {
		this.g = g;
	}

	public void setB(float b) {
		this.b = b;
	}
	
	public boolean isWhite() {
		return r == 1.0f && g == 1.0f && b == 1.0f;
	}
	public boolean isBlack() {
		return r == 0.0f && g == 0.0f && b == 0.0f;
	}

	public void readLE(InputStreamAccessor in) throws IOException {
		r = in.readLEFloat();
		g = in.readLEFloat();
		b = in.readLEFloat();
	}
	
	public void readBE(InputStreamAccessor in) throws IOException {
		r = in.readFloat();
		g = in.readFloat();
		b = in.readFloat();
	}
	
	public void writeLE(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(r);
		out.writeLEFloat(g);
		out.writeLEFloat(b);
	}
	
	public void writeBE(OutputStreamAccessor out) throws IOException {
		out.writeFloat(r);
		out.writeFloat(g);
		out.writeFloat(b);
	}
	
}
