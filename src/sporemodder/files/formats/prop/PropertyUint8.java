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

public class PropertyUint8 extends Property {
	public short value;
	public static final String singularName = "uint8";
	public static final String pluralName = "uint8s";
	public static final int PROP_TYPE = 0x0006;
	public static final int itemSize = 1;
	
	public PropertyUint8(int name, int type, int flags) {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyUint8(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyUint8(String name, short value) {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<uint8>" + Short.toString(value) + "</uint8>" + PROPMain.eol;
		} else {
			return "\t<uint8 name=\"" + Hasher.getPropName(this.name) + "\">" + Short.toString(value) + "</uint8>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readUByte();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Short.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Short.parseShort(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeUByte(value);
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
		if (elem.getNodeName().length() > 5) {
			ArrayProperty<PropertyUint8> array = new ArrayProperty<PropertyUint8>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyUint8.class;
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
			array.setValues(new ArrayList<PropertyUint8>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyUint8 prop = new PropertyUint8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyUint8 prop = new PropertyUint8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeUByte(Hasher.decodeUByte(text));
	}
}
