package sporemodder.files.formats.dbpf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.prop.PROPMain;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.utilities.Project;
import sporemodder.utilities.performance.Profiler;
import sporemodder.utilities.performance.ProfilerGraph;

public class PerformanceTest {

	private static final String OUTPUT_PATH = "C:\\Users\\Eric\\Downloads\\ProfilerTest\\";
	
	private static final String TEST_SMALL = "ProfilerTestSmall";
	private static final String TEST_LARGE = "ProfilerTestLarge";
	private static final String TEST_XLARGE = "EP1_PatchData";
	
	private static final int NUM_TESTS = 2;
	
	public static final String PROFILER_GET_FOLDERS = "Get Folders";
	public static final String PROFILER_PROCESS_FOLDERS = "ProcessFolders";
	public static final String PROFILER_PROCESS_NAMES = "Process Names";
	public static final String PROFILER_CONVERT = "Conversions";
	public static final String PROFILER_EXTRA_FILES = "Extra Files";
	public static final String PROFILER_WRITE_INDEX = "Write Index";
	public static final String PROFILER_WRITE_FILE = "Write File";
	public static final String PROFILER_OTHER = "Other";
	public static final String PROFILER_GENERAL = "Total Time";
	
	public static final String[] PROFILES = new String[] {
			PROFILER_GET_FOLDERS, PROFILER_PROCESS_NAMES, PROFILER_CONVERT, PROFILER_EXTRA_FILES, 
			PROFILER_WRITE_INDEX, PROFILER_WRITE_FILE, PROFILER_OTHER};
	
	
	public static void main(String[] args) throws Exception {
		MainApp.redirectConsole = false;
		MainApp.init();
		MainApp.loadProjects();
		
		Project project = MainApp.getProjectByName(TEST_XLARGE);
		
		List<ConvertAction> converters = new ArrayList<ConvertAction>();
		
		converters.add(new XmlToProp());
		
		PROPMain.profiler = new Profiler();
		
		
		Profiler oldProfiler = new Profiler();
		Profiler newProfiler = new Profiler();
		
		// first do one attempt so everything loads
		// the first one is usually slower
		try (FileStreamAccessor out = new FileStreamAccessor(new File(new File(OUTPUT_PATH), project.getPackageName()), "rw", true))
		{
			DBPFPackingTaskOptimized test = new DBPFPackingTaskOptimized(out, project, converters, null);
			test.bConvertInMemory = true;
			test.bUseFastConverters = true;
			test.execute();
		}
		
		long totalTime = 0;
		
		
		for (int i = 0; i < NUM_TESTS; i++) {
			DBPFPackingTaskOptimized newTest;
			
			newProfiler.startMeasure(PROFILER_GENERAL);
			
			try (FileStreamAccessor out = new FileStreamAccessor(new File(new File(OUTPUT_PATH), project.getPackageName()), "rw", true))
			{
				newTest = new DBPFPackingTaskOptimized(out, project, converters, null);
				newTest.bConvertInMemory = true;
				newTest.bUseFastConverters = true;
				newTest.execute();
			}
			
			newProfiler.endMeasure(PROFILER_GENERAL);
			
			newProfiler.addProfiler(newTest.getProfiler());
		}
		
		totalTime = newProfiler.getTotalTime();
		newProfiler.addTime(PROFILER_OTHER, newProfiler.getTotalTime(PROFILER_GENERAL) - (totalTime - newProfiler.getTotalTime(PROFILER_GENERAL)));
		
		for (int i = 0; i < NUM_TESTS; i++) {
			
//			DBPFPackingTaskOptimizedOld oldTest;
			DBPFPackingTaskOptimized oldTest;
			
			oldProfiler.startMeasure(PROFILER_GENERAL);
			
//			try (DBPFMain dbpf = new DBPFMain(new FileStreamAccessor(new File(new File(OUTPUT_PATH), project.getPackageName()), "rw", true), false))
//			{
////				 oldTest = new DBPFPackingTaskOptimizedOld(dbpf, project, converters, null);
//				oldTest = new DBPFPackingTaskOptimized(dbpf.source, project, converters, null);
//				oldTest.bConvertInMemory = true;
//				oldTest.bUseFastConverters = true;
//				oldTest.execute();
//				
//			}
			
			try (FileStreamAccessor out = new FileStreamAccessor(new File(new File(OUTPUT_PATH), project.getPackageName()), "rw", true))
			{
				oldTest = new DBPFPackingTaskOptimized(out, project, converters, null);
				oldTest.bConvertInMemory = true;
				oldTest.bUseFastConverters = false;
				oldTest.execute();
			}
			
			oldProfiler.endMeasure(PROFILER_GENERAL);
			
			oldProfiler.addProfiler(oldTest.getProfiler());
		}
		
		totalTime = oldProfiler.getTotalTime();
		oldProfiler.addTime(PROFILER_OTHER, oldProfiler.getTotalTime(PROFILER_GENERAL) - (totalTime - oldProfiler.getTotalTime(PROFILER_GENERAL)));
		
//		new Thread() {
//			@Override
//			public void run() {
//				if (PROPMain.profiler != null) {
//					PerformanceViewer propViewer = new PerformanceViewer(PROPMain.profiler, null, PROPMain.PROFILES, null);
//					propViewer.setTitle("XML to PROP Conversion Profiling");
//					propViewer.setVisible(true);
//				}
//			}
//		}.start();
		
		
		PerformanceViewer dbpfViewer = new PerformanceViewer(oldProfiler, newProfiler, PROFILES, PROFILER_GENERAL);
		dbpfViewer.setVisible(true);
	}
	
	public static void showProfilerGraph(Profiler profiler, String[] profiles) {
		
		JFrame frame = new JFrame();
		frame.setSize(450, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		
		
		ProfilerGraph graph = new ProfilerGraph(profiler);
		graph.setPreferredSize(new Dimension(400, 400));
		graph.setSize(new Dimension(400, 400));
		graph.setMaximumSize(new Dimension(400, 400));
		graph.setProfiles(profiles);
		
		container.add(Box.createHorizontalGlue());
		container.add(graph);
		container.add(Box.createHorizontalGlue());
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(container, BorderLayout.CENTER);
		
		
		frame.setVisible(true);
	}
}


