package sporemodder.files.formats.prop;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
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

public class PropertyColorRGB extends Property{
	private float[] value = new float[3];
	private int unk1;
	public static final String singularName = "colorRGB";
	public static final String pluralName = "colorRGBs";
	public static final int PROP_TYPE = 0x0032;
	public static final int itemSize = 12;
	
	public PropertyColorRGB(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyColorRGB(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyColorRGB(String name, Color color) throws IOException {
		super(name, PROP_TYPE);
		setColor(color);
	}
	public PropertyColorRGB(String name, int red, int green, int blue) throws IOException {
		super(name, PROP_TYPE);
		setColor(red, green, blue);
	}
	public PropertyColorRGB(String name, float red, float green, float blue) throws IOException {
		super(name, PROP_TYPE);
		setColor(red, green, blue);
	}
	public PropertyColorRGB(String name, int color) throws IOException {
		super(name, PROP_TYPE);
		setColor(color);
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		NumberFormat nf = new DecimalFormat("#.#######");
		if (array) {
			String str = "\t\t<colorRGB";
			if (getDebugMode() && unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
			str += ">" + PROPMain.eol + 
					"\t\t\t<r>" + nf.format(value[0]) + "</r>" + PROPMain.eol +
					"\t\t\t<g>" + nf.format(value[1]) + "</g>" + PROPMain.eol +
					"\t\t\t<b>" + nf.format(value[2]) + "</b>" + PROPMain.eol +
					"\t\t</colorRGB>" + PROPMain.eol;
			return str;
		} else {
			String str = "\t<colorRGB name=\"" + Hasher.getPropName(this.name) + "\"";
			if (getDebugMode() && unk1 != 0) str += " unk1=\"" + Hasher.getFileName(unk1) + "\"";
			str += ">" + PROPMain.eol +  
					"\t\t<r>" + nf.format(value[0]) + "</r>" + PROPMain.eol +
					"\t\t<g>" + nf.format(value[1]) + "</g>" + PROPMain.eol +
					"\t\t<b>" + nf.format(value[2]) + "</b>" + PROPMain.eol +
					"\t</colorRGB>" + PROPMain.eol;
			return str;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			value[0] = in.readLEFloat();
			value[1] = in.readLEFloat();
			value[2] = in.readLEFloat();
		} else {
			value[0] = in.readLEFloat();
			value[1] = in.readLEFloat();
			value[2] = in.readLEFloat();
			unk1 = in.readLEInt();
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemR = doc.createElement("r");
		Element elemG = doc.createElement("g");
		Element elemB = doc.createElement("b");
		elemR.appendChild(doc.createTextNode(Float.toString(value[0])));
		elemG.appendChild(doc.createTextNode(Float.toString(value[1])));
		elemB.appendChild(doc.createTextNode(Float.toString(value[2])));
		elem.appendChild(elemR);elem.appendChild(elemG);elem.appendChild(elemB);
	}
	@Override
	public void readXML(Element elem) throws IOException {
		NodeList nl = elem.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("r")) {
				value[0] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("g")) {
				value[1] = Float.parseFloat(item.getTextContent());
			} else if (item.getNodeName().equals("b")) {
				value[2] = Float.parseFloat(item.getTextContent());
			}
		}
		
		String str1 = elem.getAttribute("unk1");
		if (str1.length() > 0) {
			unk1 = Hasher.getFileHash(str1);
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEFloat(value[0]);
		out.writeLEFloat(value[1]);
		out.writeLEFloat(value[2]);
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
		if (elem.getNodeName().length() > 8) {
			ArrayProperty<PropertyColorRGB> array = new ArrayProperty<PropertyColorRGB>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyColorRGB.class;
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
			array.setValues(new ArrayList<PropertyColorRGB>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyColorRGB prop = new PropertyColorRGB(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyColorRGB prop = new PropertyColorRGB(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setColor(Color color) {
		this.value[0] = color.getRed() / 255f;
		this.value[1] = color.getGreen() / 255f;
		this.value[2] = color.getBlue() / 255f;
	}
	public void setColor(int red, int green, int blue) {
		this.value[0] = red / 255f;
		this.value[1] = green / 255f;
		this.value[2] = blue / 255f;
	}
	public void setColor(float red, float green, float blue) {
		this.value[0] = red;
		this.value[1] = green;
		this.value[2] = blue;
	}
	public void setColor(int color) {
		this.value[0] = ((color & 0xFF0000) >> 16) / 255f;
		this.value[1] = ((color & 0xFF00) >> 8) / 255f;
		this.value[2] = (color & 0xFF) / 255f;
	}
	
	public Color getColor() {
		return new Color(value[0], value[1], value[2]);
	}
	
	
	public void setUnk(int unk) {
		this.unk1 = unk;
	}
	
	public int getUnk() {
		return unk1;
	}
}
