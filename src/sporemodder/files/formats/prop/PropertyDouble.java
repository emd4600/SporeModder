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

public class PropertyDouble extends Property {
	private double value;
	public static final String singularName = "double";
	public static final String pluralName = "doubles";
	public static final int PROP_TYPE = 0x000E;
	public static final int itemSize = 4;
	
	public PropertyDouble(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyDouble(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyDouble(String name, double value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		NumberFormat nf = new DecimalFormat("#.#######");
		if (array) {
			return "\t\t<double>" + nf.format(value) + "</double>" + PROPMain.eol;
		} else {
			return "\t<double name=\"" + Hasher.getPropName(this.name) + "\">" + nf.format(value) + "</double>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readDouble();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Double.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Double.parseDouble(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeDouble(value);
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
			ArrayProperty<PropertyDouble> array = new ArrayProperty<PropertyDouble>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyDouble.class;
			array.arrayItemSize = 8;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyDouble>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyDouble prop = new PropertyDouble(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyDouble prop = new PropertyDouble(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
}
