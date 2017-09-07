import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class HMM0 {
	
	//private static ArrayList<ArrayList<Double>> A = new ArrayList<ArrayList<Double>>();	
	//private static ArrayList<ArrayList<Double>> B = new ArrayList<ArrayList<Double>>();
	//private static ArrayList<ArrayList<Double>> pi = new ArrayList<ArrayList<Double>>();
	
	private static mat A;
	private static mat B;
	private static mat pi;
	
	public static Scanner sc;
	
	public static void main(String[] args) throws java.io.IOException {
		
		  //  Create Matrices From input
	      //File tmpfile = new File("C:\\Users\\bjart\\Downloads\\samplesHmm0\\sample_00.in");
	      sc = new Scanner(System.in);
	     // sc = new Scanner(tmpfile).useDelimiter("\n");//
	      //sc = new Scanner(tmpfile);
  	      int WhichMatrixIndicator = 0;
  	      
	      while(sc.hasNextLine()) {
	  	      // To Know Which matrix we are creating i.e A, B or pi
	  	      // Get The number of rows
	  	      // Get the number of columns
	  	      // The rest which is the matrix
	  	      if(!sc.hasNext()) break;
	  	      double nmbrOfRows = Double.parseDouble(sc.next()); 
	  	      double nmbrOfColumns = Double.parseDouble(sc.next());
	  	      switch(WhichMatrixIndicator) {
		    	case 0: A = new mat((int)nmbrOfRows,(int)nmbrOfColumns);
		    	case 1: B = new mat((int)nmbrOfRows,(int)nmbrOfColumns);
		    	case 2: pi = new mat((int)nmbrOfRows,(int)nmbrOfColumns);
	  	      }
				for(int i = 0; i < nmbrOfRows; i++) {
				    ArrayList<Double> Column = new ArrayList<Double>();
					for (int j = 0; j < nmbrOfColumns; j++) {
						double value = Double.parseDouble(sc.next());
						switch(WhichMatrixIndicator) {
							case 0: A.setElement(i, j, value);
							case 1: B.setElement(i, j, value);
							case 2: pi.setElement(i, j, value);
						}						
					}
				}		    	    
			    WhichMatrixIndicator +=1;		    	      
	      }
	      mat piProductA = pi.product(A);
	      mat piProductAProductB = piProductA.product(B);
		  piProductAProductB.printMatrixForKattis();
	}
}