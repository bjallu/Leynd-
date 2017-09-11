import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class HMM2 {
	
    public static int[] ViterbiAlgrimi(mat A, mat B, mat pi, List<String> obs){
        
    	mat T1 = new mat(A.getNmrOfRows(),obs.size());
        mat T2 = new mat(A.getNmrOfRows(),obs.size());
        
        for(int i = 0; i<A.getNmrOfRows(); i++) {
        	double value = pi.getElement(0, i) * B.getElement(i, Integer.parseInt(obs.get(0)));
        	T1.setElement(i, 0, value);
        	T2.setElement(i, 0, 0.0);
        }
        //boolean isFirst = true;
        for (int i = 1; i<obs.size(); i++){
        	for (int j = 0; j<A.getNmrOfRows(); j++) {
        		
        		List<Double> transitionProbabilities = new ArrayList<Double>(A.getNmrOfRows()-1);

        		// All possible odds
        		
        		for(int m = 0; m<A.getNmrOfRows(); m++) {
        			
        			double movingProbabilty = T1.getElement(m, i-1) * A.getElement(m, j);			
        			transitionProbabilities.add(movingProbabilty);
        			
        		}
        		
        		double value = B.getElement(j, Integer.parseInt(obs.get(i))) * max(transitionProbabilities);		
        		T1.setElement(j, i, value);
        		double id = argMax(transitionProbabilities);
        		T2.setElement(j, i, id);
        	}
        }              
        
		List<Double> finalState = new ArrayList<Double>();
		// Now we go backwards through the ids to find the likeliest state path		
		for(int i = 0; i<A.getNmrOfRows(); i++) {			
			double value = T1.getElement(i, obs.size()-1);			
			finalState.add(value);			
		}
		
        double lastState = argMax(finalState);       
        int[] theIdsCorrespondingToTheLikeliestStates = new int[obs.size()];            
        theIdsCorrespondingToTheLikeliestStates[obs.size()-1] = (int) lastState;
        
        for(int p = obs.size()-2; p>-1;p--) {
        	int currentState = theIdsCorrespondingToTheLikeliestStates[p+1];
        	double value = T2.getElement(currentState, p+1);
        	theIdsCorrespondingToTheLikeliestStates[p] = (int) value;
        }
        return theIdsCorrespondingToTheLikeliestStates;
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
        //File tmpFile = new File("C:\\Users\\bjart\\Downloads\\hmm3\\hmm3_01.in");
        //Scanner sc = new Scanner(tmpFile);
        Scanner sc = new Scanner(System.in);
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

        int[] viterbiDecoder = ViterbiAlgrimi(A,B,pi,obs);
        for(int id:viterbiDecoder) {
        	System.out.print(id + " ");
        }
        //alpha.printMatrixForKattis();

    }

}
