package nca.util;

import java.util.Random;

public class Randomization {

	private int[] _numOfOutputNodes;

	private double _scale;

	private Random _random;

	private String[][] _strMatrix;

	private boolean _hasUnconfirmedConnection;


	public Randomization(){
		_hasUnconfirmedConnection = false;
		_random = new Random();
	}

	public Randomization(String[][] matrix){
		this();
		_strMatrix = matrix;
		setNumOfOutputNodes(matrix);
		setScale();
		setHasUnconfirmed();
	}


	public int[] getNumOfOutputNodes(){
		return _numOfOutputNodes;
	}

	private void setNumOfOutputNodes(String[][] matrix){

		_numOfOutputNodes = new int[matrix[0].length];
		String [][] transposeStrMatrix = transposeMatrix(matrix);
		for (int i = 0; i < matrix[0].length; i++ )
			_numOfOutputNodes[i] = getNumOfOutputNodes(transposeStrMatrix[i]);
	}


	// now scale by the largest value instead of the largest num of output nodes
	public double getScale(){
		return _scale;
	}

	private void setScale (){
		int max = 0;
		for (int i: this.getNumOfOutputNodes()){
			if (i > max)
				max = i;
		}

		_scale = (double)(max/1);
	}


	public String[][] getStrMatrix(){
		return _strMatrix;
	}

	public Random getRandom(){
		return _random;
	}

	public boolean hasUnconfirmed(){
		return _hasUnconfirmedConnection;
	}

	private void setHasUnconfirmed(){
		for (int i = 0; i < _strMatrix.length ; i++){
			for (int j = 0; j < _strMatrix[0].length ; j++){
				if (_strMatrix[i][j].equals("*")){
					if (!this.hasUnconfirmed())
						_hasUnconfirmedConnection = true;
					return;
				}
			}
		}

	}



	private double[] randomPositive (String[] col){
		double[] value = new double[col.length];
		for (int i = 0; i < col.length; i++){
			double temp = this.getRandom().nextDouble();
			if (col[i].isEmpty())
				value[i] = 0.0;
			else
				value[i] = temp;
		}

		return value;
	}


	public double[][] normalize(){
		double[][] transposedMatrix = null;
		try {
			String[][] transposedStrMatrix = transposeMatrix(this.getStrMatrix());
			transposedMatrix = new double[this.getStrMatrix()[0].length][this.getStrMatrix().length];

			for (int i = 0; i < transposedStrMatrix.length; i++){
				transposedMatrix[i] = randomPositive(transposedStrMatrix[i]);
				double[] tempAbs = new double[transposedStrMatrix[0].length];
				double sumOfAbs = 0.0;
	 			for (int j = 0; j < transposedStrMatrix[0].length; j++){
	 				tempAbs[j] = getAbsolute(transposedMatrix[i][j]);
	 				sumOfAbs += tempAbs[j];
	 			}

	 			double addVal = getAddValue(this.getNumOfOutputNodes()[i], sumOfAbs);

	 			for (int j = 0; j < tempAbs.length ; j++){
	 				if (Double.compare(tempAbs[j], 0.0) != 0){
		 				tempAbs[j] = tempAbs[j] + addVal;

		 				if (transposedStrMatrix[i][j].equals("+")){
		 					transposedMatrix[i][j] = tempAbs[j];
		 	 			}
		 	 			else if (transposedStrMatrix[i][j].equals("-")){
		 	 				transposedMatrix[i][j] = - tempAbs[j];
		 	 			}
		 	 			else if (transposedStrMatrix[i][j].equals("*")){
		 	 				if (this.getRandom().nextDouble() >= 0.5)
		 	 					transposedMatrix[i][j] = tempAbs[j];
		 	 				else
		 	 					transposedMatrix[i][j] = - tempAbs[j];
		 	 			} else {
		 	 				throw new Exception ("Value should not be empty string");
		 	 			}
	 				}
	 				else
	 					transposedMatrix[i][j] = 0.0;
	 			}

			}

			double maxVal = 0.0 ;
			for (int i = 0; i < transposedMatrix.length; i++){
				for (int j = 0; j < transposedMatrix[0].length ; j++){
					double temp = transposedMatrix[i][j];
					if (transposedMatrix[i][j] < 0){
						temp = - transposedMatrix[i][j];
					}
					if (temp > maxVal)
						maxVal = temp;
				}
			}

			// scale all the value to the interval of +/- [0,1]
			for (int i = 0; i < transposedMatrix.length; i++){
				for (int j = 0; j < transposedMatrix[0].length ; j++){
					//maxVal
					transposedMatrix[i][j] = transposedMatrix[i][j]/1;
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}


		return transposeMatrix(transposedMatrix);

	}

	private double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

	private String[][] transposeMatrix(String [][] m){
		String[][] temp = new String[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

	private double getAddValue(int numOfOutputNodes, double sumOfAbs){
		return (numOfOutputNodes*numOfOutputNodes-sumOfAbs)/numOfOutputNodes;
	}


	private double getAbsolute(double val){
		if (val >= 0)
			return val;
		else
			return -val;
	}

	private int getNumOfOutputNodes (String[] col){
		int count = 0;
		for (String i: col){
			if (!i.isEmpty())
				count ++;
		}

		return count;
	}






}
