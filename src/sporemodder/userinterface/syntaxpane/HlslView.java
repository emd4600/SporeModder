package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import sporemodder.userinterface.fileview.TextFileView;

public class HlslView extends HighlightedView {
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
	
	private static final String[] TAGS_ENUMS = new String[] {
			// Vertex Shader input semantics
			"BINORMAL", "BINORMAL0", "BINORMAL1", "BINORMAL2", "BINORMAL3", "BINORMAL4", 
			"BLENDINDICES", "BLENDINDICES0", "BLENDINDICES1", "BLENDINDICES2", "BLENDINDICES3", "BLENDINDICES4", 
			"BLENDWEIGHT", "BLENDWEIGHT0", "BLENDWEIGHT1", "BLENDWEIGHT2", "BLENDWEIGHT3", "BLENDWEIGHT4", 
			"COLOR", "COLOR0", "COLOR1", "COLOR2", "COLOR3", "COLOR4", 
			"NORMAL", "NORMAL0", "NORMAL1", "NORMAL2", "NORMAL3", "NORMAL4", 
			"POSITION", "POSITION0", "POSITION1", "POSITION2", "POSITION3", "POSITION4", "POSITIONT",
			"PSIZE", "PSIZE0", "PSIZE1", "PSIZE2", "PSIZE3", "PSIZE4", 
			"TANGENT", "TANGENT0", "TANGENT1", "TANGENT2", "TANGENT3", "TANGENT4", 
			"TESSFACTOR", "TESSFACTOR0", "TESSFACTOR1", "TESSFACTOR2", "TESSFACTOR3", "TESSFACTOR4", 
			"TEXCOORD", "TEXCOORD0", "TEXCOORD1", "TEXCOORD2", "TEXCOORD3", "TEXCOORD4",
			// Vertex Shader output Symantics
			"FOG", "PSIZE",
			// Pixel Shader input Symantics
			"VFACE", "VPOS",
			// Pixel Shader output Symantics
			"DEPTH", "DEPTH0", "DEPTH1", "DEPTH2", "DEPTH3", "DEPTH4", "DEPTH5"
			};
	
	private static final String[] TAGS_TYPES = new String[] {
			// Basic types
			"BOOL", "bool", "int", "half", "float", "double", "sampler", "struct", "string",
			"sampler1D", "sampler2D", "sampler3D", "samplerCUBE",
			// Vector and matrix types
			"bool1", "bool2", "bool3", "bool4", 
			"BOOL1", "BOOL2", "BOOL3", "BOOL4",
			"int1", "int2", "int3", "int4", 
			"half1", "half2", "half3", "half4", 
			"float1", "float2", "float3", "float4", 
			"double1", "double2", "double3", "double4", 
			"vector", 
			"bool1x1", "bool1x2", "bool1x3", "bool1x4", 
			"bool2x1", "bool2x2", "bool2x3", "bool2x4", 
			"bool3x1", "bool3x2", "bool3x3", "bool3x4", 
			"bool4x1", "bool4x2", "bool4x3", "bool4x4", 
			"BOOL1x1", "BOOL1x2", "BOOL1x3", "BOOL1x4", 
			"BOOL2x1", "BOOL2x2", "BOOL2x3", "BOOL2x4", 
			"BOOL3x1", "BOOL3x2", "BOOL3x3", "BOOL3x4", 
			"BOOL4x1", "BOOL4x2", "BOOL4x3", "BOOL4x4", 
			"half1x1", "half1x2", "half1x3", "half1x4", 
			"half2x1", "half2x2", "half2x3", "half2x4", 
			"half3x1", "half3x2", "half3x3", "half3x4", 
			"half4x1", "half4x2", "half4x3", "half4x4", 
			"int1x1", "int1x2", "int1x3", "int1x4", 
			"int2x1", "int2x2", "int2x3", "int2x4", 
			"int3x1", "int3x2", "int3x3", "int3x4", 
			"int4x1", "int4x2", "int4x3", "int4x4", 
			"float1x1", "float1x2", "float1x3", "float1x4", 
			"float2x1", "float2x2", "float2x3", "float2x4", 
			"float3x1", "float3x2", "float3x3", "float3x4", 
			"float4x1", "float4x2", "float4x3", "float4x4", 
			"double1x1", "double1x2", "double1x3", "double1x4", 
			"double2x1", "double2x2", "double2x3", "double2x4", 
			"double3x1", "double3x2", "double3x3", "double3x4", 
			"double4x1", "double4x2", "double4x3", "double4x4", 
			"matrix", "vertexshader", "pixelshader"
	};
	
