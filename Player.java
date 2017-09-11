import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

class Player {

    private List<HMM> birdPatternHMMs = new ArrayList<>();
    int currRound = -1;
    private final int NMR_OF_STATES = 5;

    public Player() {
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

        int sequenceLength = pState.getBird(0).getSeqLength();
        if (sequenceLength < 35) {return cDontShoot;}

        if (birdPatternHMMs.size() == 0) {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                birdPatternHMMs.add(new HMM());
            }
        }

        int mostPredictableBird = -1;
        int nextPredictedMove = -1;
        double predictionThresholdState = 0.65;

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

            List<Double> nextStatesProb = birdHMM.predictNextEmissions();
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
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;
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
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
