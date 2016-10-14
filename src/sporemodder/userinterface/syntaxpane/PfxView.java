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

import sporemodder.MainApp;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.effects.*;
import sporemodder.userinterface.fileview.TextFileView;

public class PfxView extends HighlightedView {
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
	
	private static class Pair<K, V> {
		K key;
		V value;
		Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		// only use key to generate so we don't have repeated patterns
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}		
		
	}
	private static List<Pair<Pattern, Color>> patternColors;
	
	private static final Color COLOR_BLOCKS = new Color(176, 176, 0);
	private static final Color COLOR_COMMANDS = new Color(128, 128, 64);
	private static final Color COLOR_ENUMS = new Color(255, 128, 0);
	private static final Color COLOR_OPTIONS = new Color(128, 0, 0);
	
	static {
	    // NOTE: the order is important!
	    patternColors = new ArrayList<Pair<Pattern, Color>>();
	    
	    for (Class<?> clazz : EffectMain.SUPPORTED_SH_STRUCTS) {
	    	
	    	//TODO 'attract' doesn't work!
	    	String[] enums = EffectComponent.getEnumTags(clazz);
	    	for (String s : enums) {
	    		patternColors.add(new Pair<Pattern, Color>(Pattern.compile("\\s+(" + s + ")\\s+"), COLOR_ENUMS));
	    	}
	    }
	    for (Class<?> clazz : EffectMain.SUPPORTED_SH_STRUCTS) {
	    	// commands must be done after enums?
	    	String[] commands = EffectComponent.getCommandTags(clazz);
	    	for (String s : commands) {
	    		//patternColors.put(Pattern.compile("\\s+(" + s + ")\\s+"), COLOR_COMMANDS);
	    		patternColors.add(new Pair<Pattern, Color>(Pattern.compile("^\\s+(" + s + ")\\s+"), COLOR_COMMANDS));
	    	}
	    }
	    for (Class<?> clazz : EffectMain.SUPPORTED_SH_STRUCTS) {	
	    	String[] blocks = EffectComponent.getBlockTags(clazz);
	    	for (String s : blocks) {
	    		//patternColors.put(Pattern.compile("\\s*(" + s + ")\\s+"), COLOR_BLOCKS);
	    		patternColors.add(new Pair<Pattern, Color>(Pattern.compile("^(" + s + ")\\s+"), COLOR_BLOCKS));
	    		// They must be added as commands too
	    		patternColors.add(new Pair<Pattern, Color>(Pattern.compile("^\\s+(" + s + ")\\s+"), COLOR_COMMANDS));
	    	}
	    }
	    for (Class<?> clazz : EffectMain.SUPPORTED_SH_STRUCTS) {	
	    	String[] options = EffectComponent.getOptionTags(clazz);
	    	for (String s : options) {
	    		patternColors.add(new Pair<Pattern, Color>(Pattern.compile("\\s+(-" + s + ")\\s+"), COLOR_OPTIONS));
	    	}
	    }
	    
	    //patternColors.put(Pattern.compile("\\s*(" + ArgScript.END_KEYWORD + ")\\s+"), COLOR_BLOCKS);
	    patternColors.add(new Pair<Pattern, Color>(Pattern.compile("^\\s*(" + ArgScript.END_KEYWORD + ")\\s+"), COLOR_BLOCKS));
	}
	
	
	public PfxView(Element arg0, TextFileView textFileView) {
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
		
		// first search the searched words
		processSearchedWords(text, searchStartMap, startMap, colorMap);
		
		int commentIndex = text.indexOf(TextFileView.COMMENT_KEYWORD);
		if (commentIndex != -1) {
			startMap.put(commentIndex, text.length());
			colorMap.put(commentIndex, TextFileView.COMMENT_COLOR);
		}
		
		// match all regexes on this snippet, store positions
		for (Pair<Pattern, Color> entry : patternColors) {
			
			Matcher matcher = entry.key.matcher(text);
			
			while (matcher.find()) {
				int start = matcher.start(1);
				int end = matcher.end(1);
				// check if the word contains a searched word; if so, don't add it to the list
				if (!mapContainsWord(searchStartMap, start, end) && !wordContainsMap(searchStartMap, start, end) && (commentIndex == -1 || end <= commentIndex)) {
					startMap.put(start, end);
					colorMap.put(start, entry.value);
				}
			}
		}
		
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
	
	public static class PfxViewFactory implements ViewFactory {

		private TextFileView textFileView;
		
		public PfxViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}

		
		@Override
		public View create(Element arg0) {
			return new PfxView(arg0, textFileView);
		}
		
	}
	
	public static class PfxEditorKit extends StyledEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8878195176046066538L;
	
		private ViewFactory pfxViewFactory;
		
		public PfxEditorKit(TextFileView textFileView) {
			pfxViewFactory = new PfxViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return pfxViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/pfx";
		}
	}
}