	private static final String[] TAGS_KEYWORDS = new String[] {
			"extern", "shared", "static", "uniform", "volatile", "const", "row_major", "column_major", 
			"pack_matrix", "warning", "def", "once", "default", "disable", "error", 
			"vs", "vs_1_1", "vs_2_0", "vs_2_a", "ps", "ps_1_1", "ps_1_2", "ps_1_3", "ps_1_4", "ps_2_0", "ps_2_a", 
			"__FILE__", "__LINE__", "asm", "asm_fragment", "compile", "compile_fragment", "discard", "decl", 
			"do", "else", "false", "for", "if", "in", "inline", "inout", "out", 
			"pass", "pixelfragment", "return", "register", "sampler_state", "shared", "stateblock", "stateblock_state", 
			"technique", "true", "typedef", "uniform", "vertexfragment", "void", "volatile", "while",
			// Methods used by ModAPI's custom shaders
			"VS_main", "PS_main"
	};
	
	private static final String[] TAGS_FUNCTIONS = new String[] {
			"abs", "acos", "all", "any", "asin", "atan", "atan2", "ceil", "clamp", "clip", "cos", "cosh", "cross", 
			"D3DCOLORtoUBYTE4", "ddx", "ddy", "degrees", "determinant", "distance", "dot", "exp", "exp2", "faceforward", 
			"floor", "fmod", "frac", "frexp", "fwidth", "isfinite", "isinf", "isnan", "ldexp", "length", "lerp", "lit", 
			"log", "log10", "log2", "max", "min", "modf", "mul", "noise", "normalize", "pow", "radians", "reflect", 
			"refract", "round", "rsqrt", "saturate", "sign", "sin", "sincos", "sinh", "smoothstep", "sqrt", "step", 
			"tan", "tanh", "tex1D", "tex1D", "tex1Dbias", "tex1Dgrad", "tex1Dlod", "tex1Dproj", 
			"tex2D", "tex2D", "tex2Dbias", "tex2Dgrad", "tex2Dlod", "tex2Dproj", "tex3D", "tex3D", 
			"tex3Dbias", "tex3Dgrad", "tex3Dlod", "tex3Dproj", 
			"texCUBE", "texCUBE", "texCUBEbias", "texCUBEgrad", "texCUBElod", "texCUBEproj", "transpose"
	};
	
	private static final Color COLOR_ENUMS = new Color(186, 85, 211);
	private static final Color COLOR_NUMBERS = new Color(95, 158, 160);
	private static final Color COLOR_TYPES = new Color(255, 0, 0);
	private static final Color COLOR_KEYWORDS = new Color(165, 42, 42);
	private static final Color COLOR_FUNCTIONS = new Color(0, 0, 255);
	
	static {
	    // NOTE: the order is important!
	    patternColors = new HashMap<Pattern, Color>();
	    
	    patternColors.put(Pattern.compile("\\W+(\\d*\\.?\\d*)\\W"), COLOR_NUMBERS);
	    
	    for (String s : TAGS_ENUMS) {
	    	patternColors.put(Pattern.compile("\\W+(" + s + ")\\W"), COLOR_ENUMS);
	    }
	    for (String s : TAGS_TYPES) {
	    	patternColors.put(Pattern.compile("^(" + s + ")\\W"), COLOR_TYPES);
	    	patternColors.put(Pattern.compile("\\W(" + s + ")\\W"), COLOR_TYPES);
	    }
	    for (String s : TAGS_KEYWORDS) {
	    	patternColors.put(Pattern.compile("^(" + s + ")\\W"), COLOR_KEYWORDS);
	    	patternColors.put(Pattern.compile("\\W+(" + s + ")\\W"), COLOR_KEYWORDS);
	    }
	    for (String s : TAGS_FUNCTIONS) {
	    	patternColors.put(Pattern.compile("\\W(" + s + ")\\("), COLOR_FUNCTIONS);
	    }
	    
//	    for (String s : TAGS_COMMANDS) {
//	    	patternColors.put(Pattern.compile("^\\s+(" + s + ")\\s+"), COLOR_COMMANDS);
//	    }
//	    for (String s : TAGS_BLOCKS) {
//	    	patternColors.put(Pattern.compile("^(" + s + ")\\s+"), COLOR_BLOCKS);
//	    }
//	    for (String s : TAGS_OPTIONS) {
//	    	patternColors.put(Pattern.compile("\\s+(-" + s + ")\\s+"), COLOR_OPTIONS);
//	    }
	}
	
	
	public HlslView(Element arg0, TextFileView textFileView) {
		super(arg0, textFileView, patternColors);
	}

	
	public static class HlslViewFactory implements ViewFactory {

		private TextFileView textFileView;
		public HlslViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}
		
		@Override
		public View create(Element arg0) {
			return new HlslView(arg0, textFileView);
		}
		
	}
	
	public static class HlslEditorKit extends StyledEditorKit {
	
		private ViewFactory hlslViewFactory;
		
		public HlslEditorKit(TextFileView textFileView) {
			hlslViewFactory = new HlslViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return hlslViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/hlsl";
		}
	}
}
