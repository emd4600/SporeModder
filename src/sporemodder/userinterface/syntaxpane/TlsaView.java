package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import sporemodder.userinterface.fileview.TextFileView;

public class TlsaView extends HighlightedView {
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
	private static final String[] TAGS_BLOCKS = new String[] {"group", "end", "anim"};
	private static final String[] TAGS_OPTIONS = new String[] {"version", "-endMode", "-idle", "-allowLocomotion", "-unk1", "-unk2",
		"-blendInTime", "-disableToolOverlay", "-matchVariantForTool", "-priorityOverride"};
	
	private static final Color COLOR_BLOCK = new Color(176, 176, 0);
	private static final Color COLOR_OPTIONS = new Color(255, 128, 0);
	
	static {
	    // NOTE: the order is important!
	    patternColors = new HashMap<Pattern, Color>();
	    
	    for (String s : TAGS_BLOCKS) {
	    	patternColors.put(Pattern.compile("\\s*(" + s + ")\\s+"), COLOR_BLOCK);
	    }
	    for (String s : TAGS_OPTIONS) {
	    	patternColors.put(Pattern.compile("\\s+(" + s + ")\\s+"), COLOR_OPTIONS);
	    }
	}
	
	
	public TlsaView(Element arg0, TextFileView textFileView) {
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
	
	public static class TlsaViewFactory implements ViewFactory {

		private TextFileView textFileView;
		public TlsaViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}
		
		@Override
		public View create(Element arg0) {
			return new TlsaView(arg0, textFileView);
		}
		
	}
	
	public static class TlsaEditorKit extends StyledEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8878195176046066538L;
	
		private ViewFactory tlsaViewFactory;
		
		public TlsaEditorKit(TextFileView textFileView) {
			tlsaViewFactory = new TlsaViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return tlsaViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/tlsa_t";
		}
	}
}
