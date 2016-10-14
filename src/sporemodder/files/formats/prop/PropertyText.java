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

public class PropertyText extends Property{
	private String value;
	private int tableId, instanceId;
	public static final String singularName = "text";
	public static final String pluralName = "texts";
	public static final int PROP_TYPE = 0x0022;
	public static final int itemSize = 520;
	
	public PropertyText(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyText(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyText(String name, String text, int tableId, int instanceId) throws IOException {
		super(name, PROP_TYPE);
		this.value = text;
		this.tableId = tableId;
		this.instanceId = instanceId;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			return "\t\t<text" + (tableId != 0 ? " tableid=\"" + Hasher.getFileName(tableId) + "\"" : "") 
					+ (instanceId != 0 ? " instanceid=\"" + Hasher.getFileName(instanceId) + "\"" : "")  + ">" + value + "</text>" + PROPMain.eol;
		} else {
			return "\t<text name=\"" + Hasher.getPropName(this.name) + " tableid=\"" + (tableId != 0 ? " tableid=\"" + Hasher.getFileName(tableId) + "\"" : "") + 
					(instanceId != 0 ? " instanceid=\"" + Hasher.getFileName(instanceId) + "\"" : "") + ">" + value + "</text>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			if ((size-8 & 1) == 1) {
				throw new IOException("PROP001; Text length is not divisible by two");
			}
			tableId = in.readLEInt();
			instanceId = in.readLEInt();
			byte[] arr = new byte[size-8];
			in.read(arr);
			int end = 0;
			for (int i = 0; i < size-8; i += 2) {
				if (arr[i] == 0 && arr[i + 1] == 0) {
					end = i;
					break;
				}
			}
			byte[] newArr = new byte[end];
			System.arraycopy(arr, 0, newArr, 0, end);
			
			value = new String(newArr, "UTF-16LE");
		} else {
			throw new IOException("PROP001; Non-array text is unimplemented");
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.setAttribute("tableid", Hasher.getFileName(tableId));
		elem.setAttribute("instanceid", Hasher.getFileName(instanceId));
		elem.appendChild(doc.createTextNode(value));
	}
	@Override
	public void readXML(Element elem) throws IOException {
		String tId = elem.getAttribute("tableid");
		tableId = tId.length() > 0 ? Hasher.getFileHash(tId) : 0;
		String iId = elem.getAttribute("instanceid");
		instanceId = iId.length() > 0 ? Hasher.getFileHash(iId) : 0;
		value = elem.getTextContent();
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		if (array) {
			out.writeLEInt(tableId);
			out.writeLEInt(instanceId);
			byte[] arr = value.getBytes("UTF-16LE");
			out.write(arr);
			if (arr.length < 512) {
				out.writePadding(512 - arr.length);
			}
		} else {
			throw new IOException("PROP001; Non-array text is unimplemented");
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
			ArrayProperty<PropertyText> array = new ArrayProperty<PropertyText>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyText.class;
			array.arrayItemSize = 520;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyText>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyText prop = new PropertyText(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			throw new IOException("PROP001; Non-array text is unimplemented");
		}
	}
	
	
	public void setText(String text) {
		this.value = text;
	}
	public void setLocale(int tableId, int instanceId) {
		this.tableId = tableId;
		this.instanceId = instanceId;
	}
	
	public String getText() {
		return value;
	}
	public int[] getLocale() {
		return new int[] {tableId, instanceId};
	}
}
