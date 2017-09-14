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
        this.fillMatrix(A);
        this.fillMatrix(B);
        this.fillMatrix(pi);
    }

    private void fillMatrix(mat matrix){
        Random r = new Random();
        for(int i = 0; i < matrix.getNmrOfRows(); i++){
            double sum = 0;
            for(int j = 0; j < matrix.getNmrOfColumns(); j++){
                double rand = r.nextDouble();
                matrix.setElement(i,j,rand);
                sum += rand;
            }

            for(int j = 0; j < matrix.getNmrOfColumns(); j++){
                matrix.setElement(i,j,matrix.getElement(i,j)/sum);
            }
        }
    }

    public mat alphaMatrix(List<Integer> obs){
        mat alpha = new mat(obs.size(),pi.getNmrOfColumns());
        mat currAlpha = new mat(pi.getNmrOfRows(),pi.getNmrOfColumns());
        boolean isFirst = true;
        for (int ind = 0; ind<obs.size();ind++){
            int oInt = obs.get(ind);
            Double ct = 0.0;
            if (isFirst){
                currAlpha = pi.dotProductColumn(B,oInt);
                for (Double d : currAlpha.getRow(0)){
                    ct += d;
                }
                ct = 1/ct;
                for (int i=0;i<pi.getNmrOfColumns();i++){
                    currAlpha.setElement(0,i,ct*currAlpha.getElement(0,i));
                }
                alpha.setRow(0, currAlpha.getRow(0));
                isFirst = !isFirst;
            } else {
                currAlpha = currAlpha.product(A).dotProductColumn(B,oInt);
                for (Double d : currAlpha.getRow(0)){
                    ct += d;
                }
                ct = 1/ct;
                for (int i=0;i<pi.getNmrOfColumns();i++){
                    currAlpha.setElement(0,i,ct*currAlpha.getElement(0,i));
                }
                alpha.setRow(ind, currAlpha.getRow(0));
            }

        }
        return alpha;
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
            if (iters<MAX_ITERS && logProb>oldLogProb+0.0000001){
                oldLogProb = logProb;
            } else {
                break whileloop;
            }
        }
    }

    public List<Double> predictNextEmissions(List<Integer> o){
        List<Double> lastGamma = gamma.getRow(gamma.getNmrOfRows()-1);
        mat probStates = new mat(1,lastGamma.size());
        probStates.setRow(0,lastGamma);
        //mat probStates = new mat(1,alpha.getNmrOfColumns());
        probStates.setRow(0,alpha.getRow(o.size()-1));

        mat nextStatesProb = probStates.product(A);

        mat nextEmissionProbs = nextStatesProb.product(B);

        return nextEmissionProbs.getRow(0);
    }

    public List<Integer> ViterbiAlgrim(List<Integer> obs){

        mat T1 = new mat(A.getNmrOfRows(),obs.size());
        mat T2 = new mat(A.getNmrOfRows(),obs.size());

        for(int i = 0; i<A.getNmrOfRows(); i++) {
            double value = pi.getElement(0, i) * B.getElement(i, obs.get(0));
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

                double value = B.getElement(j, obs.get(i)) * max(transitionProbabilities);
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
        Integer[] theIdsCorrespondingToTheLikeliestStates = new Integer[obs.size()];
        theIdsCorrespondingToTheLikeliestStates[obs.size()-1] = (int) lastState;

        for(int p = obs.size()-2; p>-1;p--) {
            int currentState = theIdsCorrespondingToTheLikeliestStates[p+1];
            double value = T2.getElement(currentState, p+1);
            theIdsCorrespondingToTheLikeliestStates[p] = (int) value;
        }
        return Arrays.asList(theIdsCorrespondingToTheLikeliestStates);
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
