package sporemodder.files.formats.prop;

import java.awt.Color;
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

public class PropertyVector3 extends Property {
	private float[] values = new float[3];
	private int unk1;
	public static final String singularName = "vector3";
	public static final String pluralName = "vector3s";
	public static final int PROP_TYPE = 0x0031;
	public static final int itemSize = 12;
	
	public PropertyVector3(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyVector3(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyVector3(String name, float x, float y, float z) {
		super(name, PROP_TYPE);
		values[0] = x;
		values[1] = y;
		values[2] = z;
	}
	public PropertyVector3(String name, float[] arr) {
		super(name, PROP_TYPE);
		values = arr;
	}
	public PropertyVector3(String name, int r, int g, int b) {
		super(name, PROP_TYPE);
		setColor(r, g, b);
	}
	public PropertyVector3(String name, Color color) {
		super(name, PROP_TYPE);
		setColor(color);
	}
	public PropertyVector3(String name, int color) {
		super(name, PROP_TYPE);
		setColor(color);
	}
	
	@Override
	public String toString(boolean array) {
		NumberFormat nf = Hasher.getDecimalFormat("#.#######");
		if (array) {
			String str = "\t\t<vector3";
			if (getDebugMode() && unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
			str += ">" + PROPMain.eol + 
					"\t\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t\t\t<z>" + nf.format(values[2]) + "</z>" + PROPMain.eol +
					"\t\t</vector3>" + PROPMain.eol;
			return str;
		} else {
			String str = "\t<vector3 name=\"" + Hasher.getPropName(this.name) + "\"";
			if (getDebugMode() && unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
			str += ">" + PROPMain.eol + 
					"\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t\t<z>" + nf.format(values[2]) + "</z>" + PROPMain.eol +
					"\t</vector3>" + PROPMain.eol;
			return str;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			values[0] = in.readLEFloat();
			values[1] = in.readLEFloat();
			values[2] = in.readLEFloat();
		} else {
			values[0] = in.readLEFloat();
			values[1] = in.readLEFloat();
			values[2] = in.readLEFloat();
			unk1 = in.readLEInt();
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemX = doc.createElement("x");
		elemX.appendChild(doc.createTextNode(Float.toString(values[0])));
		Element elemY = doc.createElement("y");
		elemY.appendChild(doc.createTextNode(Float.toString(values[1])));
		Element elemZ = doc.createElement("z");
		elemZ.appendChild(doc.createTextNode(Float.toString(values[2])));
		
		elem.appendChild(elemX);
		elem.appendChild(elemY);
		elem.appendChild(elemZ);
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
			} else if (item.getNodeName().equals("z")) {
				values[2] = Float.parseFloat(item.getTextContent());
			}
		}
		String str1 = elem.getAttribute("unk1");
		if (str1.length() > 0) {
			unk1 = Hasher.getFileHash(str1);
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEFloat(values[0]);
		out.writeLEFloat(values[1]);
		out.writeLEFloat(values[2]);
		if (!array) {
			out.writeLEInt(unk1);
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
			ArrayProperty<PropertyVector3> array = new ArrayProperty<PropertyVector3>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyVector3.class;
			array.arrayItemSize = 12;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyVector3>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyVector3 prop = new PropertyVector3(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyVector3 prop = new PropertyVector3(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setVector(float[] vector) {
		this.values = vector;
	}
	public void setUnk(int unk) {
		unk1 = unk;
	}
	
	public float[] getVector() {
		return this.values;
	}
	public int getUnk() {
		return unk1;
	}
	
	public void setColor(Color color) {
		this.values[0] = color.getRed() / 255f;
		this.values[1] = color.getGreen() / 255f;
		this.values[2] = color.getBlue() / 255f;
	}
	public void setColor(int red, int green, int blue) {
		this.values[0] = red / 255f;
		this.values[1] = green / 255f;
		this.values[2] = blue / 255f;
	}
	public void setColor(float red, float green, float blue) {
		this.values[0] = red;
		this.values[1] = green;
		this.values[2] = blue;
	}
	public void setColor(int color) {
		this.values[0] = ((color & 0xFF0000) >> 16) / 255f;
		this.values[1] = ((color & 0xFF00) >> 8) / 255f;
		this.values[2] = (color & 0xFF) / 255f;
	}
	
	public Color getColor() {
		return new Color(values[0], values[1], values[2]);
	}
}
