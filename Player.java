import java.util.*;

class Player {

    private List<HMM> birdPatternHMMs = new ArrayList<>();
    int currRound = -1;
    // we need as many hmms as there are species to try to distinguish them apart
    // List<HMM> DifferentBirdSpecies = new ArrayList<>(Constants.COUNT_SPECIES);
    private List<Double> probabilityValues = new ArrayList<>();
    HMM[] DifferentBirdSpecies = new HMM[Constants.COUNT_SPECIES];
    public List<List<HMM>> specieHmm = new ArrayList<List<HMM>>();
    private double storkThreshold = 3.629881714570972E-79;
    private final int NMR_OF_STATES = 5;
    public static double[] guessProbabilityes;
    int[] guessIds;

    public Player() {

        for(int i = 0; i<Constants.COUNT_SPECIES; i++) {
            ArrayList<HMM> specieHmmChain = new ArrayList<HMM>();
            specieHmm.add(specieHmmChain);
        }

    }
    
    public static double godDamnStorkOdds(List<List<HMM>> Allhmms, List<Integer> obs) {
    	
    	double[] specieProbabilty = new double[Allhmms.size()];

            double totalProbabilty = 0;
            double normalizer = Allhmms.get(Constants.SPECIES_BLACK_STORK).size();
            for(int j=0; j<Allhmms.get(Constants.SPECIES_BLACK_STORK).size();j++) {
                HMM tmpHMM = Allhmms.get(Constants.SPECIES_BLACK_STORK).get(j);
                if(tmpHMM != null) {
                    double tmpProbability = tmpHMM.HowLikelyIsThisObservation(obs);
                    totalProbabilty += tmpProbability;
                }
            }
            specieProbabilty[Constants.SPECIES_BLACK_STORK] = totalProbabilty/normalizer;
        	
        return specieProbabilty[Constants.SPECIES_BLACK_STORK];

    }
    
    public static boolean likelyEnough(List<List<HMM>> Allhmms, List<Integer> obs) {
        
    	int species = Constants.SPECIES_UNKNOWN;
        double max = Double.NEGATIVE_INFINITY;
               
        for (int currHMMs = 0; currHMMs< Allhmms.size(); currHMMs++) {
            int normalizer = Allhmms.get(currHMMs).size();
            if(normalizer > 0) {
	            double totalProbabilty = 0.0;
	            for (int i = 0; i<Allhmms.get(currHMMs).size();i++) {
	            	HMM tmpHMM = Allhmms.get(currHMMs).get(i);
	                if (tmpHMM != null) {
	                    double tmpProb = tmpHMM.HowLikelyIsThisObservation(obs);
	                    totalProbabilty += tmpProb;
	                }
	            }
	            double tmp = totalProbabilty/normalizer;
	            if (tmp > max){
	                max = tmp;
	                species = currHMMs;
	            }
	            //System.err.println("local max " + tmp + "specy id " + currHMMs);
            }
        }
        if (max<7.708468746136744E-58) {
        	return false;
        }
        return true;
    }

