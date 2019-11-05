package prune;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import biolockj.*;
import biolockj.module.*;
import biolockj.util.*;

/**
 * Run FastQC to assess input sequence quality. This module is most efficient if
 * {@link biolockj.Config}.{@value this.SCRIPT_BATCH_SIZE} is 1 and 
 * {@link biolockj.Config}.{@value this.NUM_THREADS_PARAM} is a large 
 * number that evenly divides the number of input files.
 * 
 * @author ivory
 *
 */
public class FastQC extends SeqModuleImpl implements SeqModule, ScriptModule
{

	/**
	 * Build the script lines for each worker script as a nested list.
	 * With n threads, FasQC can process the n files at a time when they 
	 * are passed with a single command (that is, one script line).
	 *
	 * @param files List of module input files
	 * @return List of nested bash script lines.
	 * @throws Exception if error occurs generating bash script lines
	 */
	@Override
	public List<List<String>> buildScript( List<File> files ) throws Exception
	{
		final List<List<String>> data = new ArrayList<>();
		int batchSize = Config.requirePositiveInteger( this, Constants.SCRIPT_NUM_THREADS );

		if( DockerUtil.inDockerEnv() )//TODO - docker mode doesn't always mean it should only have one batch
		{
			batchSize = files.size();
		}

		int fullBatches = files.size() / batchSize;
		int startingIndex = 0;
		int endingIndex;

		for( int i = 0; i < fullBatches; i++ )
		{
			endingIndex = startingIndex + batchSize;
			List<String> lines = buildFastqcLine( files.subList( startingIndex, endingIndex ) );
			data.add( lines );
			startingIndex = endingIndex;
		}
		if( files.size() % batchSize > 0 )
		{
			List<String> lines = buildFastqcLine( files.subList( startingIndex, files.size() ) );
			data.add( lines );
		}
		return data;
	}

	/**
	 * Create a single line of a script to run FastQC on one or more files. When FastQC is n-threaded it processes n
	 * files at a time with one command line.
	 * 
	 * @param files Sequence files to process
	 * @return List with one String
	 */
	private List<String> buildFastqcLine( List<File> files ) throws Exception
	{
		final List<String> lines = new ArrayList<>();
		StringBuffer oneLine = new StringBuffer( Config.getExe( this, EXE_FASTQC ) + " "
				+ getRuntimeParams( Config.getList( this, EXE_FASTQC_PARAMS ), NUM_THREADS_PARAM ) + OUTPUT_PARAM
				+ getOutputDir().getAbsolutePath() + " " + TEMP_DIR_PARAM + getTempDir().getAbsolutePath() );
		for( final File file: files )
		{
			oneLine.append( " " + file.getAbsolutePath() );
		}
		lines.add( oneLine.toString() );
		return ( lines );
	}

	/**
	 * Verify that none of the derived command line parameters are included in
	 * {@link biolockj.Config}.{@value this.EXE_FASTQC_PARAMS}.
	 */
	@Override
	public void checkDependencies() throws Exception
	{
		super.checkDependencies();
		checkParams();
		if (Config.getPositiveInteger(this, SCRIPT_BATCH_SIZE) !=  1 
				&& Config.getPositiveInteger(this, Constants.SCRIPT_NUM_THREADS) > 1 ) {
			Log.info(this.getClass(), "The FastQC module runs most efficiently when " + SCRIPT_BATCH_SIZE 
					+ " is set to 1, (currenlty set to [" + Config.getPositiveInteger(this, SCRIPT_BATCH_SIZE) + "])." +
					"Each node is currently set to process [" + 
					Config.getPositiveInteger(this, Constants.SCRIPT_NUM_THREADS) + "] files at a time because of the " + Constants.SCRIPT_NUM_THREADS + "] parameter.");
		}
	}

	// TODO: some options may give a file path. (for example --contaminants and --adapters). In docker mode, this will
	// have to be detected and either avoided or the
	// path to the file must be passed via -v.
	/**
	 * Make sure number of threads, and other options given by biolockj, are not specified in
	 * {@link biolockj.Config}.{@value this.EXE_FASTQC_PARAMS}.
	 * 
	 * @throws Exception if errors occur
	 */
	private void checkParams() throws Exception
	{
		final String allParams = BioLockJUtil.join( Config.getList( this, EXE_FASTQC_PARAMS ) );
		if( allParams.indexOf( NUM_THREADS_PARAM ) > -1 )
		{
			throw new Exception( "Invalid module option (" + NUM_THREADS_PARAM + ") found in property("
					+ EXE_FASTQC_PARAMS + "). BioLockJ derives this value from property: " + Constants.SCRIPT_NUM_THREADS );
		}
		if( allParams.indexOf( NUM_THREADS_PARAM_SHORT ) > -1 )
		{
			throw new Exception( "Invalid module option (" + NUM_THREADS_PARAM_SHORT + ") found in property("
					+ EXE_FASTQC_PARAMS + "). BioLockJ derives this value from property: " + Constants.SCRIPT_NUM_THREADS );
		}
		if( allParams.indexOf( OUTPUT_PARAM ) > -1 )
		{
			throw new Exception( "Invalid module option (" + OUTPUT_PARAM + ") found in property(" + EXE_FASTQC_PARAMS
					+ "). BioLockJ supplies this value." );
		}
		if( allParams.indexOf( OUTPUT_PARAM_SHORT ) > -1 )
		{
			throw new Exception( "Invalid module option (" + OUTPUT_PARAM_SHORT + ") found in property(" + EXE_FASTQC_PARAMS
					+ "). BioLockJ supplies this value." );
		}
		if( allParams.indexOf( TEMP_DIR_PARAM ) > -1 )
		{
			throw new Exception( "Invalid module option (" + TEMP_DIR_PARAM + ") found in property(" + EXE_FASTQC_PARAMS
					+ "). BioLockJ supplies this value." );
		}
		if( allParams.indexOf( TEMP_DIR_PARAM_SHORT ) > -1 )
		{
			throw new Exception( "Invalid module option (" + TEMP_DIR_PARAM_SHORT + ") found in property(" + EXE_FASTQC_PARAMS
					+ "). BioLockJ supplies this value." );
		}
	}

	/**
	 * Build the nested list of bash script lines that will be used by {@link biolockj.util.BashScriptBuilder} to build
	 * the worker scripts. Paired reads are not handled any differently. Only
	 * allow one line per script to maximize efficient use of multi-threading. Because of how FastQC handles
	 * multi-threading (n threads allows it to process n samples at a time).
	 */
	@Override
	public void executeTask() throws Exception
	{
		BashScriptBuilder.buildScripts( this, buildScript( getInputFiles() ) );
	}

	/**
	 * {@link biolockj.Config} property {@value #EXE_FASTQC} defines the command line FastQC executable
	 */
	protected static final String EXE_FASTQC = "exe.fastqc";

	/**
	 * {@link biolockj.Config} property {@value #EXE_FASTQC_PARAMS} is used to set the FastQC executable runtime
	 * parameters
	 */
	protected static final String EXE_FASTQC_PARAMS = "exe.fastqcParams";
	
	private static final String SCRIPT_BATCH_SIZE = "fastcq.batchSize";

	private static final String NUM_THREADS_PARAM = "--threads";
	private static final String NUM_THREADS_PARAM_SHORT = "-t ";

	private static final String OUTPUT_PARAM = "--outdir ";
	private static final String OUTPUT_PARAM_SHORT = "-o ";

	private static final String TEMP_DIR_PARAM = "--dir ";
	private static final String TEMP_DIR_PARAM_SHORT = "-d ";

}
