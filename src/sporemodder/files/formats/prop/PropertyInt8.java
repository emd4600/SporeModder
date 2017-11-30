package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PropertyInt8 extends Property {
	private byte value;
	public static final String singularName = "int8";
	public static final String pluralName = "int8s";
	public static final int PROP_TYPE = 0x0005;
	public static final int itemSize = 1;
	
	public PropertyInt8(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyInt8(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyInt8(String name, byte value) {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<int8>" + Byte.toString(value) + "</int8>" + PROPMain.eol;
		} else {
			return "\t<int8 name=\"" + Hasher.getPropName(this.name) + "\">" + Byte.toString(value) + "</int8>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readByte();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Byte.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Byte.parseByte(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeByte(value);
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
		if (elem.getNodeName().length() > 4) {
			ArrayProperty<PropertyInt8> array = new ArrayProperty<PropertyInt8>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyInt8.class;
			array.arrayItemSize = 1;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyInt8>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyInt8 prop = new PropertyInt8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyInt8 prop = new PropertyInt8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(byte value) {
		this.value = value;
	}
	
	public byte getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeByte(Hasher.decodeByte(text));
	}
}
