package sporemodder.files.formats.dbpf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.ActionCommand;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.pctp.TxtToPctp;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.spui.TxtToSpui;
import sporemodder.files.formats.tlsa.TxtToTlsa;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.userinterface.dialogs.UIErrorsDialog;

public class DBPFPacker {
	
	public static boolean processCommand(List<String> inputs, List<String> outputs) {
		List<ConvertAction> converters = new ArrayList<ConvertAction>();
		converters.add(new XmlToProp());
		converters.add(new TxtToSpui());
		converters.add(new TxtToTlsa());
		converters.add(new TxtToPctp());
		converters.add(new EffectPacker());
		converters.add(new DDSToRw4());
		converters.add(new DDStoRast());
		
		if (inputs.size() != 1) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "DBPF packer can only convert 1 file at the same time.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		String inputName = inputs.get(0);
		File input = new File(inputName);
		
		String outputName = outputs.size() > 0 ? outputs.get(0) : null;
		if (outputName == null) {
			if (inputName.endsWith(".unpacked")) {
				outputName = inputName.substring(0, inputName.indexOf(".unpacked"));
			} else {
				outputName = inputName + ".package";
			}
		}
		
		File output = new File(outputName);
		if (!input.isDirectory()) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "DBPF packer input must be a folder.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		try {
			new PackWindow(input, output, converters);
		} catch (Exception e) {
			exceptions.put(input,  e);
		}
		
		if (exceptions.size() > 0) {
			new UIErrorsDialog(exceptions);
			return false;
		}


		return true;
	}

	public static boolean processCommand(String[] args) {
		List<String> inputs = new ArrayList<String>();
		List<String> outputs = new ArrayList<String>();
		ActionCommand.parseDefaultArguments(args, inputs, outputs);
		
		return processCommand(inputs, outputs);
	}
	
	public static boolean processCommandFindOutput(String[] args) {
		List<String> inputs = new ArrayList<String>();
		List<String> outputs = new ArrayList<String>();
		ActionCommand.parseDefaultArguments(args, inputs, outputs);
		
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, null, JFileChooser.FILES_ONLY, false, ChooserType.SAVE, new FileNameExtensionFilter("Spore Database Packed File (*.package)", "package"));
		String result = chooser.launch();
		outputs.clear();
		outputs.add(result);
		
		return processCommand(inputs, outputs);
	}
}
