package sporemodder.files.formats.bem;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class BEMInfo {
	private int modelType;
	private int zCorpScore;
	private int[] skinEffects = new int[3];
	private int[] skinEffectSeeds = new int[3];
	private float[][] skinColors = new float[3][3];
	
	public void read(InputStreamAccessor in) throws IOException
	{
		modelType = in.readLEInt();
		zCorpScore = in.readLEInt();
		in.readLEInts(skinEffects);
		in.readLEInts(skinEffectSeeds);
		for (float[] skinColor : skinColors) {
			skinColor = new float[3];
			in.readLEFloats(skinColor);
		}
	}
	
	public void write(OutputStreamAccessor out) throws IOException 
	{
		out.writeLEInt(modelType);
		out.writeLEInt(zCorpScore);
		out.writeLEInts(skinEffects);
		out.writeLEInts(skinEffectSeeds);
		for (float[] skinColor : skinColors) {
			out.writeLEFloats(skinColor);
		}
	}
	
	public void toXML(Element elem, Document doc) {
		Element eProperties = doc.createElement("properties");
		
		Element eModelType = doc.createElement("modeltype");
		Element eSkinEffect1 = doc.createElement("skineffect1");
		Element eSkinEffect2 = doc.createElement("skineffect2");
		Element eSkinEffect3 = doc.createElement("skineffect3");
		Element eSkinEffectSeed1 = doc.createElement("skineffectseed1");
		Element eSkinEffectSeed2 = doc.createElement("skineffectseed2");
		Element eSkinEffectSeed3 = doc.createElement("skineffectseed3");
		Element eSkinColor1 = doc.createElement("skincolor1");
		Element eSkinColor2 = doc.createElement("skincolor2");
		Element eSkinColor3 = doc.createElement("skincolor3");
		Element eZCorpScore = doc.createElement("zcorpscore");
		
		eModelType.setNodeValue(Hasher.getFileName(modelType));
		eSkinEffect1.setNodeValue(Hasher.getFileName(skinEffects[0]));
		eSkinEffect2.setNodeValue(Hasher.getFileName(skinEffects[1]));
		eSkinEffect3.setNodeValue(Hasher.getFileName(skinEffects[2]));
		eSkinEffectSeed1.setNodeValue(Integer.toString(skinEffectSeeds[0]));
		eSkinEffectSeed2.setNodeValue(Integer.toString(skinEffectSeeds[1]));
		eSkinEffectSeed3.setNodeValue(Integer.toString(skinEffectSeeds[2]));
		eSkinColor1.setNodeValue(Float.toString(skinColors[0][0]) + "," + Float.toString(skinColors[0][1]) + "," + Float.toString(skinColors[0][2]));
		eSkinColor2.setNodeValue(Float.toString(skinColors[1][0]) + "," + Float.toString(skinColors[1][1]) + "," + Float.toString(skinColors[1][2]));
		eSkinColor3.setNodeValue(Float.toString(skinColors[2][0]) + "," + Float.toString(skinColors[2][1]) + "," + Float.toString(skinColors[2][2]));
		eZCorpScore.setNodeValue(Integer.toString(zCorpScore));
		
		eProperties.appendChild(eModelType);
		eProperties.appendChild(eSkinEffect1);
		eProperties.appendChild(eSkinEffect2);
		eProperties.appendChild(eSkinEffect3);
		eProperties.appendChild(eSkinEffectSeed1);
		eProperties.appendChild(eSkinEffectSeed2);
		eProperties.appendChild(eSkinEffectSeed3);
		eProperties.appendChild(eSkinColor1);
		eProperties.appendChild(eSkinColor2);
		eProperties.appendChild(eSkinColor3);
		eProperties.appendChild(eZCorpScore);
		
		elem.appendChild(eProperties);
	}
	
}
