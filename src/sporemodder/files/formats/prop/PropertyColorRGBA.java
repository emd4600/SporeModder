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

public class PropertyColorRGBA extends Property {
	private float[] value = new float[4];
	public static final String singularName = "colorRGBA";
	public static final String pluralName = "colorRGBAs";
	public static final int PROP_TYPE = 0x0034;
	public static final int itemSize = 16;
	
	public PropertyColorRGBA(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyColorRGBA(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyColorRGBA(String name, Color color) {
		super(name, PROP_TYPE);
		setColor(color);
	}
	public PropertyColorRGBA(String name, int red, int green, int blue, int alpha) {
		super(name, PROP_TYPE);
		setColor(red, green, blue, alpha);
	}
	public PropertyColorRGBA(String name, float red, float green, float blue, float alpha) {
		super(name, PROP_TYPE);
		setColor(red, green, blue, alpha);
	}
	public PropertyColorRGBA(String name, int color) {
		super(name, PROP_TYPE);
		setColor(color);
	}

	@Override
	public String toString(boolean array) {
		NumberFormat nf = Hasher.getDecimalFormat("#.#######");
		if (array) {
			return "\t\t<colorRGBA>" + PROPMain.eol + 
					"\t\t\t<r>" + nf.format(value[0]) + "</r>" + PROPMain.eol +
					"\t\t\t<g>" + nf.format(value[1]) + "</g>" + PROPMain.eol +
					"\t\t\t<b>" + nf.format(value[2]) + "</b>" + PROPMain.eol +
					"\t\t\t<a>" + nf.format(value[3]) + "</a>" + PROPMain.eol +
					"\t\t</colorRGBA>" + PROPMain.eol;
		} else {
			return "\t<colorRGBA name=\"" + Hasher.getPropName(this.name) + "\">" + PROPMain.eol + 
					"\t\t<r>" + nf.format(value[0]) + "</r>" + PROPMain.eol +
					"\t\t<g>" + nf.format(value[1]) + "</g>" + PROPMain.eol +
					"\t\t<b>" + nf.format(value[2]) + "</b>" + PROPMain.eol +
					"\t\t<a>" + nf.format(value[3]) + "</a>" + PROPMain.eol +
					"\t</colorRGBA>" + PROPMain.eol;
		}
	}	
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value[0] = in.readLEFloat();
		value[1] = in.readLEFloat();
		value[2] = in.readLEFloat();
		value[3] = in.readLEFloat();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemR = doc.createElement("r");
		Element elemG = doc.createElement("g");
		Element elemB = doc.createElement("b");
		Element elemA = doc.createElement("a");
		elemR.appendChild(doc.createTextNode(Float.toString(value[0])));
		elemG.appendChild(doc.createTextNode(Float.toString(value[1])));
		elemB.appendChild(doc.createTextNode(Float.toString(value[2])));
		elemA.appendChild(doc.createTextNode(Float.toString(value[3])));
		elem.appendChild(elemR);elem.appendChild(elemG);elem.appendChild(elemB);elem.appendChild(elemA);
	}
	@Override
	public void readXML(Element elem) {
		NodeList nl = elem.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("r")) {
//				System.out.println(item.getTextContent());
				value[0] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("g")) {
				value[1] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("b")) {
				value[2] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("a")) {
				value[3] = Float.parseFloat(item.getTextContent());
			}
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEFloat(value[0]);
		out.writeLEFloat(value[1]);
		out.writeLEFloat(value[2]);
		out.writeLEFloat(value[3]);
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
		if (elem.getNodeName().length() > 9) {
			ArrayProperty<PropertyColorRGBA> array = new ArrayProperty<PropertyColorRGBA>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyColorRGBA.class;
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
			array.setValues(new ArrayList<PropertyColorRGBA>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyColorRGBA prop = new PropertyColorRGBA(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyColorRGBA prop = new PropertyColorRGBA(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setColor(Color color) {
		this.value[0] = color.getRed() / 255f;
		this.value[1] = color.getGreen() / 255f;
		this.value[2] = color.getBlue() / 255f;
		this.value[3] = color.getAlpha() / 255f;
	}
	public void setColor(int red, int green, int blue, int alpha) {
		this.value[0] = red / 255f;
		this.value[1] = green / 255f;
		this.value[2] = blue / 255f;
		this.value[3] = alpha / 255f;
	}
	public void setColor(float red, float green, float blue, float alpha) {
		this.value[0] = red;
		this.value[1] = green;
		this.value[2] = blue;
		this.value[3] = alpha;
	}
	public void setColor(int color) {
		this.value[0] = ((color & 0xFF000000) >> 24) / 255f;
		this.value[1] = ((color & 0xFF0000) >> 16) / 255f;
		this.value[2] = ((color & 0xFF00) >> 8) / 255f;
		this.value[3] = (color & 0xFF) / 255f;
	}
	
	public Color getColor() {
		return new Color(value[0], value[1], value[2], value[3]);
	}
}
