import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class HMM0 {
	
	private static ArrayList<ArrayList<Double>> A = new ArrayList<ArrayList<Double>>();	
	private static ArrayList<ArrayList<Double>> B = new ArrayList<ArrayList<Double>>();
	private static ArrayList<ArrayList<Double>> pi = new ArrayList<ArrayList<Double>>();
	
	public static Scanner sc;
	
	public static void main(String[] args) throws java.io.IOException {
		  //  Create Matrices From input
	      File tmpfile = new File("C:\\Users\\bjart\\Downloads\\samplesHmm0\\sample_00.in");
	      //sc = new Scanner(System.in).useDelimiter("\n");
	     // sc = new Scanner(tmpfile).useDelimiter("\n");//
	      sc = new Scanner(tmpfile);
  	      int WhichMatrixIndicator = 0;
  	      
	      while(sc.hasNextLine()) {
	  	      // To Know Which matrix we are creating i.e A, B or pi
	    	 // int index = 0;
	  	      // Get The number of rows
	  	      // Get the number of columns
	  	      // The rest which is the matrix
	  	      if(!sc.hasNext()) break;
	  	      double nmbrOfRows = Double.parseDouble(sc.next()); 
	  	      double nmbrOfColumns = Double.parseDouble(sc.next());	  	      
	  	      //String MatrixMinusSizeIndicators = nextMatrix.substring(3);
	  	      System.out.println(nmbrOfRows);
	  	      System.out.println(nmbrOfColumns);
			    	for(int i = 0; i < nmbrOfRows; i++) {
			    	    ArrayList<Double> Column = new ArrayList<Double>();
		    	    	for (int j = 0; j < nmbrOfColumns; j++) {
		    	    		double value = Double.parseDouble(sc.next());
		    	    		Column.add(value);		
		    	    	}
		    	    	switch(WhichMatrixIndicator) {
			    	    	case 0: A.add(Column);
			    	    	case 1: B.add(Column);
			    	    	case 2: pi.add(Column);
		    	    	}
		    	    	System.out.println(Column);
			    	}		    	    
			    	WhichMatrixIndicator +=1;
			         //matrix[i][j] = sc.nextInt();			    	      
	      }	      
	      System.out.print(A);		      
	}
}