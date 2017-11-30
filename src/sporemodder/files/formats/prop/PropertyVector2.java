package sporemodder.files.formats.prop;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PropertyVector2 extends Property {
	private float[] values = new float[2];
	private int unk1, unk2;
	public static String singularName = "vector2";
	public static String pluralName = "vector2s";
	public static final int PROP_TYPE = 0x0030;
	public static final int itemSize = 8;
	
	public PropertyVector2(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyVector2(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyVector2(String name, float x, float y) {
		super(name, PROP_TYPE);
		values[0] = x;
		values[1] = y;
	}
	
	@Override
	public String toString(boolean array) {
		NumberFormat nf = Hasher.getDecimalFormat("#.#######");
		if (array) {
			String str = "\t\t<vector2";
			if (getDebugMode()) {
				if (unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
				if (unk2 != 0) str += " unk2=\"" + Hasher.getFileName(unk2) + "\"";
			}
			str += ">" + PROPMain.eol + 
					"\t\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t\t</vector2>" + PROPMain.eol;
			return str;
		} else {
			String str = "\t<vector2 name=\"" + Hasher.getPropName(this.name) + "\"";
			if (getDebugMode()) {
				if (unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
				if (unk2 != 0) str += " unk2=\"" + Hasher.getFileName(unk2) + "\"";
			}
			str += ">" + PROPMain.eol + 
					"\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t</vector2>" + PROPMain.eol;
			return str;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			values[0] = in.readLEFloat();
			values[1] = in.readLEFloat();
		} else {
			values[0] = in.readLEFloat();
			values[1] = in.readLEFloat();
			unk1 = in.readLEInt();
			unk2 = in.readLEInt();
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemX = doc.createElement("x");
		elemX.appendChild(doc.createTextNode(Float.toString(values[0])));
		Element elemY = doc.createElement("y");
		elemY.appendChild(doc.createTextNode(Float.toString(values[1])));
		
		elem.appendChild(elemX);
		elem.appendChild(elemY);
	}
	@Override
	public void readXML(Element elem) throws IOException {
		NodeList nl = elem.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("x")) {
				values[0] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("y")) {
				values[1] = Float.parseFloat(item.getTextContent());
			}
		}

		String str1 = elem.getAttribute("unk1");
		if (str1.length() > 0) {
			unk1 = Hasher.getFileHash(str1);
		}
		String str2 = elem.getAttribute("unk2");
		if (str2.length() > 0) {
			unk2 = Hasher.getFileHash(str2);
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEFloat(values[0]);
		out.writeLEFloat(values[1]);
		if (!array) {
			out.writeLEInt(unk1);
			out.writeLEInt(unk2);
		}
	}
	@Override
	public String getSingularName() {
		return singularName;
	}
	@Override
	public String getPluralName() {
		return pluralName;
	}
	public static Property processNewProperty(Element elem) throws InstantiationException, IllegalAccessException, IOException {
		if (elem.getNodeName().length() > 7) {
			ArrayProperty<PropertyVector2> array = new ArrayProperty<PropertyVector2>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyVector2.class;
			array.arrayItemSize = 8;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyVector2>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyVector2 prop = new PropertyVector2(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyVector2 prop = new PropertyVector2(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setVector(float[] vector) {
		this.values = vector;
	}
	public void setUnks(int[] unks) {
		unk1 = unks[0];
		unk2 = unks[1];
	}
	
	public float[] getVector() {
		return this.values;
	}
	public int[] getUnks() {
		return new int[] {unk1, unk2};
	}
}