    public static int likeliestSpecies(List<List<HMM>> Allhmms, List<Integer> obs){
        int species = Constants.SPECIES_UNKNOWN;
        double max = Double.NEGATIVE_INFINITY;
        for (int currHMMs = 0; currHMMs< Allhmms.size(); currHMMs++) {
            int normalizer = Allhmms.get(currHMMs).size();
            double totalProbabilty = 0.0;
            for (int i = 0; i<Allhmms.get(currHMMs).size();i++) {
                if (Allhmms.get(currHMMs).get(i) != null) {
                    double tmpProb = Allhmms.get(currHMMs).get(i).HowLikelyIsThisObservation(obs);
                    totalProbabilty += tmpProb;
                }
            }
            double tmp = totalProbabilty/normalizer;
            if (tmp > max){
                max = tmp;
                species = currHMMs;
            }
        }
        return species;
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */

       /* if (pState.getRound() < 2){
            return cDontShoot;
        }
        Integer[] nextMoves = new Integer[pState.getNumBirds()];
        Double[] maxProbs = new Double[pState.getNumBirds()];
        for (int i = 0; i<nextMoves.length;i++){
            nextMoves[i]=0;
            maxProbs[i]=0.0;
        }


        for (int b = 0; b < pState.getNumBirds();b++){
            if (pState.getBird(b).getSeqLength()< 60){ return cDontShoot;}
            if (pState.getBird(b).isDead()){
                continue;
            }
            List<Integer> o = new ArrayList<>();
            for (int i = 0; i< pState.getBird(b).getSeqLength();i++){
                if (pState.getBird(b).getObservation(i) != -1){
                    o.add(pState.getBird(b).getObservation(i));
                }
            }
            int species = likeliestSpecies(specieHmm,o);
            List<Integer> birdMoves = new ArrayList<>();
            double maxProb = 0.0;
            int bestMove = -1;

            if (species != -1 && species != 5){
                List<HMM> hmms = specieHmm.get(species);
                for (HMM h: hmms){
                    List<Double> nextEmissionsProb = h.predictNextEmissions(o);
                    int mostProbableEmission = nextEmissionsProb.indexOf(Collections.max(nextEmissionsProb));
                    if (maxProb<nextEmissionsProb.get(mostProbableEmission)){
                        maxProb = nextEmissionsProb.get(mostProbableEmission);
                        bestMove = mostProbableEmission;
                    }
                    birdMoves.add(mostProbableEmission);
                }
                int nmrOfItems = birdMoves.size();
                Set<Integer> tmpSet = new HashSet<>();
                tmpSet.addAll(birdMoves);
                birdMoves.clear();
                birdMoves.addAll(tmpSet);
                if (nmrOfItems-birdMoves.size()>0.05*nmrOfItems && birdMoves.indexOf(bestMove)>-1){
                    nextMoves[b] = bestMove;
                    maxProbs[b] = maxProb;
                }
            }
        }

        int bestBird = Arrays.asList(maxProbs).indexOf(Collections.max(Arrays.asList(maxProbs)));
        int bestMove = nextMoves[bestBird];
        double bestProb = maxProbs[bestBird];


        if (bestProb>0.8) {
            return new Action(bestBird, bestMove);
        } else {
            return cDontShoot;
        }

        */
        if (pState.getRound() != currRound) {
            currRound = pState.getRound();
            birdPatternHMMs.clear();
        }

        // add some code that not only gets the next possible state but also
        // compares it to some hmm that are optimesd to detect the 5 different patterns
        // ie dyving circling fx and are initialized in such a way

        /*int birdsAlive = 0;
        for (int b = 0; b < pState.getNumBirds();b++){
            if (pState.getBird(b).isAlive()){
                birdsAlive++;
            }
        }*/
        
        int sequenceLength = pState.getBird(0).getSeqLength();
        //int sequenceThreshold = 100- (int) (pState.getNumBirds()*1.7);
        int sequenceThreshold = 65;
        if (sequenceLength < sequenceThreshold /*|| specieHmm.get(Constants.SPECIES_BLACK_STORK).size() == 0*/) {return cDontShoot;}
        //if (specieHmm.get(Constants.SPECIES_BLACK_STORK).size() == 0 && Math.random()<0.6) {return cDontShoot;}
        if (birdPatternHMMs.size() == 0) {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                birdPatternHMMs.add(new HMM());
            }
        }

        int mostPredictableBird = -1;
        int nextPredictedMove = -1;
        List<Integer> mostPredictableSeq = new ArrayList<>();
        //double[] predictionThresholdState = {0.45, 0.45, 0.6, 0.8, 0.8};
        //double predictionThresholdState = /*0.67*/ 1-birdsAlive/(100-sequenceLength);
        double predictionThresholdState = 0.60;

        for (int b = 0; b<pState.getNumBirds(); b++){
            if (pState.getBird(b).isDead()){
                continue;
            }

            Integer[] seqArray = new Integer[pState.getBird(b).getSeqLength()];
            for (int i = 0; i< pState.getBird(b).getSeqLength();i++){
                seqArray[i] = pState.getBird(b).getObservation(i);
            }
            List<Integer> seq = Arrays.asList(seqArray);
            HMM birdHMM = new HMM();
            birdHMM.BaumWelchTrain(seq);
            birdPatternHMMs.set(b,birdHMM);

            List<Double> nextStatesProb = birdHMM.predictNextEmissions(seq);
            int mostProbableState = nextStatesProb.indexOf(Collections.max(nextStatesProb));
            if (nextStatesProb.get(mostProbableState)>predictionThresholdState){
                mostPredictableBird = b;
                nextPredictedMove = mostProbableState;
                predictionThresholdState = nextStatesProb.get(mostProbableState);
                mostPredictableSeq = seq;
            }

        }

        // || likeliestSpecies(specieHmm,mostPredictableSeq) == Constants.SPECIES_BLACK_STORK
        if (godDamnStorkOdds(specieHmm,mostPredictableSeq)>=storkThreshold) return cDontShoot;
        
