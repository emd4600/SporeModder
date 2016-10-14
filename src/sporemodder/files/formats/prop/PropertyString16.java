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

public class PropertyString16 extends Property {
	private String value;
	public static final String singularName = "string16";
	public static final String pluralName = "string16s";
	public static final int PROP_TYPE = 0x0013;
	public static final int itemSize = 16;
	
	public PropertyString16(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyString16(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyString16(String name, String text) throws IOException {
		super(name, PROP_TYPE);
		this.value = text;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			return "\t\t<string16>" + value + "</string16>" + PROPMain.eol;
		} else {
			return "\t<string16 name=\"" + Hasher.getPropName(this.name) + "\">" + value + "</string16>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		byte[] arr = new byte[in.readInt()*2];
		in.read(arr);
		value = new String(arr, "UTF-16LE");
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
		byte[] arr = value.getBytes("UTF-16LE");
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
		if (elem.getNodeName().length() > 8) {
			ArrayProperty<PropertyString16> array = new ArrayProperty<PropertyString16>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyString16.class;
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
			array.setValues(new ArrayList<PropertyString16>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyString16 prop = new PropertyString16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyString16 prop = new PropertyString16(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
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
}
