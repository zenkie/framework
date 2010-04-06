package nds.process;

/**
 * For JReportRunner, extend using java or python
 *  
 * @author yfzhu
 * 
 */
public interface ProcessRunner {
	/**
	 * Generate report according to pinstance parameters, and return generated file
	 * 
	 * @param pinstanceId ad_pi_id, all parameters in db
	 * @return file generated, must store in user's web home directory
	 * @throws Exception
	 */
	public String execute(int pinstanceId) throws Exception;
}
