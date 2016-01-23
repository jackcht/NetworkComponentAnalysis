package nca.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ROBNCA.Class1;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

public class MatlabUtil {

	private double[][] _tfa;

	private double[][] _modifiedConnection;

	private String _tfaResultFile;
	private String _connResultFile;


	public MatlabUtil(){

	}
	public MatlabUtil(String tfaResultFileName, String connResultFileName){
		_tfaResultFile = tfaResultFileName;
		_connResultFile = connResultFileName;

	}

	public double[][] getTfa (){
		return _tfa;
	}

	private void setTfa( double[][] tfa){
		_tfa = tfa;
	}

	public String getTfaResultFile(){
		return _tfaResultFile;
	}

	public String getConnResultFile(){
		return _connResultFile;
	}

	public double[][] getModifiedConnection(){
		return _modifiedConnection;
	}

	private void setModifiedConnection(double[][] matrix){
		_modifiedConnection = matrix;
	}

	private double[][] addToTfa(double[][] tfa, double[][] newTfa){
		double[][] addedTfa = new double[tfa.length][tfa[0].length];

		for (int i = 0; i < tfa.length ; i++){
			for (int j = 0; j < tfa[0].length ; j++){
				addedTfa[i][j] = tfa[i][j] + newTfa[i][j];
			}
		}

		return addedTfa;
	}

	private double[][] addToConnection(double[][] matrix, double[][] newMatrix){
		double[][] addedConnection = new double[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length ; i++){
			for (int j = 0; j < matrix[0].length ; j++){
				addedConnection[i][j] = matrix[i][j] + newMatrix[i][j];
			}
		}

		return addedConnection;
	}

	private double[][] getFinalMeanTfa (double[][] tfa, int n){
		double[][] finalMeanTfa = new double[tfa.length][tfa[0].length];
		for (int i = 0; i < tfa.length ; i++){
			for (int j = 0; j < tfa[0].length ; j++){
				finalMeanTfa[i][j] = (tfa[i][j]/n);
			}
		}
		return finalMeanTfa;
	}

	private double[][] getFinalMeanConnection (double[][] connection, int n){
		double[][] finalMeanConnection = new double[connection.length][connection[0].length];
		for (int i = 0; i < connection.length ; i++){
			for (int j = 0; j < connection[0].length ; j++){
				finalMeanConnection[i][j] = (connection[i][j]/n);
			}
		}
		return finalMeanConnection;
	}


	public void randomize(Randomization random, Data data){
		double[][] finalConnectionMatrix = random.normalize();

		MWNumericArray geneData = null;
		MWNumericArray connection = null;

		Object[] result = null;
		Class1 robnca = null;

		try{
			geneData = new MWNumericArray(data.getGeneData(), MWClassID.DOUBLE);

			connection = new MWNumericArray(finalConnectionMatrix, MWClassID.DOUBLE);

		    robnca = new Class1();

		    result = robnca.ROBNCA(2, geneData,connection);

		    setTfa( (double[][]) ((MWNumericArray)result[1]).toDoubleArray() );

		    setModifiedConnection ( (double[][]) ((MWNumericArray)result[0]).toDoubleArray() );

		}
		catch (Exception e){
			e.getMessage();
			e.printStackTrace();
		}
		finally{
			MWArray.disposeArray(geneData);
			MWArray.disposeArray(connection);
			MWArray.disposeArray(result);
			robnca.dispose();
		}
	}



	public void randomize(Randomization random, Data data, int n){

		double[][] cumulativeTfa = null;
		double[][] cumulativeConnection  = null;

		int tempCount = n;

		for (int i = 0; i < tempCount; i++){
			System.out.println(i + "\n");
			double[][] finalConnectionMatrix = random.normalize();

			/** iterative calculation of TFA [S] and Influence strength matrix [A] until the diff between A[n-1] & A[n] < 5%*/
			NcaResult ncaResult = calculateNCAResult(data.getGeneData(), finalConnectionMatrix);

			if (ncaResult.getTfa() == null || ncaResult.getConn() == null){
				tempCount ++;
				continue;
			}


			double[][] resultTfa = ncaResult.getTfa();
			double[][] resultConnection = ncaResult.getConn();


		    String tfaResultPrint = "Result["+ i +"]\r\n";
		    for (int k = 0; k < resultTfa.length; k++){
		    	for (int j = 0; j<resultTfa[0].length; j++){
		    		tfaResultPrint += resultTfa[k][j] + "\t";
		    	}
		    	tfaResultPrint += "\r\n";
		    }
		    textOutput(this.getTfaResultFile(),tfaResultPrint);


		    String connResultPrint = "Result["+ i +"]\r\n";
		    for (int k = 0; k < resultConnection.length; k++){
		    	for (int j = 0; j < resultConnection[0].length; j++){
		    		connResultPrint += resultConnection[k][j] + "\t";
		    	}
		    	connResultPrint += "\r\n";
		    }
		    textOutput(this.getConnResultFile(),connResultPrint);


		    if (i == 0){
		    	cumulativeTfa = resultTfa;
		    	cumulativeConnection = resultConnection;

		    }
		    else{
		    	if (cumulativeTfa == null && cumulativeConnection == null){
		    		cumulativeTfa = resultTfa;
			    	cumulativeConnection = resultConnection;
		    	}
		    	else{
		    		cumulativeTfa = addToTfa(cumulativeTfa, resultTfa );
		    		cumulativeConnection = addToConnection(cumulativeConnection, resultConnection);
		    	}
		    }
		}

		setTfa(getFinalMeanTfa(cumulativeTfa, n)) ;
		setModifiedConnection(getFinalMeanConnection(cumulativeConnection,n));

	}


