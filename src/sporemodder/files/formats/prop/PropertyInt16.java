package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PropertyInt16 extends Property {
	private short value;
	public static final String singularName = "int16";
	public static final String pluralName = "int16s";
	public static final int PROP_TYPE = 0x0007;
	public static final int itemSize = 2;
	
	public PropertyInt16(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyInt16(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyInt16(String name, short value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			return "\t\t<int16>" + Short.toString(value) + "</int16>" + PROPMain.eol;
		} else {
			return "\t<int16 name=\"" + Hasher.getPropName(this.name) + "\">" + Short.toString(value) + "</int16>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readShort();
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
		out.writeShort(value);
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
			ArrayProperty<PropertyInt16> array = new ArrayProperty<PropertyInt16>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyInt16.class;
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
			array.setValues(new ArrayList<PropertyInt16>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyInt16 prop = new PropertyInt16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyInt16 prop = new PropertyInt16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
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
}
