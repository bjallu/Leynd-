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
        matrix = new ArrayList<>(N);
        for (int i = 0; i<N; i++){
            Double[] arr = new Double[M];
            ArrayList<Double> row = new ArrayList<>(Arrays.asList(arr));
            Collections.fill(row, 0.0);//fills all M entries with 0
            matrix.add(row);
        }
    }

    public mat(int N,int M,Double initialValue) {
        nmrOfRows = N;
        nmrOfColumns = M;
        matrix = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            Double[] arr = new Double[M];
            ArrayList<Double> row = new ArrayList<>(Arrays.asList(arr));
            Collections.fill(row, initialValue);//fills all M entries with 0
            matrix.add(row);
        }
    }
    /*
    Build matrix from Kattis' input lines
    matDesc is on the format:
    1 3 0.3 0.3 0.4
     */
    public mat (String matDesc){
        matDesc = matDesc.trim();
        String[] i = matDesc.split(" ");
        List<String> items = Arrays.asList(i);
        nmrOfRows = Integer.parseInt(items.get(0));
        nmrOfColumns = Integer.parseInt(items.get(1));
        items = items.subList(2,items.size());
        int currRow = 0;
        matrix = new ArrayList<>(nmrOfRows);
        while(currRow<nmrOfRows){
            List<String> tmpStr = new ArrayList<>(items.subList(currRow*nmrOfColumns,(currRow+1)*nmrOfColumns));
            List<Double> tmpDouble = new ArrayList<>(nmrOfColumns);
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

    public void setRow(int rowNmr, List<Double> row){
        if (row.size() == this.getNmrOfColumns()){
            matrix.set(rowNmr, row);
        }
    }

    public List<Double> getRow(int i){
        return matrix.get(i);
    }

    public mat getColAsMat(int col){
        mat result = new mat(this.getNmrOfRows(),1);
        for (int i = 0;i<matrix.size();i++){
            result.setElement(i,1,this.getElement(i,col));
        }
        return result;
    }

    public mat RowAsCol() {
        mat result = new mat(this.getNmrOfColumns(),1);
        if (this.getNmrOfRows() != 1) {
            throw new java.lang.RuntimeException("Needs to be a single row matrix (row vector)");
        } else {
            for (int i=0; i<this.getNmrOfColumns();i++){
                result.setElement(i,0,this.getElement(0,i));
            }
        }
        return result;
    }

    public mat ColAsRow() {
        mat result = new mat(1,this.getNmrOfRows());
        if (this.getNmrOfColumns() != 1) {
            throw new java.lang.RuntimeException("Needs to be a single column matrix (column vector)");
        } else {
            for (int i=0; i<this.getNmrOfRows();i++){
                result.setElement(0,i,this.getElement(i,0));
            }
        }
        return result;
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
        mat result = new mat(this.getNmrOfRows(),this.getNmrOfColumns());
        if (this.getNmrOfColumns() != multBy.getNmrOfRows()|| this.getNmrOfRows()!=1) {
            throw new java.lang.RuntimeException("Dimensions do not match!");
        } else {
            for (int j = 0; j<multBy.getNmrOfRows();j++){
                result.setElement(0,j,result.getElement(0,j)+this.getElement(0,j)*multBy.getElement(j,col));
            }
        }
        return result;
    }

    public Double sumElements(){
        Double sum = 0.0;
        int scale = (int) Math.pow(10, 6); // prufa med scale 7 ef etta virkar ekki
        if (this.getNmrOfRows() != 1){
            throw new java.lang.RuntimeException("Must have dimensions 1XM");
        } else {
            for (Double d:matrix.get(0)){
                sum += (double) Math.round(d * scale) / scale;
            }
        }
       // double roundedSum = (double) Math.round(sum * scale) / scale;
        return sum;
    }

    public void printMatrix(){
        for (List v:this.matrix){
            System.out.println(Arrays.toString(v.toArray()));
        }
        System.out.println();
    }
    
    public void printMatrixForKattis() {
    	// First print the matrix dimensions then loop through the values
    	// And round them    	
    	System.out.print(this.getNmrOfRows() + " "); // Could also just call the element right away i.e. nmrOfRows but calling the func is cleaner imo
    	System.out.print(this.getNmrOfColumns() + " ");
    	
        for(int i = 0; i < this.matrix.size(); i++){
            for(int j = 0; j < this.matrix.get(i).size(); j++){
                double value = this.matrix.get(i).get(j);
                int scale = (int) Math.pow(10, 2);
                double roundedValue = (double) Math.round(value * scale) / scale;
        		System.out.print(roundedValue + " ");
            }
        }
    }

    public void printMatrixForKattis2() {
        // First print the matrix dimensions then loop through the values
        // And round them
        String str = this.getNmrOfRows()+" "+this.getNmrOfColumns();

        for(int i = 0; i < this.matrix.size(); i++){
            for(int j = 0; j < this.matrix.get(i).size(); j++){
                double value = this.matrix.get(i).get(j);
                str = str + " " + value;
            }
        }
        System.out.println(str);
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

        mat matFromString = new mat("0 3");
        matFromString.product(m).printMatrix();

        mat res = m.product(n);
        mat res2 = f.dotProduct(g);
        res.printMatrix();
        res2.printMatrix();
    }

}