	private NcaResult calculateNCAResult(double[][] rawGeneData, double[][] connectionMatrix){

		MWNumericArray geneData = null;
		MWNumericArray connection = null;

		Object[] result = null;
		Class1 robnca = null;

		NcaResult ncaResult = null;

		try{
			geneData = new MWNumericArray(rawGeneData, MWClassID.DOUBLE);
			connection = new MWNumericArray(connectionMatrix, MWClassID.DOUBLE);

		    robnca = new Class1();
		    result = robnca.ROBNCA(2, geneData,connection);

		    double[][] resultTfa = (double[][]) ((MWNumericArray)result[1]).toDoubleArray();
		    double[][] resultConnection = (double[][]) ((MWNumericArray)result[0]).toDoubleArray();

		    boolean hasNaN = checkNaN(resultConnection);

		    if (hasNaN){
		    	//System.out.println("In calculateNCAResult: it has NaN");
		    	return new NcaResult(null,null);
		    }
		    else{
		    	connection = new MWNumericArray(resultConnection, MWClassID.DOUBLE);
			    result = robnca.ROBNCA(2, geneData,connection);
			    resultTfa = (double[][]) ((MWNumericArray)result[1]).toDoubleArray();
			    double[][] newResultConnection = (double[][]) ((MWNumericArray)result[0]).toDoubleArray();

			    int count = 0;
		    	while (!checkThreshold(resultConnection, newResultConnection)){
		    		count ++;
		    		resultConnection = newResultConnection;
		    		connection = new MWNumericArray(newResultConnection, MWClassID.DOUBLE);
				    result = robnca.ROBNCA(2, geneData,connection);
				    resultTfa = (double[][]) ((MWNumericArray)result[1]).toDoubleArray();
				    newResultConnection = (double[][]) ((MWNumericArray)result[0]).toDoubleArray();

				    hasNaN = checkNaN(newResultConnection);

				    if (hasNaN){
				    	//System.out.println("it has NaN");
				    	return new NcaResult(null,null);
				    }
		    	}
		    	System.out.println("iteration count: "+ count);

		    	ncaResult = new NcaResult(resultTfa,newResultConnection);
		    }

		}
		catch(Exception e){
			e.getMessage();
			e.printStackTrace();
		}
		finally{
			MWArray.disposeArray(geneData);
			MWArray.disposeArray(connection);
			MWArray.disposeArray(result);
			robnca.dispose();
		}
		return ncaResult;

	}



	private boolean textOutput(String filename, String text) {
		FileWriter output = null;
	    try {
	    	output = new FileWriter(filename, true);
		    BufferedWriter writer = new BufferedWriter(output);
			writer.write(text);
			writer.close();
			output.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return false;
	}

	private boolean checkNaN (double[][] matrix){
		boolean hasNaN = false;
		for(int k = 0; k < matrix.length ; k++){
	    	for (int j = 0; j < matrix[0].length; j++){
	    		if (Double.isNaN(matrix[k][j])){
	    			hasNaN = true;
	    			break;
	    		}
	    	}
	    	if (hasNaN){
	    		break;
	    	}
	    }

		return hasNaN;
	}

	private boolean checkThreshold (double[][] oldConn, double[][] newConn){
		boolean withInThreshold =true;
		for(int i = 0; i < oldConn.length ; i++){
	    	for (int j = 0; j < oldConn[0].length; j++){
	    		if ( (Math.abs(newConn[i][j]-oldConn[i][j]))/oldConn[i][j] > 0.05 ){
	    			withInThreshold = false;
	    			break;
	    		}
	    	}
	    	if (!withInThreshold){
	    		break;
	    	}
		}

		return withInThreshold;
	}



	final class NcaResult {
	    private final double[][] _tfa;
	    private final double[][] _conn;

	    public NcaResult(double[][] tfa, double[][] conn) {
	        this._tfa = tfa;
	        this._conn = conn;
	    }

	    public double[][] getTfa() {
	        return _tfa;
	    }

	    public double[][] getConn() {
	        return _conn;
	    }
	}

}
