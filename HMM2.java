import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class HMM2 {
	
    public static mat ViterbiAlgrimi(mat A, mat B, mat pi, List<String> obs){
        
    	mat T1 = new mat(obs.size(),A.getNmrOfRows());
        mat T2 = new mat(obs.size(),A.getNmrOfRows());
        
        for(int l = 0; l<A.getNmrOfRows(); l++) {
        	double value = pi.getElement(0, l) * B.getElement(l, Integer.parseInt(obs.get(0)));
        	T1.setElement(0, l, value);
        }
        //boolean isFirst = true;
        for (int i = 1; i<obs.size(); i++){
        	for(int j = 0; j<A.getNmrOfRows(); j++) {
        		
        		List<Double> transitionProbabilities = new ArrayList<Double>(A.getNmrOfRows());
        		//mat currentDeltas = new mat(obs.size(),A.getNmrOfRows());
        		// All possible odds
        		
        		for(int m = 0; m<A.getNmrOfRows(); m++) {
        			
        			double movingProbabilty = T1.getElement(i-1, m) * A.getElement(m, j);			
        			transitionProbabilities.add(movingProbabilty);
        			
        		}
        		
        		double bestProbability = max(transitionProbabilities);
        		double whichStateHadTheBestProbability = argMax(transitionProbabilities);
        		// Get all next available probabilities
        		double prob = bestProbability * B.getElement(j, Integer.parseInt(obs.get(i)));        		
        		T1.setElement(i, j, prob);
        		T2.setElement(i, j, whichStateHadTheBestProbability);
        	}
        }              
        
        mat thePath = new mat(1,obs.size());     
        int argMax = argMax(T1.getRow(obs.size()-1));
        //thePath.setElement(0, obs.size()-1, (double) argMax);
        thePath.setElement(0, obs.size()-1, T2.getElement(obs.size()-1, argMax));
        for(int p = obs.size()-2; p>-1;p--) {
        	double value = argMax(T2.getRow(p));
        	thePath.setElement(0, p, value);
        }
        return thePath;
    }
    
    public static double max (List<Double> matrix)
    {
      double max = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < matrix.size(); i++) {
        double x = matrix.get(i);
        if (x > max) {
          max = x;
        }
      }
      return max;
    }
    
    public static int argMax (List<Double> matrix)
    {
      int id = -1;
      double max = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < matrix.size(); i++) {
        double x = matrix.get(i);
        if (x > max) {
          max = x;
          id = i;
        }
      }
      return id;
    }
	
	public static void main(String[] args) throws FileNotFoundException {
        mat A = new mat();
        mat B = new mat();
        mat pi = new mat();
        List<String> obs = new ArrayList<>();
        // Read input
	    File tmpfile = new File("C:\\Users\\bjart\\Downloads\\hmm3\\hmm3_01.in");
	    Scanner sc = new Scanner(tmpfile);
        //Scanner sc = new Scanner(System.in);
        int ind = 0;
        while (sc.hasNextLine()){
            switch (ind){
                case 0: A = new mat(sc.nextLine());
                case 1: B = new mat(sc.nextLine());
                case 2: pi = new mat(sc.nextLine());
                case 3: obs = new ArrayList<>(Arrays.asList(sc.nextLine().split(" ")));
                        obs = obs.subList(1,obs.size());
            }
            ind++;
        }

        mat alpha = ViterbiAlgrimi(A,B,pi,obs);
        alpha.printMatrixForKattis();

    }

}
