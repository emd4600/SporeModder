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

public class PropertyChar extends Property {
	private char value;
	public static final String singularName = "char";
	public static final String pluralName = "chars";
	public static final int PROP_TYPE = 0x0002;
	public static final int itemSize = 1;
	
	public PropertyChar(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyChar(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyChar(String name, char value) {
		super(name, PROP_TYPE);
		this.value = value;
	}

	
	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<char>" + Character.toString(value) + "</char>" + PROPMain.eol;
		} else {
			return "\t<char name=\"" + Hasher.getPropName(this.name) + "\">" + Character.toString(value) + "</char>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = (char)in.readByte();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Character.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		//TODO
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
	
	public static Property processNewProperty(Element elem) throws InstantiationException, IllegalAccessException, IOException  {
		if (elem.getNodeName().length() > 4) {
			ArrayProperty<PropertyChar> array = new ArrayProperty<PropertyChar>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyChar.class;
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
			array.setValues(new ArrayList<PropertyChar>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyChar prop = new PropertyChar(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyChar prop = new PropertyChar(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(char value) {
		this.value = value;
	}
	
	public char getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeByte(text.charAt(0));
	}
}
