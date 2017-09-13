import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

class Player {

    private List<HMM> birdPatternHMMs = new ArrayList<>();
    int currRound = -1;
    // we need as many hmms as there are species to try to distinguish them apart
    // List<HMM> DifferentBirdSpecies = new ArrayList<>(Constants.COUNT_SPECIES);
    HMM[] DifferentBirdSpecies = new HMM[Constants.COUNT_SPECIES];

    public Player() {
    	
    	for(int i = 0; i<Constants.COUNT_SPECIES; i++) {
    		DifferentBirdSpecies[i] = new HMM();
    	}
    	
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


        if (pState.getRound() != currRound) {
            currRound = pState.getRound();
            birdPatternHMMs.clear();
        }
        
        // add some code that not only gets the next possible state but also 
        // compares it to some hmm that are optimesd to detect the 5 different patterns
        // ie dyving circling fx and are initialized in such a way

        int sequenceLength = pState.getBird(0).getSeqLength();
        List<Integer> seq = new ArrayList<>();
        //if (sequenceLength < (int) (100-pState.getNumBirds()*1.6)) {return cDontShoot;}
        if (sequenceLength < 65) {return cDontShoot;}

        if (birdPatternHMMs.size() == 0) {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                birdPatternHMMs.add(new HMM());
            }
        }


        int mostPredictableBird = -1;
        int nextPredictedMove = -1;
        double predictionThresholdState = 0.67;

        for (int b = 0; b<pState.getNumBirds(); b++){
            if (pState.getBird(b).isDead()){
                continue;
            }

            Integer[] seqArray = new Integer[pState.getBird(b).getSeqLength()];
            for (int i = 0; i< pState.getBird(b).getSeqLength();i++){
                seqArray[i] = pState.getBird(b).getObservation(i);
            }
            seq = Arrays.asList(seqArray);
            HMM birdHMM = new HMM();
            birdHMM.BaumWelchTrain(seq);
            birdPatternHMMs.set(b,birdHMM);

            List<Double> nextStatesProb = birdHMM.predictNextEmissions(seq);
            int mostProbableState = nextStatesProb.indexOf(Collections.max(nextStatesProb));
            if (nextStatesProb.get(mostProbableState)>predictionThresholdState){
                mostPredictableBird = b;
                nextPredictedMove = mostProbableState;
                predictionThresholdState = nextStatesProb.get(mostProbableState);
            }
        }

        if (mostPredictableBird<0){
            return cDontShoot;
        } else {
            return new Action(mostPredictableBird,nextPredictedMove);
        }
        //return cDontShoot;


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

        List<Integer> seq = new ArrayList<>();


		int[] lGuess = new int[pState.getNumBirds()];
		double guessThreshold = 0.65;
    	// first round identify the pattern for species  and mark them into the models and then we can guess?

        for (int i = 0; i < pState.getNumBirds(); i++) {
            lGuess[i] = Constants.SPECIES_UNKNOWN; // most common bird as far as I know
        }

    	if(pState.getRound() == 0) {

    	}
    	else {
    		// We try to guess dem species
    		for(int i = 0; i < pState.getNumBirds(); i++) {  			
    			double maxProbability = 0;
    			int specieID = 0;    			
                List<Integer> seqArray = new ArrayList<Integer>();
                for (int j = 0; j< pState.getBird(i).getSeqLength();j++){
                	int tmpObsv = pState.getBird(i).getObservation(j);
                	if(tmpObsv != -1) seqArray.add(tmpObsv);
                	else continue; 
                }			
    			for(int k = 0; k<DifferentBirdSpecies.length; k++) {
    				HMM tmpHMM = DifferentBirdSpecies[k];
	    				if(tmpHMM != null) {
		    				double tmpProbability = tmpHMM.HowLikelyIsThisObservation(seqArray);
		    				if(tmpProbability > maxProbability) {
		    					maxProbability = tmpProbability;
		    					specieID = k;
		    				}
	    				}
    			}
    			if (maxProbability>=guessThreshold) {
                    lGuess[i] = specieID;
                } else {
                    lGuess[i] = Constants.SPECIES_UNKNOWN;
                }
    		}
    	}
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
    	
    	// if its the first round we train our guessing model on the
    	// correct species
    	// or we havent seen this species before then we train on it 
    		
		// for each different bird we train the model on the same types
		for(int i = 0; i<pState.getNumBirds(); i++) {
			
			Bird tmpBirdPerson = pState.getBird(i);
			int specieOftmpBird = pSpecies[i];
            Integer[] seqArray = new Integer[tmpBirdPerson.getSeqLength()];
            
            if(!tmpBirdPerson.isDead()) {           
	            for (int j = 0; j< tmpBirdPerson.getSeqLength();j++){	            	
	                seqArray[j] = tmpBirdPerson.getObservation(j);
	            }                    
	            List<Integer> seq = Arrays.asList(seqArray);
                DifferentBirdSpecies[specieOftmpBird].BaumWelchTrain(seq);
            }
		}
        // Could need a second check if a certain bird spotting model hasn't been trained
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
