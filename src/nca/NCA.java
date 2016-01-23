package nca;

import nca.util.*;

public class NCA {

	public static void main(String[] args) throws Exception {


		String geneDataFile = "geneData.txt";
		String connectionFile = "connection.txt";

		String tfaResultFile = "result-tfa.txt";
		String connResultFile = "result-conn.txt";


		Data data = new Data();

		data.getGeneData(geneDataFile);
		data.getConnectionData(connectionFile);

		System.out.println(data.toString());

		Randomization random = new Randomization(data.getConnectionMatrix());



		MatlabUtil matlab = new MatlabUtil(tfaResultFile,connResultFile);

		if (random.hasUnconfirmed()){
			// randomization for 100 times
			System.out.println("calculate for 500 times");
			matlab.randomize(random, data, 10);
		}
		else{
			// randomization for only once
			matlab.randomize(random, data);
		}


		double[][] tfa = matlab.getTfa();

		double[][] modifiedConnection = matlab.getModifiedConnection();

		System.out.println("500 times tfa: ");
		String tfaResult = "\r\nFinal\r\n";
	    for (int i = 0; i < tfa.length; i++){
	    	for (int j = 0; j<tfa[0].length; j++){
	    		System.out.print(tfa[i][j] + "\t");
	    		tfaResult += tfa[i][j] + "\t";
	    	}
	    	System.out.println();
	    	tfaResult += "\r\n";
	    }

	    System.out.println("\n 500 times connection: ");
	    String connResult = "\r\nFinal\r\n";
	    for (int i = 0; i < modifiedConnection.length; i++){
	    	for (int j = 0; j < modifiedConnection[0].length; j++){
	    		System.out.print(modifiedConnection[i][j]+"\t");
	    		connResult += modifiedConnection[i][j]+"\t";
	    	}
	    	System.out.println();
	    	connResult += "\r\n";
	    }

	    //
	    data.textOutput(tfaResultFile, tfaResult);
	    data.textOutput(connResultFile, connResult);
	}
}

