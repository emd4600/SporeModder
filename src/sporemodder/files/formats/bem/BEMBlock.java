package sporemodder.files.formats.bem;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.utilities.Hasher;

public class BEMBlock extends FileStructure {
	private int[] blockId = new int[2];
	private int unk1;
	private int symmetric; // index of symmetric block. -1 when it doesn't have a symmetric block
	private float scale;
	private float[] position = new float[3];
	private float[] triangleDirection = new float[3];
	private float[] trianglePickOrigin = new float[3];
	private float[][] orientation = new float[3][3];
	private float[][] userOrientation = new float[3][3]; // identity matrix when not present
	private boolean snapped;
	private boolean isAsymmetric;
	// two bytes of padding ?
	private int unk3; // 3 ?
	private int unk4; // 0 ?
	private float baseMuscleScale;
	private int handlesCount;
	private float[] handlesWeight = new float[8];
	private int[] handlesChannel = new int[8];
	
	// 260 padding bytes?
	
	public void read(InputStreamAccessor in) throws IOException 
	{
		blockId[0] = in.readLEInt();
		blockId[1] = in.readLEInt();
		unk1 = in.readLEInt();
		symmetric = in.readLEInt();
		scale = in.readLEFloat();
		in.readLEFloats(position);
		in.readLEFloats(triangleDirection);
		in.readLEFloats(trianglePickOrigin);
		for (float[] row : orientation) in.readLEFloats(row);
		for (float[] row : userOrientation) in.readLEFloats(row);
		snapped =  in.readBoolean();
		isAsymmetric = in.readBoolean();
		in.skipBytes(2); // two bytes of padding ? 
		unk3 = in.readLEInt();
		unk4 = in.readLEInt();
		baseMuscleScale = in.readLEFloat();
		handlesCount = in.readLEInt();
		in.readLEFloats(handlesWeight);
		in.readLEInts(handlesChannel);
	}
	
	public void write(OutputStreamAccessor out) throws IOException
	{
		out.writeLEInt(blockId[0]);
		out.writeLEInt(blockId[1]);
		out.writeLEInt(unk1);
		out.writeLEInt(symmetric);
		out.writeLEFloat(scale);
		out.writeLEFloats(position);
		out.writeLEFloats(triangleDirection);
		out.writeLEFloats(trianglePickOrigin);
		for (float[] row : orientation) out.writeLEFloats(row);
		for (float[] row : userOrientation) out.writeLEFloats(row);
		out.writeBoolean(snapped);
		out.writeBoolean(isAsymmetric);
		out.writePadding(2);
		out.writeLEInt(unk3);
		out.writeLEInt(unk4);
		out.writeLEFloat(baseMuscleScale);
		out.writeLEInt(handlesCount);
		out.writeLEFloats(handlesWeight);
		out.writeLEInts(handlesChannel);
		out.writePadding(260); // ?
	}
	
	public void toXML(Element elem, Document doc) 
	{
		Element blockRef = doc.createElement("blockref");
		Element eBlockID = doc.createElement("blockid");
		Element eUnk1 = doc.createElement("unk1");
		eBlockID.setNodeValue(Hasher.getFileName(blockId[0]) + ", " + Hasher.getFileName(blockId[1]));
		eUnk1.setNodeValue(Integer.toString(unk1));
		
		Element eTransform = doc.createElement("transform");
		///// transform /////
		Element eBaseMuscleScale = doc.createElement("basemusclescale");
		Element eScale = doc.createElement("scale");
		Element ePosition = doc.createElement("position");
		Element eTriDirection = doc.createElement("triangledirection");
		Element eTriPickOrigin = doc.createElement("trianglepickorigin");
		
		eBaseMuscleScale.setNodeValue(Float.toString(baseMuscleScale));
		eScale.setNodeValue(Float.toString(scale));
		ePosition.setNodeValue(Float.toString(position[0]) + "," + Float.toString(position[1]) + "," + Float.toString(position[2]));
		eTriDirection.setNodeValue(Float.toString(triangleDirection[0]) + "," + Float.toString(triangleDirection[1]) + "," + Float.toString(triangleDirection[2]));
		eTriPickOrigin.setNodeValue(Float.toString(trianglePickOrigin[0]) + "," + Float.toString(trianglePickOrigin[1]) + "," + Float.toString(trianglePickOrigin[2]));
		
		Element eOrientation = doc.createElement("orientation");
		{
			Element eRow1 = doc.createElement("row1");
			Element eRow2 = doc.createElement("row2");
			Element eRow3 = doc.createElement("row3");
			eRow1.setNodeValue(Float.toString(orientation[0][0]) + "," + Float.toString(orientation[0][1]) + "," + Float.toString(orientation[0][2]));
			eRow2.setNodeValue(Float.toString(orientation[1][0]) + "," + Float.toString(orientation[1][1]) + "," + Float.toString(orientation[1][2]));
			eRow3.setNodeValue(Float.toString(orientation[2][0]) + "," + Float.toString(orientation[2][1]) + "," + Float.toString(orientation[2][2]));
			eOrientation.appendChild(eRow1);
			eOrientation.appendChild(eRow2);
			eOrientation.appendChild(eRow3);
		}
		Element eUserOrientation = doc.createElement("userorientation");
		{
			Element eRow1 = doc.createElement("row1");
			Element eRow2 = doc.createElement("row2");
			Element eRow3 = doc.createElement("row3");
			eRow1.setNodeValue(Float.toString(userOrientation[0][0]) + "," + Float.toString(userOrientation[0][1]) + "," + Float.toString(userOrientation[0][2]));
			eRow2.setNodeValue(Float.toString(userOrientation[1][0]) + "," + Float.toString(userOrientation[1][1]) + "," + Float.toString(userOrientation[1][2]));
			eRow3.setNodeValue(Float.toString(userOrientation[2][0]) + "," + Float.toString(userOrientation[2][1]) + "," + Float.toString(userOrientation[2][2]));
			eUserOrientation.appendChild(eRow1);
			eUserOrientation.appendChild(eRow2);
			eUserOrientation.appendChild(eRow3);
		}
		
		eTransform.appendChild(eScale);
		eTransform.appendChild(ePosition);
		eTransform.appendChild(eTriDirection);
		eTransform.appendChild(eTriPickOrigin);
		eTransform.appendChild(eOrientation);
		eTransform.appendChild(eUserOrientation);
		///// -------- /////
		
		Element eSnapped = doc.createElement("snapped");
		Element eSymmetric = doc.createElement("symmetric");
		eSnapped.setNodeValue(Boolean.toString(snapped));
		eSymmetric.setNodeValue(Integer.toString(symmetric));
		
		Element eHandles = doc.createElement("handles");
		eHandles.setAttribute("count", Integer.toString(handlesCount));
		for (int i = 0; i < handlesCount; i++) {
			Element eWeight = doc.createElement("weight");
			eWeight.setAttribute("channel", Hasher.getFileName(handlesChannel[i]));
			eWeight.setNodeValue(Float.toString(handlesWeight[i]));
		}
		
		Element eIsAsymmetric = doc.createElement("isasymmetric");
		eIsAsymmetric.setNodeValue(Boolean.toString(isAsymmetric));
		
		blockRef.appendChild(eBlockID);
		blockRef.appendChild(eTransform);
		blockRef.appendChild(eSnapped);
		blockRef.appendChild(eSymmetric);
		blockRef.appendChild(eHandles);
		blockRef.appendChild(eIsAsymmetric);
		elem.appendChild(blockRef);
		
		//TODO what about childlist? Isn't it present in bem files? 
	}
}

