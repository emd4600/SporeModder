package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.TextFileView;
import sporemodder.utilities.names.NameRegistry;

public class SpuiView extends HighlightedView {
	
	private static HashMap<Pattern, Color> patternColors = new HashMap<Pattern, Color>();
	private static final String[] TAGS_BLOCKS = new String[] {"block", "sections", "end"};
	private static final Color COLOR_BLOCK = new Color(128, 128, 64);
	private static final String[] TAGS_RESOURCES = new String[] {"StructResource", "FileResource", "ResourceType3"};
	private static final Color COLOR_RESOURCES = new Color(255, 128, 0);
	private static final String[] TAGS_TYPES = new String[] {"bool", "byte", "short", "int", "float", "vec2", 
		"vec4", "dimension", "text"};
	private static final Color COLOR_TYPES = new Color(128, 0, 0);
	private static final Color COLOR_VARIABLES = new Color(128, 128, 255);
	private static final Color COLOR_STRUCTS = new Color(255, 0, 0);

	public SpuiView(Element arg0, TextFileView textFileView) {
		super(arg0, textFileView);
		
		for (String s : TAGS_BLOCKS) {
			patternColors.put(Pattern.compile("\\s*(" + s + ")\\s+"), COLOR_BLOCK);
		}
		for (String s : TAGS_RESOURCES) {
			patternColors.put(Pattern.compile("\\s*(" + s + ")\\s+"), COLOR_RESOURCES);
		}
		for (String s : TAGS_TYPES) {
			patternColors.put(Pattern.compile("\\s+(" + s + ")\\s+"), COLOR_TYPES);
		}
		{
			Collection<String> pairs = MainApp.getRegistry(NameRegistry.NAME_SPUI).getGroup("SPUI_STRUCT").getNames();
			for (String p : pairs) {
				patternColors.put(Pattern.compile("\\s+(" + p + ")\\s+"), COLOR_STRUCTS);
			}
		}
		{
			Collection<String> pairs = MainApp.getRegistry(NameRegistry.NAME_SPUI).getGroup("SPUI_CHANNEL").getNames();
			for (String p : pairs) {
				patternColors.put(Pattern.compile("\\s+(" + p + ")\\s+"), COLOR_VARIABLES);
			}
			patternColors.put(Pattern.compile("\\s+(atlas)\\s+"), COLOR_RESOURCES);
			patternColors.put(Pattern.compile("\\s+(root)\\s+"), COLOR_RESOURCES);
			patternColors.put(Pattern.compile("\\s+(type)\\s+"), COLOR_RESOURCES);
		}
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
	
	public static class SpuiViewFactory implements ViewFactory {

		private TextFileView textFileView;
		public SpuiViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}

		
		@Override
		public View create(Element arg0) {
			return new SpuiView(arg0, textFileView);
		}
		
	}
	
	public static class SpuiEditorKit extends StyledEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8878195176046066538L;
	
		private ViewFactory spuiViewFactory;
		
		public SpuiEditorKit(TextFileView textFileView) {
			spuiViewFactory = new SpuiViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return spuiViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/spui_t";
		}
	}
}
