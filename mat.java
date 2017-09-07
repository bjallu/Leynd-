import java.util.*;

public class mat {

    private List<List<Double>> matrix;
    private int nmrOfColumns;
    private int nmrOfRows;

    public mat(){
        nmrOfColumns=0;
        nmrOfRows=0;
        matrix = new ArrayList<>();
    }

    public mat(int N,int M){
        nmrOfRows = N;
        nmrOfColumns = M;
        matrix = new ArrayList<>();
        for (int i = 0; i<N; i++){
            Double[] arr = new Double[M];
            ArrayList<Double> row = new ArrayList<>(Arrays.asList(arr));
            Collections.fill(row, 0.0);//fills all M entries with 0
            matrix.add(row);
        }
    }

    /*
    Build matrix from Kattis' input lines
    matDesc is on the format:
    1 3 0.3 0.3 0.4
     */
    public mat (String matDesc){
        String[] i = matDesc.split(" ");
        List<String> items = Arrays.asList(i);
        nmrOfRows = Integer.parseInt(items.get(0));
        nmrOfColumns = Integer.parseInt(items.get(1));
        items = items.subList(2,items.size());
        int currRow = 0;
        matrix = new ArrayList<>();
        while(currRow<nmrOfRows){
            List<String> tmpStr = new ArrayList<>(items.subList(currRow*nmrOfColumns,(currRow+1)*nmrOfColumns));
            List<Double> tmpDouble = new ArrayList<>();
            for(String s: tmpStr) tmpDouble.add(Double.valueOf(s));
            matrix.add(tmpDouble);
            currRow++;
        }
    }

    public Double getElement(int i, int j){
        return matrix.get(i).get(j);
    }

    public void setElement(int i, int j, Double e){
        matrix.get(i).set(j,e);
    }

    public List<Double> getRow(int i){
        return matrix.get(i);
    }


    public int getNmrOfColumns() {
        return nmrOfColumns;
    }

    public int getNmrOfRows() {
        return nmrOfRows;
    }

    public mat product(mat multBy){
        mat result = new mat(this.getNmrOfRows(),multBy.getNmrOfColumns());
        if (this.getNmrOfColumns() != multBy.getNmrOfRows()) {
            throw new java.lang.RuntimeException("Dimensions do not match!");
        } else {
            for (int i = 0; i<this.getNmrOfRows();i++) {
                for (int col=0;col<multBy.getNmrOfColumns();col++){
                    for (int j = 0; j<multBy.getNmrOfRows();j++){
                        result.setElement(i,col,result.getElement(i,col)+this.getElement(i,j)*multBy.getElement(j,col));
                    }
                }

            }
        }
        return result;
    }

    public mat dotProduct(mat multBy){
        mat result = new mat(this.getNmrOfRows(),multBy.getNmrOfColumns());
        if (this.getNmrOfColumns() != multBy.getNmrOfColumns() || this.getNmrOfRows()!=1 || multBy.getNmrOfRows() != 1) {
            throw new java.lang.RuntimeException("Dimensions do not match!");
        } else {
            for (int i=0;i<this.getNmrOfColumns();i++){
                result.setElement(0,i,this.getElement(0,i)*multBy.getElement(0,i));
            }
        }
        return result;
    }

    public mat dotProductColumn(mat multBy, int col){
        mat result = new mat(this.getNmrOfRows(),multBy.getNmrOfColumns());
        if (this.getNmrOfColumns() != multBy.getNmrOfRows()|| this.getNmrOfRows()!=1) {
            throw new java.lang.RuntimeException("Dimensions do not match!");
        } else {
            for (int i = 0; i<this.getNmrOfRows();i++) {
                for (int j = 0; j<multBy.getNmrOfRows();j++){
                    result.setElement(i,col,result.getElement(i,col)+this.getElement(i,j)*multBy.getElement(j,col));
                }
            }
        }
        return result;
    }

    public Double sumElements(){
        Double sum = 0.0;
        if (this.getNmrOfRows() != 1){
            throw new java.lang.RuntimeException("Must be have dimensions 1XM");
        } else {
            for (Double d:matrix.get(0)){
                sum += d;
            }
        }
        return sum;
    }

    public void printMatrix(){
        for (List v:this.matrix){
            System.out.println(Arrays.toString(v.toArray()));
        }
    }

    public static void main(String[] args){
        mat m = new mat(3,3);
        mat n = new mat(3,3);
        m.setElement(0,0,1.0);
        m.setElement(0,1,2.0);
        m.setElement(0,2,3.0);
        n.setElement(0,0,1.0);
        n.setElement(1,0,2.0);
        n.setElement(2,0,3.0);

        mat f = new mat(1,3);
        mat g = new mat(1,3);
        f.setElement(0,0,1.0);
        f.setElement(0,1,2.0);
        f.setElement(0,2,3.0);
        g.setElement(0,0,1.0);
        g.setElement(0,1,2.0);
        g.setElement(0,2,3.0);

        mat matFromString = new mat("2 3 1 1 1 2 2 2");
        matFromString.product(m).printMatrix();

        mat res = m.product(n);
        mat res2 = f.dotProduct(g);
        res.printMatrix();
        res2.printMatrix();
    }

}
