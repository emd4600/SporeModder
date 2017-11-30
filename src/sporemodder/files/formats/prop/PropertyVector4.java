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

public class PropertyVector4 extends Property {
	private float[] values;
	public static final String singularName = "vector4";
	public static final String pluralName = "vector4s";
	public static final int PROP_TYPE = 0x0033;
	public static final int itemSize = 16;
	
	public PropertyVector4(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyVector4(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyVector4(String name, float x, float y, float z, float alpha) {
		super(name, PROP_TYPE);
		values[0] = x;
		values[1] = y;
		values[2] = z;
		values[2] = alpha;
	}
	public PropertyVector4(String name, int r, int g, int b, int alpha) {
		super(name, PROP_TYPE);
		setColor(r, g, b, alpha);
	}
	public PropertyVector4(String name, Color color) {
		super(name, PROP_TYPE);
		setColor(color);
	}
	public PropertyVector4(String name, int color) {
		super(name, PROP_TYPE);
		setColor(color);
	}
	
	@Override
	public String toString(boolean array) {
		NumberFormat nf = Hasher.getDecimalFormat("#.#######");
		if (array) {
			return "\t\t<vector4>" + PROPMain.eol + 
					"\t\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t\t\t<z>" + nf.format(values[2]) + "</z>" + PROPMain.eol +
					"\t\t\t<w>" + nf.format(values[3]) + "</w>" + PROPMain.eol +
					"\t\t</vector4>" + PROPMain.eol;
		} else {
			return "\t<vector4 name=\"" + Hasher.getPropName(this.name) + "\">" + PROPMain.eol + 
					"\t\t<x>" + nf.format(values[0]) + "</x>" + PROPMain.eol +
					"\t\t<y>" + nf.format(values[1]) + "</y>" + PROPMain.eol +
					"\t\t<z>" + nf.format(values[2]) + "</z>" + PROPMain.eol +
					"\t\t<w>" + nf.format(values[3]) + "</w>" + PROPMain.eol +
					"\t</vector4>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		values = new float[4];
		in.readLEFloats(values);
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemX = doc.createElement("x");
		elemX.appendChild(doc.createTextNode(Float.toString(values[0])));
		Element elemY = doc.createElement("y");
		elemY.appendChild(doc.createTextNode(Float.toString(values[1])));
		Element elemZ = doc.createElement("z");
		elemZ.appendChild(doc.createTextNode(Float.toString(values[2])));
		Element elemW = doc.createElement("z");
		elemW.appendChild(doc.createTextNode(Float.toString(values[3])));
		
		elem.appendChild(elemX);
		elem.appendChild(elemY);
		elem.appendChild(elemZ);
		elem.appendChild(elemW);
	}
	@Override
	public void readXML(Element elem) {
		values = new float[4];
		NodeList nl = elem.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("x")) {
				values[0] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("y")) {
				values[1] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("z")) {
				values[2] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("w")) {
				values[3] = Float.parseFloat(item.getTextContent());
			}
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEFloat(values[0]);
		out.writeLEFloat(values[1]);
		out.writeLEFloat(values[2]);
		out.writeLEFloat(values[3]);
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
			ArrayProperty<PropertyVector4> array = new ArrayProperty<PropertyVector4>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyVector4.class;
			array.arrayItemSize = 16;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyVector4>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyVector4 prop = new PropertyVector4(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyVector4 prop = new PropertyVector4(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setVector(float[] vector) {
		this.values = vector;
	}
	
	public float[] getVector() {
		return this.values;
	}
	
	public void setColor(Color color) {
		this.values[0] = color.getRed() / 255f;
		this.values[1] = color.getGreen() / 255f;
		this.values[2] = color.getBlue() / 255f;
		this.values[3] = color.getAlpha() / 255f;
	}
	public void setColor(int red, int green, int blue, int alpha) {
		this.values[0] = red / 255f;
		this.values[1] = green / 255f;
		this.values[2] = blue / 255f;
		this.values[3] = alpha / 255f;
	}
	public void setColor(float red, float green, float blue, float alpha) {
		this.values[0] = red;
		this.values[1] = green;
		this.values[2] = blue;
		this.values[3] = alpha;
	}
	public void setColor(int color) {
		this.values[0] = ((color & 0xFF000000) >> 24) / 255f;
		this.values[1] = ((color & 0xFF0000) >> 16) / 255f;
		this.values[2] = ((color & 0xFF00) >> 8) / 255f;
		this.values[3] = (color & 0xFF) / 255f;
	}
	
	public Color getColor() {
		return new Color(values[0], values[1], values[2], values[3]);
	}
}
