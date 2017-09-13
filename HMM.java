import java.util.*;

public class HMM {

    private mat A;
    private mat B;
    private mat pi;
    private mat gamma;
    private mat beta;
    private mat alpha;
    private final int MAX_ITERS = 35;
    double oldLogProb = Double.NEGATIVE_INFINITY;

    public HMM(mat A, mat B, mat pi){
        this.A = A;
        this.B = B;
        this.pi = pi;
    }

    public HMM(){
        this.A = new mat(Initials.TRANSITION);
        this.B = new mat(Initials.EMISSION);
        this.pi = new mat(Initials.INITIAL_STATES);
    }

    public double getProb() {
        return this.oldLogProb;
    }

    public void BaumWelchTrain(List<Integer> obs){
        int iters = 0;
        gamma = new mat(obs.size(),A.getNmrOfRows());
        beta = new mat(obs.size(),A.getNmrOfRows());
        alpha = new mat(obs.size(),pi.getNmrOfColumns());

        whileloop:
        while (iters<MAX_ITERS){
            List<Double> c = new ArrayList<>(obs.size());
            List<mat> digamma = new ArrayList<>(obs.size());

            // calc alpha
            double c0 = 0.0;
            Integer o0 = obs.get(0);
            for (int i = 0; i<A.getNmrOfColumns();i++){
                alpha.setElement(0,i,pi.getElement(0,i)*B.getElement(i,o0));
                c0 += alpha.getElement(0,i);
            }
            c0 = 1.0/c0;
            for (int i = 0;i<A.getNmrOfColumns();i++){
                alpha.setElement(0,i,c0*alpha.getElement(0,i));
            }
            c.add(c0);

            for (int t=1;t<obs.size();t++){
                double ct = 0.0;
                Integer o = obs.get(t);
                for (int i=0;i<A.getNmrOfColumns();i++){
                    double currAlpha = 0.0;
                    for (int j=0;j<A.getNmrOfColumns();j++){
                        currAlpha += alpha.getElement(t-1,j)*A.getElement(j,i);
                    }
                    currAlpha = currAlpha*B.getElement(i,o);
                    ct += currAlpha;
                    alpha.setElement(t,i,currAlpha);
                }
                ct = 1.0/ct;
                for (int i = 0;i<A.getNmrOfColumns();i++){
                    alpha.setElement(t,i,ct*alpha.getElement(t,i));
                }
                c.add(ct);
            }

            // calc beta
            for (int i = 0; i<A.getNmrOfColumns();i++){
                beta.setElement(obs.size()-1,i,c.get(obs.size()-1));
            }
            for (int t = obs.size()-2;t>=0;t--){
                int o = obs.get(t+1);
                for (int i = 0; i<A.getNmrOfColumns();i++){
                    double currBeta = 0.0;
                    for (int j = 0;j<A.getNmrOfColumns();j++){
                        currBeta += A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j);
                    }
                    currBeta = c.get(t)*currBeta;
                    beta.setElement(t,i,currBeta);
                }
            }

            // calc digamma and gamma
            for (int t = 0; t<obs.size()-1;t++){
                int o = obs.get(t+1);
                double denom = 0.0;
                for (int i = 0; i<A.getNmrOfColumns();i++){
                    for (int j = 0; j<A.getNmrOfColumns();j++){
                        denom += alpha.getElement(t,i)*A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j);
                    }
                }
                mat digammaMat = new mat(A.getNmrOfColumns(),A.getNmrOfColumns());
                for (int i = 0;i<A.getNmrOfColumns();i++){
                    double currGamma = 0.0;
                    for (int j=0;j<A.getNmrOfColumns();j++){
                        double currDiGamma = (alpha.getElement(t,i)*A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j))/denom;
                        currGamma += currDiGamma;
                        digammaMat.setElement(i,j,currDiGamma);
                    }
                    gamma.setElement(t,i,currGamma);
                }
                digamma.add(digammaMat);
            }

            double denom = 0.0;
            for (int i = 0; i<A.getNmrOfColumns();i++){
                denom += alpha.getElement(obs.size()-1,i);
            }
            for (int i = 0; i<A.getNmrOfColumns();i++){
                gamma.setElement(obs.size()-1,i,alpha.getElement(obs.size()-1,i)/denom);
            }

            // re-estimate Pi
            for (int i = 0; i<pi.getNmrOfColumns();i++){
                pi.setElement(0,i,gamma.getElement(0,i));
            }

            // re-estimate A
            for (int i = 0; i<A.getNmrOfColumns();i++){
                for (int j = 0; j<A.getNmrOfColumns();j++){
                    double tmpNumer = 0.0;
                    double tmpDenom = 0.0;
                    for (int t = 0;t<obs.size()-1;t++){
                        tmpNumer += digamma.get(t).getElement(i,j);
                        tmpDenom += gamma.getElement(t,i);
                    }
                    A.setElement(i,j,tmpNumer/tmpDenom);
                }
            }

            // re-estimate B
            for (int i = 0; i<A.getNmrOfColumns();i++){
                for (int j = 0; j<B.getNmrOfColumns();j++){
                    double tmpNumer = 0.0;
                    double tmpDenom = 0.0;
                    for (int t = 0; t<obs.size();t++){
                        int o = obs.get(t);
                        if (j == o){
                            tmpNumer += gamma.getElement(t,i);
                        }
                        tmpDenom += gamma.getElement(t,i);
                    }
                    B.setElement(i,j,tmpNumer/tmpDenom);
                }
            }

            // compute log[P(O|lambda)]
            double logProb = 0.0;
            for (int t = 0;t<obs.size();t++){
                logProb += Math.log10(c.get(t));
            }
            logProb = -1.0*logProb;

            iters++;
            if (iters<MAX_ITERS && logProb>oldLogProb){
                oldLogProb = logProb;
            } else {
                break whileloop;
            }
        }
    }

    public List<Double> predictNextEmissions(){
        List<Double> lastGamma = gamma.getRow(gamma.getNmrOfRows()-1);
        mat probStates = new mat(1,lastGamma.size());
        probStates.setRow(0,lastGamma);
        mat nextStatesProb = probStates.product(A);

        mat nextEmissionProbs = nextStatesProb.product(B);

        return nextEmissionProbs.getRow(0);
    }
    
    public double HowLikelyIsThisObservation(List<Integer> obs) {
        
    	mat currAlpha = new mat(pi.getNmrOfRows(),pi.getNmrOfColumns());
        boolean isFirst = true;
        for (int o:obs){
            if (isFirst){
                currAlpha = pi.dotProductColumn(B,o);
                isFirst = !isFirst;
            } else {
                currAlpha = currAlpha.product(A);
                currAlpha = currAlpha.dotProductColumn(B,o);
            }
        }
        return currAlpha.sumElements();
    }

    public void LogHMMModels() {
    	this.A.LogMatrixes();
    	this.B.LogMatrixes();
    	this.pi.LogMatrixes();
    }
    
    public mat AlphaPassSeqOdds(List<Integer> obs) {
        // calc alpha
    	List<Double> c = new ArrayList<>(obs.size());
        mat alpha = new mat(obs.size(),pi.getNmrOfColumns());   	 
        double c0 = 0.0;
        Integer o0 = obs.get(0);
        for (int i = 0; i<A.getNmrOfColumns();i++){
            alpha.setElement(0,i,pi.getElement(0,i)*B.getElement(i,o0));
            c0 += alpha.getElement(0,i);
        }
        c0 = 1.0/c0;
        for (int i = 0;i<A.getNmrOfColumns();i++){
            alpha.setElement(0,i,c0*alpha.getElement(0,i));
        }
        c.add(c0);

        for (int t=1;t<obs.size();t++){
            double ct = 0.0;
            Integer o = obs.get(t);
            for (int i=0;i<A.getNmrOfColumns();i++){
                double currAlpha = 0.0;
                for (int j=0;j<A.getNmrOfColumns();j++){
                    currAlpha += alpha.getElement(t-1,j)*A.getElement(j,i);
                }
                currAlpha = currAlpha*B.getElement(i,o);
                ct += currAlpha;
                alpha.setElement(t,i,currAlpha);
            }
            ct = 1.0/ct;
            for (int i = 0;i<A.getNmrOfColumns();i++){
                alpha.setElement(t,i,ct*alpha.getElement(t,i));
            }
            c.add(ct);
        }        	
        return alpha;        
    }
    
    public double GetScaledOdds(List<Integer> obs) {
    	mat alpha = AlphaPassSeqOdds(obs);
    	// get the last row to get the odds of observing this seq from the given hmm
    	double odds = 0;
    	for(double d:alpha.getRow(obs.size()-1)) {
    		odds += d;
    	}
    	
    	return odds;
    }

    public static void main(String[] args){
        mat A = new mat();
        mat B = new mat();
        mat pi = new mat();
        List<String> obs = new ArrayList<>();
        // Read input
        Scanner sc = new Scanner(System.in);
        int ind = 0;
        while (sc.hasNextLine()){
            switch (ind){
                case 0: A = new mat(sc.nextLine());
                case 1: B = new mat(sc.nextLine());
                case 2: pi = new mat(sc.nextLine());
                case 3: obs = new ArrayList<>(Arrays.asList(sc.nextLine().trim().split(" ")));
                    obs = obs.subList(1,obs.size());
            }
            ind++;
        }
        obs = obs.subList(0,10);

    }
}
