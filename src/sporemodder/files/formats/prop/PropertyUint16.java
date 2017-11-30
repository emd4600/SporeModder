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

public class PropertyUint16 extends Property {
	private int value;
	public static final String singularName = "uint16";
	public static final String pluralName = "uint16s";
	public static final int PROP_TYPE = 0x0008;
	public static final int itemSize = 2;
	
	public PropertyUint16(int name, int type, int flags) {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyUint16(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyUint16(String name, int value) {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<uint16>" + Integer.toString(value) + "</uint16>" + PROPMain.eol;
		} else {
			return "\t<uint16 name=\"" + Hasher.getPropName(this.name) + "\">" + Integer.toString(value) + "</uint16>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readUShort();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Integer.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Integer.parseInt(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeUShort(value);
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
		if (elem.getNodeName().length() > 6) {
			ArrayProperty<PropertyUint16> array = new ArrayProperty<PropertyUint16>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyUint16.class;
			array.arrayItemSize = 2;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyUint16>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyUint16 prop = new PropertyUint16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyUint16 prop = new PropertyUint16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeUShort(Hasher.decodeUShort(text));
	}
}
