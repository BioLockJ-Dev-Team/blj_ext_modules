package basic;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import biolockj.Config;
import biolockj.Constants;
import biolockj.module.SeqModuleImpl;
import biolockj.util.BashScriptBuilder;
import biolockj.util.SeqUtil;

public class HelloSeqInput extends SeqModuleImpl
{

	@Override
	public void checkDependencies() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeTask() throws Exception
	{
		System.out.println( "Hello World wc!" );
		saveFile();
		BashScriptBuilder.buildScripts( this, buildScript( getInputFiles() ) );
	}
	
	private void saveFile() throws Exception
	{
		File myfile = new File(getOutputDir(), "myFile.txt");
		FileWriter fw = new FileWriter( myfile );
		fw.write( "Hello Seq file tester!" );
		fw.close();
	}

	@Override
	public List<List<String>> buildScript( List<File> files ) throws Exception
	{
		final List<List<String>> data = new ArrayList<>();
		for (File f:files) {
			List<String> inner = new ArrayList<>();
			File output = new File(getOutputDir(), f.getName() + "_wc.txt");
			inner.add( RUN_WC + " " + f.getAbsolutePath() + " " + output.getAbsolutePath());
			data.add( inner );
		}
		return data;
	}
	
	
	@Override
	public List<String> getWorkerScriptFunctions() throws Exception {
		final List<String> lines = super.getWorkerScriptFunctions();
		lines.add( "function " + RUN_WC + "() {" );
		lines.add("wc $1 > $2");
		lines.add( "}" + RETURN );
		return lines;
	}
	
	
	private static String RUN_WC = "runWC";
	

}