        if(!likelyEnough(specieHmm,mostPredictableSeq)) return cDontShoot;
        
        if (mostPredictableBird<0 || likeliestSpecies(specieHmm,mostPredictableSeq) == Constants.SPECIES_BLACK_STORK){
            return cDontShoot;
        } else {
            return new Action(mostPredictableBird, nextPredictedMove);
        }


        // This line would predict that bird 0 will move right and shoot at it.
        // return Action(0, MOVE_RIGHT);
    }



    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */
        int[] lGuess = new int[pState.getNumBirds()];
        guessProbabilityes = new double[pState.getNumBirds()];
        // first round identify the pattern for species  and mark them into the models and then we can guess?

        if(pState.getRound() == 0) {
            for (int i = 0; i < pState.getNumBirds(); ++i) {
                int randomValues = (int) ( Math.random() * 6);
                lGuess[i] = randomValues; // most common bird as far as I know
                //guessProbabilityes[i] = -1;
            }
        }
        else {
            // We try to guess dem species
            for(int i = 0; i < pState.getNumBirds(); i++) {
                double maxProbability = Double.NEGATIVE_INFINITY;
                int specieID = -1;
                List<Integer> seqArray = new ArrayList<Integer>();
                Bird tmpBirdperson = pState.getBird(i);

                for (int j = 0; j< tmpBirdperson.getSeqLength();j++){
                    int tmpObsv = tmpBirdperson.getObservation(j);
                    if(tmpObsv != -1) seqArray.add(tmpObsv);
                    else continue;
                }

                //if(seqArray.size()>45) {
                double[] specieProbabilty = new double[specieHmm.size()];

                for(int k = 0; k<specieHmm.size(); k++) {
                    double totalProbabilty = 0;
                    double normalizer = specieHmm.get(k).size();
                    for(int j=0; j<specieHmm.get(k).size();j++) {
                        HMM tmpHMM = specieHmm.get(k).get(j);
                        if(tmpHMM != null) {
                            double tmpProbability = tmpHMM.HowLikelyIsThisObservation(seqArray);
                            totalProbabilty += tmpProbability;
                        }
                    }
                    specieProbabilty[k] = totalProbabilty/normalizer;
                }

                specieID = argumentMax(specieProbabilty);
                //if(maxProbability > - 5.0) {
                if(specieID != -1) {
                    guessProbabilityes[i] = specieProbabilty[specieID];
                }
                else {
                    guessProbabilityes[i] = 0;
                }
                lGuess[i] = specieID;
            }
        }
        guessIds = lGuess;
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {

        // print info of what probabilities gave me the right answer to figure out some
        // threshold to put

        // if its the first round we train our guessing model on the
        // correct species
        // or we havent seen this species before then we train on it

        // for each different bird we train the model on the same types
        // try a version where we only train each model once per bird sight


        for(int i = 0; i<pSpecies.length;i++) {
            int specieOfTmpBird = pSpecies[i];

            if(specieOfTmpBird !=-1) {
                // We train dat dere model to recgonise this specie
                ArrayList<Integer> seqArray = new ArrayList<Integer>();
                Bird tmpBirdPerson = pState.getBird(i);
                for (int j = 0; j< tmpBirdPerson.getSeqLength();j++){
                    if(tmpBirdPerson.wasAlive(j)) {
                        seqArray.add(tmpBirdPerson.getObservation(j));
                    }
                }
                HMM specieHMMModelToTrain = new HMM();
               // if(seqArray.size()>70) {
                    specieHMMModelToTrain.BaumWelchTrain(seqArray);
                    List<HMM> toAddThisNewModelTo = specieHmm.get(specieOfTmpBird);
                    toAddThisNewModelTo.add(specieHMMModelToTrain);
                    specieHmm.set(specieOfTmpBird, toAddThisNewModelTo);
              //  }
            }
        }

        // print info of what probabilities gave me the right answer to figure out some
        // threshold to put
        /*
        if(pState.getRound() != 0) {
            for(int i = 0; i<pSpecies.length;i++) {
                System.err.println("The Correct specie " + pSpecies[i]);
                System.err.println("My guess " + guessIds[i]);
                System.err.println("my probabilty "  + guessProbabilityes[i]);
            }
        }
        */
        // Could need a second check if a certain bird spotting model hasn't been trained
    }

    public static int argumentMax (double[] matrix)
    {
        int id = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < matrix.length; i++) {
            double x = matrix[i];
            if (x > max) {
                max = x;
                id = i;
            }
        }
        return id;
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
