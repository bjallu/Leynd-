import java.util.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class HMM1 {

    public static mat calcAlphaPass(mat A, mat B, mat pi, List<String> obs){
        mat currAlpha = new mat(pi.getNmrOfRows(),pi.getNmrOfColumns());
        boolean isFirst = true;
        for (String o:obs){
            int oInt = Integer.parseInt(o);
            if (isFirst){
                currAlpha = pi.dotProductColumn(B,oInt);
                isFirst = !isFirst;
            } else {
                currAlpha = currAlpha.product(A);
                currAlpha = currAlpha.dotProductColumn(B,oInt);
            }
        }
        return currAlpha;
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

        mat alpha = calcAlphaPass(A,B,pi,obs);
        //DecimalFormat df = new DecimalFormat("#.######");
        //df.setRoundingMode(RoundingMode.CEILING);
        //System.out.println(df.format(alpha.sumElements()));
        System.out.println(alpha.sumElements());

    }
}
