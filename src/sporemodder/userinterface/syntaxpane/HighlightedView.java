package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
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
import javax.swing.text.Utilities;

import sporemodder.userinterface.fileview.TextFileView;

public abstract class HighlightedView extends PlainView {
	
	protected static Map<?, ?> desktopHints = null;
	
	protected TextFileView textFileView;
	private HashMap<Pattern, Color> patternColors;
	
	public HighlightedView(Element arg0, TextFileView textFileView) {
		this(arg0, textFileView, null);
	}

	public HighlightedView(Element arg0, TextFileView textFileView, HashMap<Pattern, Color> patternColors) {
		super(arg0);

		this.textFileView = textFileView;
		this.patternColors = patternColors;
		
		getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
		
		if (desktopHints == null) { 
		    Toolkit tk = Toolkit.getDefaultToolkit(); 
		    desktopHints = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints")); 
		}
	}

	protected static boolean mapContainsWord(SortedMap<Integer, Integer> startMap, int start, int end) {
		for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
			int wordStart = entry.getKey();
			int wordEnd = entry.getValue();
			// if ((start >= wordStart && start < wordEnd) || (end <= wordEnd && end > wordStart)) {
			// if ((wordStart >= start && wordStart < end) || (wordEnd <= end && wordEnd > start)) {
			// if (wordStart >= start && wordEnd <= end) {
			if (start >= wordStart && end <= wordEnd) {
				return true;
			}
		}
		return false;
	}
	
	protected static boolean wordContainsMap(SortedMap<Integer, Integer> startMap, int start, int end) {
		for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
			int wordStart = entry.getKey();
			int wordEnd = entry.getValue();
			// if ((start >= wordStart && start < wordEnd) || (end <= wordEnd && end > wordStart)) {
			// if ((wordStart >= start && wordStart < end) || (wordEnd <= end && wordEnd > start)) {
			// if (wordStart >= start && wordEnd <= end) {
			if (wordStart >= start && wordEnd <= end) {
				return true;
			}
		}
		return false;
	}
	
	protected void processSearchedWords(String text, SortedMap<Integer, Integer> searchStartMap, 
			SortedMap<Integer, Integer> startMap,
			SortedMap<Integer, Color> colorMap) {
		
		if (textFileView == null) {
			return;
		}
		
		List<String> searchStrings = textFileView.getSearchTerms();
		if (searchStrings != null) {
			for (String str : searchStrings) {
				Matcher matcher = Pattern.compile(str, textFileView.isSearchCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE).matcher(text);
				
				while (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					// to avoid rendering twice with multiple search words
					if (!mapContainsWord(searchStartMap, start, end) && !wordContainsMap(searchStartMap, start, end)) {
						searchStartMap.put(start, end);
						startMap.put(start, end);
						colorMap.put(start, TextFileView.SEARCH_COLOR);
					}
				}
			}
		}
	}
	
	protected void processSearchedWords(String text, 
			SortedMap<Integer, Integer> searchStartMap, 
			SortedMap<Integer, Integer> startMap,
			SortedMap<Integer, Color> colorMap,
			int commentIndex) {
		
		if (textFileView == null) {
			return;
		}
		
		List<String> searchStrings = textFileView.getSearchTerms();
		if (searchStrings != null) {
			for (String str : searchStrings) {
				Matcher matcher = Pattern.compile(str, textFileView.isSearchCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE).matcher(text);
				
				while (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					// to avoid rendering twice with multiple search words
					if (commentIndex != -1) {
						if (start >= commentIndex) {
							// the word is inside a comment
							// we have to remove the actual index and add two news: (commentIndex, start), (end, commentEnd)
							int closerCommentIndex = commentIndex;
							for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
								int commentStart = entry.getKey();
								// if entry is a comment && entry is closer to the word than the last we found
								if (commentStart >= commentIndex && commentStart >= closerCommentIndex && commentStart < start) {
									closerCommentIndex = commentStart;
								} else {
									break;
								}
							}
							Color commentColor = colorMap.get(closerCommentIndex);
							int commentEnd = startMap.get(closerCommentIndex);
							startMap.put(closerCommentIndex, start);
							startMap.put(end, commentEnd);
							colorMap.put(end, commentColor);
						} else if (end > commentIndex) {
							// the word is outside and inside the comment
							// we have to remove the actual index and add a new one: (start, commentEnd)
							Color commentColor = colorMap.get(commentIndex);
							int commentEnd = startMap.get(commentIndex);
							startMap.remove(commentIndex);
							startMap.put(end, commentEnd);
							colorMap.put(end, commentColor);
						}
						searchStartMap.put(start, end);
						startMap.put(start, end);
						colorMap.put(start, TextFileView.SEARCH_COLOR);
					} 
					else if (!mapContainsWord(searchStartMap, start, end) && !wordContainsMap(searchStartMap, start, end)) {
						
						searchStartMap.put(start, end);
						startMap.put(start, end);
						colorMap.put(start, TextFileView.SEARCH_COLOR);
					}
				}
			}
		}
	}
	
	protected void drawHighlightedWord(SortedMap<Integer, Integer> searchStartMap, Segment segment, int start, int x, int y, Graphics graphics) {
		if (searchStartMap.containsKey(start)) {
			// highlight word
			Rectangle2D bounds = metrics.getStringBounds(segment.array, segment.offset, segment.offset + segment.count, graphics);
			// get descent/ascent ? 
			int height = (int) bounds.getHeight();
			//graphics.fillRect(x, y - height, (int) bounds.getWidth(), height);
			
			graphics.fillRect(x, y - metrics.getAscent(), (int) bounds.getWidth(), height);
			
			//graphics.setColor(Color.black);
			graphics.setColor(Color.white);
		}
	}
	
	protected void addRenderingHints(Graphics graphics) {
		if (desktopHints != null) { 
		    ((Graphics2D)graphics).addRenderingHints(desktopHints); 
		}
	}
	
	protected void processText(
			String text,
			HashMap<Pattern, Color> patternColors,
			SortedMap<Integer, Integer> searchStartMap, 
			SortedMap<Integer, Integer> startMap,
			SortedMap<Integer, Color> colorMap) {
		
		int commentIndex = text.indexOf(TextFileView.COMMENT_KEYWORD);
		if (commentIndex != -1) {
			startMap.put(commentIndex, text.length());
			colorMap.put(commentIndex, TextFileView.COMMENT_COLOR);
		}
		
		// first search the searched words
		processSearchedWords(text, searchStartMap, startMap, colorMap, commentIndex);
		
		// match all regexes on this snippet, store positions
		for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {
			
			if (entry.getKey().pattern().equals("\\W(mul)\\(")) {
				System.out.print("BREAKPOINT");
			}
			
			Matcher matcher = entry.getKey().matcher(text);
			
			
			while (matcher.find()) {
				int start = matcher.start(1);
				int end = matcher.end(1);
				// check if the word contains a searched word; if so, don't add it to the list
				if (!mapContainsWord(searchStartMap, start, end) && !wordContainsMap(searchStartMap, start, end) && (commentIndex == -1 || end <= commentIndex)) {
					startMap.put(start, end);
					colorMap.put(start, entry.getValue());
				}
				
				// Some regex need to use characters that belong to the previous regex (but that aren't highlighted)
				// We set the matcher to start at the end of the group instead of at the end of the regex
				matcher.region(end, matcher.regionEnd());
			}
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
}
