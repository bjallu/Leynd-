import java.util.*;

public class HMM4 {

    public static class lambda {
        private mat A;
        private mat B;
        private mat pi;

        private lambda(mat A,mat B,mat pi){
            this.A=A;
            this.B=B;
            this.pi=pi;
        }

        private void setA(mat newA){
            this.A = newA;
        }

        private void setB(mat newB){
            this.B = newB;
        }

        private void setPi(mat newPi){
            this.pi = newPi;
        }

        private mat getA(){
            return this.A;
        }

        private mat getB(){
            return this.B;
        }

        private mat getPi(){
            return this.pi;
        }
    }

    public static mat alphaMatrix(mat A, mat B, mat pi, List<String> obs){
        mat alpha = new mat(obs.size(),pi.getNmrOfColumns());
        mat currAlpha = new mat(pi.getNmrOfRows(),pi.getNmrOfColumns());
        boolean isFirst = true;
        for (int ind = 0; ind<obs.size();ind++){
            int oInt = Integer.parseInt(obs.get(ind));
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

    public static mat betaMatrix(mat A, mat B, List<String> obs){
        mat beta = new mat(obs.size(),A.getNmrOfRows());
        mat currBeta = new mat(1,A.getNmrOfColumns(),1.0);
        beta.setRow(obs.size()-1,currBeta.getRow(0));
        for (int ind = obs.size()-2;ind >= 0;ind--){
            int oInt = Integer.parseInt(obs.get(ind));
            mat dot = currBeta.dotProductColumn(B,oInt);
            currBeta = A.product(dot.RowAsCol()).ColAsRow();
            beta.setRow(ind,currBeta.getRow(0));
        }
        return beta;
    }

    public static mat betaMatrix2(mat A, mat B, List<String> obs){
        mat beta = new mat(obs.size(),A.getNmrOfRows());
        for (int i = 0; i<A.getNmrOfColumns();i++){
            beta.setElement(obs.size()-1,i,1.0);
        }
        for (int t = obs.size()-2;t>=0;t--){
            int o = Integer.parseInt(obs.get(t+1));
            Double ct = 0.0;
            for (int i = 0; i<A.getNmrOfColumns();i++){
                Double currBeta = 0.0;
                for (int j = 0;j<A.getNmrOfColumns();j++){
                    currBeta += A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j);
                    ct += currBeta;
                }
                ct = 1/ct;
                currBeta = ct*currBeta;
                beta.setElement(t,i,currBeta);
            }
        }
        return beta;
    }

    public static lambda BaumWelch(mat A, mat B, mat pi, List<String> obs){
        List<mat> digamma = new ArrayList<>();
        mat gamma = new mat(obs.size(),A.getNmrOfRows());
        List<Double> c = new ArrayList<>();
        mat beta = new mat(obs.size(),A.getNmrOfRows());
        mat alpha = new mat(obs.size(),pi.getNmrOfColumns());
        int maxIters = 1000;
        int iters = 0;
        Double oldLogProb = Double.NEGATIVE_INFINITY;

        whileloop:
        while (iters<maxIters){

            // calc alpha
            mat currAlpha = new mat(pi.getNmrOfRows(),pi.getNmrOfColumns());
            boolean isFirst = true;
            for (int ind = 0; ind<obs.size();ind++){
                int oInt = Integer.parseInt(obs.get(ind));
                Double ct = 0.0;
                if (isFirst){
                    currAlpha = pi.dotProductColumn(B,oInt);
                    for (Double d : currAlpha.getRow(0)){
                        ct += d;
                    }
                    ct = 1/ct;
                    c.add(ct);
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
                    c.add(ct);
                    for (int i=0;i<pi.getNmrOfColumns();i++){
                        currAlpha.setElement(0,i,ct*currAlpha.getElement(0,i));
                    }
                    alpha.setRow(ind, currAlpha.getRow(0));
                }
            }

            // calc beta
            for (int i = 0; i<A.getNmrOfColumns();i++){
                beta.setElement(obs.size()-1,i,1.0);
            }
            for (int t = obs.size()-2;t>=0;t--){
                int o = Integer.parseInt(obs.get(t+1));
                for (int i = 0; i<A.getNmrOfColumns();i++){
                    Double currBeta = 0.0;
                    for (int j = 0;j<A.getNmrOfColumns();j++){
                        currBeta += A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j);
                    }
                    currBeta = c.get(t)*currBeta;
                    beta.setElement(t,i,currBeta);
                }
            }

            // calc digamma and gamma
            for (int t = 0; t<obs.size()-1;t++){
                int o = Integer.parseInt(obs.get(t+1));
                Double denom = 0.0;
                for (int i = 0; i<A.getNmrOfColumns();i++){
                    for (int j = 0; j<A.getNmrOfColumns();j++){
                        denom += alpha.getElement(t,i)*A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j);
                    }
                }
                mat digammaMat = new mat(A.getNmrOfColumns(),A.getNmrOfColumns());
                for (int i = 0;i<A.getNmrOfColumns();i++){
                    Double currGamma = 0.0;
                    for (int j=0;j<A.getNmrOfColumns();j++){
                        Double currDiGamma = alpha.getElement(t,i)*A.getElement(i,j)*B.getElement(j,o)*beta.getElement(t+1,j)/denom;
                        currGamma += currDiGamma;
                        digammaMat.setElement(i,j,currDiGamma);
                    }
                    gamma.setElement(t,i,currGamma);
                }
                digamma.add(digammaMat);
            }

            Double denom = 0.0;
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
                    Double tmpNumer = 0.0;
                    Double tmpDenom = 0.0;
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
                    Double tmpNumer = 0.0;
                    Double tmpDenom = 0.0;
                    for (int t = 0; t<obs.size();t++){
                        int o = Integer.parseInt(obs.get(t));
                        if (j == o){
                            tmpNumer += gamma.getElement(t,i);
                        }
                        tmpDenom += gamma.getElement(t,i);
                    }
                    B.setElement(i,j,tmpNumer/tmpDenom);
                }
            }

            // compute log[P(O|lambda)]
            Double logProb = 0.0;
            for (int t = 0;t<obs.size();t++){
                logProb += Math.log(c.get(t));
            }
            logProb = -1*logProb;

            iters++;
            if (iters<maxIters && logProb>oldLogProb){
                oldLogProb = logProb;
            } else {
                break whileloop;
            }
        }
        lambda l = new lambda(A,B,pi);
        return l;
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

        lambda l = BaumWelch(A,B,pi,obs);
        l.getA().printMatrix();
        l.getB().printMatrix();
    }
}
