package simple;

import java.io.File;
import java.io.FileWriter;
import biolockj.module.BioModuleImpl;

public class SimpleBioModule extends BioModuleImpl
{

	@Override
	public void checkDependencies() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeTask() throws Exception
	{
		saveFile();
	}
	
	private void saveFile() throws Exception
	{
		File myfile = new File(getOutputDir(), "itsHere.txt");
		FileWriter fw = new FileWriter( myfile );
		fw.write( "I did it!" );
		fw.close();
	}
	
}
