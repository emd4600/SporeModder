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

public class PropertyString8 extends Property {
	private String value;
	public static final String singularName = "string8";
	public static final String pluralName = "string8s";
	public static final int PROP_TYPE = 0x0012;
	public static final int itemSize = 8;
	
	public PropertyString8(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyString8(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyString8(String name, String text) {
		super(name, PROP_TYPE);
		this.value = text;
	}
	
	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<string8>" + SpecialCharacters.fixStringLiteral(value) + "</string8>" + PROPMain.eol;
		} else {
			return "\t<string8 name=\"" + Hasher.getPropName(this.name) + "\">" + SpecialCharacters.fixStringLiteral(value) + "</string8>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		byte[] arr = new byte[in.readInt()];
		in.read(arr);
		value = new String(arr, "US-ASCII");
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(value));
	}
	@Override
	public void readXML(Element elem) {
		value = elem.getTextContent();
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeInt(value.length());
		byte[] arr = value.getBytes("US-ASCII");
		out.write(arr);
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
			ArrayProperty<PropertyString8> array = new ArrayProperty<PropertyString8>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyString8.class;
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
			array.setValues(new ArrayList<PropertyString8>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyString8 prop = new PropertyString8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyString8 prop = new PropertyString8(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setString(String string) {
		this.value = string;
	}
	
	public String getString() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeInt(text.length());
		stream.write(text.getBytes("US-ASCII"));
	}

}
