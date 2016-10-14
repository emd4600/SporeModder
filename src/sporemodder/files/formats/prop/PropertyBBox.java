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

public class PropertyBBox extends Property {
	public static final String singularName = "bbox";
	public static final String pluralName = "bboxes";
	public static final int PROP_TYPE = 0x0039;
	public static final int itemSize = 24;
	
	private float min_x, min_y, min_z, max_x, max_y, max_z;
	public PropertyBBox(int name, int type, int flags) throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
	}
	public PropertyBBox(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyBBox(String name, float[] bbox) throws IOException {
		super(name, PROP_TYPE);
		min_x = bbox[0];
		min_y = bbox[1];
		min_z = bbox[2];
		max_x = bbox[3];
		max_y = bbox[4];
		max_z = bbox[5];
	}
	
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			min_x = in.readLEFloat();
			min_y = in.readLEFloat();
			min_z = in.readLEFloat();
			max_x = in.readLEFloat();
			max_y = in.readLEFloat();
			max_z = in.readLEFloat();
		} else {
			throw new IOException("PROP001; Non-array bbox is unimplemented");
		}
	}
	@Override
	public String toString(boolean array) throws IOException {
		NumberFormat nf = new DecimalFormat("#.#######");
		if (array) {
			return "\t\t<bbox>" + PROPMain.eol + 
					"\t\t\t<min>" + nf.format(min_x) + ", " + nf.format(min_y) + ", " + nf.format(min_z) + "</min>" + PROPMain.eol +
					"\t\t\t<max>" + nf.format(max_x) + ", " + nf.format(max_y) + ", " + (max_z) + "</max>" + PROPMain.eol +
					"\t\t</bbox>" + PROPMain.eol;
		} else {
			throw new IOException("PROP001; Non-array bbox is unimplemented");
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		Element elemMin = doc.createElement("min");
		elemMin.appendChild(doc.createTextNode(Float.toString(min_x) + ", " + Float.toString(min_y) + ", " + Float.toString(min_z)));
		Element elemMax = doc.createElement("max");
		elemMin.appendChild(doc.createTextNode(Float.toString(max_x) + ", " + Float.toString(max_y) + ", " + Float.toString(max_z)));
		
		elem.appendChild(elemMin);
		elem.appendChild(elemMax);
	}
	@Override
	public void readXML(Element elem) {
		NodeList nl = elem.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("min")) {
				String[] splits = item.getTextContent().split(", ");
				min_x = Float.parseFloat(splits[0]);
				min_y = Float.parseFloat(splits[1]);
				min_z = Float.parseFloat(splits[2]);
			} else if (item.getNodeName().equals("max")) {
				String[] splits = item.getTextContent().split(", ");
				min_x = Float.parseFloat(splits[0]);
				max_y = Float.parseFloat(splits[1]);
				max_z = Float.parseFloat(splits[2]);
			}
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		if (array) {
			out.writeLEFloat(min_x);
			out.writeLEFloat(min_y);
			out.writeLEFloat(min_z);
			out.writeLEFloat(max_x);
			out.writeLEFloat(max_y);
			out.writeLEFloat(max_z);
		} else {
			throw new IOException("PROP001; Non-array bbox is unimplemented");
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
		if (elem.getNodeName().length() > 4) {
			ArrayProperty<PropertyBBox> array = new ArrayProperty<PropertyBBox>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyBBox.class;
			array.arrayItemSize = 24;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyBBox>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyBBox prop = new PropertyBBox(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			throw new IOException("PROP001; Non-array bbox is unimplemented");
		}
	}
	
	
	public void setMin(float x, float y, float z) {
		min_x = x;
		min_y = y;
		min_z = z;
	}
	public void setMax(float x, float y, float z) {
		max_x = x;
		max_y = y;
		max_z = z;
	}
	
	public float[] getMin() {
		return new float[] {min_x, min_y, min_z};
	}
	public float[] getMax() {
		return new float[] {max_x, max_y, max_z};
	}
}
