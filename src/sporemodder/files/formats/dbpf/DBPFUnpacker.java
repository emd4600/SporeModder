package sporemodder.files.formats.dbpf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import sporemodder.MainApp;
import sporemodder.files.ActionCommand;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.rast.RastToDDS;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.userinterface.dialogs.UIErrorsDialog;

public class DBPFUnpacker {
	
	public static final boolean CONVERT_PROP = true;
	public static final boolean CONVERT_RW4 = true;
	public static final boolean CONVERT_TLSA = true;
	public static final boolean CONVERT_PCTP = false;
	public static final boolean CONVERT_SPUI = false;
	public static final boolean CONVERT_EFFECTS = false;
	public static final boolean CONVERT_RAST = false;

	public static boolean processCommand(String[] args) {
		List<String> inputs = new ArrayList<String>();
		List<String> outputs = new ArrayList<String>();
		ActionCommand.parseDefaultArguments(args, inputs, outputs);
		
		List<ConvertAction> converters = new ArrayList<ConvertAction>();
		for (String s : args) {
			if (s.equals("-convert_spui")) {
				converters.add(new SpuiToTxt());
			} else if (s.equals("-convert_prop")) {
				converters.add(new PropToXml());
			} else if (s.equals("-convert_tlsa")) {
				converters.add(new TlsaToTxt());
			} else if (s.equals("-convert_pctp")) {
				converters.add(new PctpToTxt());
			} else if (s.equals("-convert_rw4")) {
				converters.add(new Rw4ToDDS());
			} else if (s.equals("-convert_effects")) {
				converters.add(new EffectUnpacker());
			} else if (s.equals("-convert_raster")) {
				converters.add(new RastToDDS());
			}
		}
		
		if (inputs.size() != 1) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "DBPF unpacker can only convert 1 file at the same time.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		File input = new File(inputs.get(0));
		
		String outputName = outputs.size() > 0 ? outputs.get(0) : null;
		if (outputName == null) {
			outputName = inputs.get(0) + ".unpacked";
		}
		
		File output = new File(outputName);
		if (!output.exists()) {
			output.mkdir();
		} else {
			if (!output.isDirectory()) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "DBPF unpacker output must be a folder.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		try {
			//TODO add converters
			
			new UnpackWindow(converters, input.getAbsolutePath(), output.getAbsolutePath());
		} catch (Exception e) {
			exceptions.put(input,  e);
		}
		
		if (exceptions.size() > 0) {
			new UIErrorsDialog(exceptions);
			return false;
		}


		return true;
	}
}
