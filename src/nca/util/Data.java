package nca.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Data {


	private String[] _geneNames;

	private String[] _timePoint;

	private double[][] _geneData;

	private String[][] _connectionMatrix;

	private String[] _transcriptFactor;


	public double[][] getGeneData(){
		return _geneData;
	}

	private void setGeneData(double[][] geneData){
		_geneData = geneData;
	}

	public String[][] getConnectionMatrix(){
		return _connectionMatrix;
	}

	private void setConnectionMatrix(String[][] matrix){
		_connectionMatrix = matrix;
	}

	public String[] getGeneNames(){
		return _geneNames;
	}

	private void setGeneNames(String[] geneNames){
		_geneNames = geneNames;
	}

	public String[] getTimePoint(){
		return _timePoint;
	}

	private void setTimePoint(String[] timepoint){
		_timePoint = timepoint;
	}

	public String[] getTranscriptFactor(){
		return _transcriptFactor;
	}

	private void setTranscriptFactor(String[] tf){
		_transcriptFactor = tf;
	}


	private String[] removeDuplicates(String[] array) {
	    Set<String> alreadyPresent = new HashSet<>();
	    String[] list = new String[array.length];
	    int i = 0;

	    for (String element : array) {
	        if (alreadyPresent.add(element)) {
	        	list[i++] = element;
	        }
	    }

	    return Arrays.copyOf(list, i);
	}



	public void getGeneData(String filename){
		// read matrix separated by blanks
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<String> geneNameList = new ArrayList<String>();
		String timePoint[] = null;

		try{
			BufferedReader input = new BufferedReader(new FileReader(filename));

			timePoint = input.readLine().split("\\s+");
			String inputLine;

			while ((inputLine = input.readLine()) != null){

				String temp[] = inputLine.split("\\s+");

				//System.out.println("temp[] length is: "+ temp.length);
				if (temp.length == 1)
					break;

				double[] row = new double[temp.length-1];

				for (int i = 0 ; i < temp.length; i++){
					if (i == 0)
						geneNameList.add(temp[i]);
					else
						row[i-1] = Double.parseDouble(temp[i]);
				}
				data.add(row);
			}
			input.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		double[][] matrix = new double[data.size()][data.get(0).length];

		for (int i = 0; i < data.size(); i++){
			matrix[i] = data.get(i);
		}

		String[] geneName = new String[geneNameList.size()];

		for (int i = 0; i < geneNameList.size(); i++){
			geneName[i] = geneNameList.get(i);
		}

		setTimePoint(timePoint);
		setGeneNames(geneName);
		setGeneData(matrix);

	}


	public void getConnectionData(String filename) throws Exception{
		// Must read the gene data file first (known how many gene)
		// TF	|| 	interaction	||	Gene		separated by ","
		// +: positive connection
		// -: negative connection
		// *: not known yet

		if (this.getGeneNames() == null)
			throw new Exception("Must read the gene data file first !");

		ArrayList<String[]> connection = new ArrayList<String[]>();

		try{
			BufferedReader input = new BufferedReader(new FileReader(filename));
			String inputLine;
			while ((inputLine = input.readLine()) != null){
				String temp[] = inputLine.split(",");
				for (int i = 0; i < temp.length; i++)
					temp[i] = temp[i].trim();

				connection.add(temp);
			}
			input.close();
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}

		String[] tf = new String[connection.size()];
		for (int i = 0; i < connection.size(); i++){
			tf[i] = connection.get(i)[0];
		}
		tf = removeDuplicates(tf);
		setTranscriptFactor(tf);

		String[][] matrix = new String[this.getGeneNames().length][tf.length];
		for (int i = 0; i < this.getTranscriptFactor().length; i++){
			for (int j = 0; j < connection.size(); j++){
				if (connection.get(j)[0].equals(this.getTranscriptFactor()[i])){
					for (int k = 0; k < this.getGeneNames().length; k++){
						if (connection.get(j)[2].equals(this.getGeneNames()[k])){
							matrix[k][i] = connection.get(j)[1];
						}
					}
				}
			}
		}

		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++){

				if (matrix[i][j] == null)
					matrix[i][j] = "";
			}
		}

		setConnectionMatrix(matrix);

	}


	public boolean textOutput(String filename, String text) {
		FileWriter output = null;
	    try {
	    	output = new FileWriter(filename,true);
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


	public String toString(){
		StringBuilder sb = new StringBuilder("Data: \r\n");
		sb.append("  Gene Names: ");
		for (String i: this.getGeneNames()){
			sb.append(i+"; ");
		}
		sb.append("\r\n  Time Points: ");
		for (String i: this.getTimePoint()){
			sb.append(i+"; ");
		}
		sb.append("\r\n");

		sb.append("  Transcription Factors: ");
		for (String i: this.getTranscriptFactor()){
			sb.append(i+"; ");
		}

		return sb.toString();
	}


}
