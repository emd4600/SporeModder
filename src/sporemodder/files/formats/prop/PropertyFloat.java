package sporemodder.files.formats.prop;

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

public class PropertyFloat extends Property {
	public static final String singularName = "float";
	public static final String pluralName = "floats";
	public static final int PROP_TYPE = 0x000D;
	private float value;
	public static final int itemSize = 4;
	
	public PropertyFloat(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyFloat(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyFloat(String name, float value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		NumberFormat nf = new DecimalFormat("#.#######");
		if (array) {
			return "\t\t<float>" + nf.format(value) + "</float>" + PROPMain.eol;
		} else {
			return "\t<float name=\"" + Hasher.getPropName(this.name) + "\">" + nf.format(value) + "</float>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readFloat();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Float.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Float.parseFloat(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeFloat(value);
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
			ArrayProperty<PropertyFloat> array = new ArrayProperty<PropertyFloat>(Hasher.getPropHash(elem.getAttribute("name")), 0x000D, 0x30);
			array.valueType = PropertyFloat.class;
			array.arrayItemSize = 4;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyFloat>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyFloat prop = new PropertyFloat(Hasher.getPropHash(elem.getAttribute("name")), 0x000D, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyFloat prop = new PropertyFloat(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
}
