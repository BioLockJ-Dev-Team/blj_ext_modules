package basic;

import java.io.File;
import java.io.FileWriter;
import biolockj.module.BioModuleImpl;

public class HelloBioModule extends BioModuleImpl
{

	@Override
	public void checkDependencies() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeTask() throws Exception
	{
		System.out.println( "Hello World!" );
		saveFile();
	}
	
	private void saveFile() throws Exception
	{
		File myfile = new File(getOutputDir(), "myFile.txt");
		FileWriter fw = new FileWriter( myfile );
		fw.write( "Hello File!" );
		fw.close();
	}
	
}
