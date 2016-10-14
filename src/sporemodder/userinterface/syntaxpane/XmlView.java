package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import sporemodder.userinterface.fileview.TextFileView;

public class XmlView extends HighlightedView {
	/**
	 * Thanks: http://groups.google.com/group/de.comp.lang.java/msg/2bbeb016abad270
	 * 
	 * IMPORTANT NOTE: regex should contain 1 group.
	 * 
	 * Using PlainView here because we don't want line wrapping to occur.
	 * 
	 * @author kees
	 * @date 13-jan-2006
	 *
	 */
	private static HashMap<Pattern, Color> patternColors;
	private static final String TAG_PATTERN = "(</?\\w*)\\s?>?";
	private static final String TAG_END_PATTERN = "(/>)";
	private static final String TAG_ATTRIBUTE_PATTERN = "\\s(\\w*)\\=";
	private static final String TAG_ATTRIBUTE_VALUE = "[a-z-]*\\=(\"[^\"]*\")"; // [a-z-]
    private static final String TAG_COMMENT = "(<!--.*-->)";
    private static final String TAG_CDATA_START = "(\\<!\\[CDATA\\[).*";
    private static final String TAG_CDATA_END = ".*(]]>)";
    
    static {
    	// NOTE: the order is important!
    	patternColors = new HashMap<Pattern, Color>();
    	patternColors.put(Pattern.compile(TAG_CDATA_START), new Color(0x808080));
    	patternColors.put(Pattern.compile(TAG_CDATA_END), new Color(0x808080));
    	patternColors.put(Pattern.compile(TAG_PATTERN), new Color(0x7F007F));
    	patternColors.put(Pattern.compile(TAG_ATTRIBUTE_PATTERN), new Color(0xFF0000));
    	patternColors.put(Pattern.compile(TAG_END_PATTERN), new Color(0x7F007F));
    	patternColors.put(Pattern.compile(TAG_ATTRIBUTE_VALUE), new Color(0x2A00FF));
    	patternColors.put(Pattern.compile(TAG_COMMENT), new Color(0x3F5FBF));
    }
    
	public XmlView(Element arg0, TextFileView textFileView) {
		super(arg0, textFileView);
	}
 
	@Override
	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0, int p1) throws BadLocationException {
		addRenderingHints(graphics);
		
		Document doc = getDocument();
		String text = doc.getText(p0, p1 - p0);
		
		
		Segment segment = getLineBuffer();
		
		SortedMap<Integer, Integer> searchStartMap = new TreeMap<Integer, Integer>();
		SortedMap<Integer, Integer> startMap = new TreeMap<Integer, Integer>();
		SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();
		
		
		processText(text, patternColors, searchStartMap, startMap, colorMap);
		
		int i = 0;
		// Colour the parts
		for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
			
			int start = entry.getKey();
			int end = entry.getValue();
			
			if (i < start) {
				graphics.setColor(Color.black);
				doc.getText(p0 + i, start - i, segment);
				x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
			}
			
			graphics.setColor(colorMap.get(start));
			i = end;
			doc.getText(p0 + start, i - start, segment);
			drawHighlightedWord(searchStartMap, segment, start, x, y, graphics);
			x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
		}
		
		// Paint possible remaining text black
		if (i < text.length()) {
			graphics.setColor(Color.black);
			doc.getText(p0 + i, text.length() - i, segment);
			x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
		}
		
		return x;
	}
	
	public static class XmlViewFactory implements ViewFactory {
		
		private TextFileView textFileView;
		
		public XmlViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}
		
		@Override
		public View create(Element element) {
			return new XmlView(element, textFileView);
		}

	}
	
	public static class XmlEditorKit extends StyledEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6148029492865754941L;

		private ViewFactory xmlViewFactory;
		
		public XmlEditorKit(TextFileView textFileView) {
			xmlViewFactory = new XmlViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return xmlViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/xml";
		}
	}

}
