import java.io.*;
import java.util.*;

class Image_Compression{

    public static class ImageProcessing{
        
        //Attributes
        private int numRows;
        private int numCols;
        private int minVal;
        private int maxVal;
        private int newMinVal = 999999;
        private int newMaxVal = 0;
        private int[][] zeroFramedAry;
        private int[][] skeletonAry;

        //Methods
        public ImageProcessing(int r, int c, int min, int max){
            this.numRows = r;
            this.numCols = c;
            this.minVal = min;
            this.maxVal = max;

            this.zeroFramedAry = new int[this.numRows + 2][this.numCols + 2];
            this.skeletonAry = new int[this.numRows + 2][this.numCols + 2];
            for(int i = 0; i < this.numRows + 2; i++){
                for(int j = 0; j < this.numCols + 2; j++){
                    this.zeroFramedAry[i][j] = 0;
                    this.skeletonAry[i][j] = 0;
                }
            }
        }

        public void setZero(int[][] a){
            for(int i = 0; i < a.length; i++){
                for(int j = 0; j < a[i].length; j++){
                    a[i][j] = 0;
                }
            }
        }

        public void loadImage(Scanner imgFile){
            for(int i = 1; i < this.numRows + 1; i++){
                for(int j = 1; j < this.numCols + 1; j++){
                    this.zeroFramedAry[i][j] = imgFile.nextInt();
                }
            }
        }

        public void Distance8(PrintWriter out, PrintWriter debugFile){
            debugFile.print("Entering Distance8\n");

            this.Distance8Pass1();
            out.println("Distnce8Pass1");
            this.reformatPrettyPrint(this.zeroFramedAry, out);
            out.println("\n");

            this.Distance8Pass2();
            out.println("DistancePass2");
            this.reformatPrettyPrint(this.zeroFramedAry, out);
            out.println("\n");

            debugFile.println("Leaving Distance8");
        }

        public void Distance8Pass1(){
            for(int i = 1; i < this.numRows - 1; i++){
                for(int j = 1; j < this.numCols - 1; j++){
                    if(this.zeroFramedAry[i][j] > 0){
                        this.zeroFramedAry[i][j] = min1(i, j) + 1;
                    }
                }
            }
        }

        public int min1(int i, int j){

            int[] neighborAry = new int[4]; 

            neighborAry[0] = this.zeroFramedAry[i - 1][j - 1];
            neighborAry[1] = this.zeroFramedAry[i - 1][j];
            neighborAry[2] = this.zeroFramedAry[i - 1][j + 1];
            neighborAry[3] = this.zeroFramedAry[i][j - 1];

            int mn = 999999;
            for(int k = 0; k < 4; k++){
                if(neighborAry[k] < mn)
                {
                    mn = neighborAry[k];
                }
            }

            return mn;
        }

        public void Distance8Pass2(){
            for(int i = this.numRows; i > 0; i--){
                for(int j = this.numCols; j > 0; j--){
                    if(this.zeroFramedAry[i][j] > 0){
                        this.zeroFramedAry[i][j] = min2(i, j);
                    }
                }
            }

            for(int i = 1; i < this.numRows + 1; i++){
                for(int j = 1; j < this.numCols + 1; j++){
                    if(this.zeroFramedAry[i][j] < this.newMinVal){
                        this.newMinVal = this.zeroFramedAry[i][j];
                    }

                    if(this.zeroFramedAry[i][j] > this.newMaxVal){
                        this.newMaxVal = this.zeroFramedAry[i][j];
                    }
                }
            }
        }

        public int min2(int i, int j){
            int[] neighborAry = new int[4]; 

            neighborAry[0] = this.zeroFramedAry[i][j + 1] + 1;
            neighborAry[1] = this.zeroFramedAry[i + 1][j - 1] + 1;
            neighborAry[2] = this.zeroFramedAry[i + 1][j] + 1;
            neighborAry[3] = this.zeroFramedAry[i + 1][j + 1] + 1;

            int mn = this.zeroFramedAry[i][j];
            for(int k = 0; k < 4; k++){
                if(neighborAry[k] < mn)
                {
                    mn = neighborAry[k];
                }
            }

            return mn;
        }

        public Boolean isLocalMaxima(int i, int j){

            if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i - 1][j - 1]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i - 1][j]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i - 1][j + 1]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i][j - 1]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i][j + 1]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i + 1][j - 1]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i + 1][j]){
                return false;
            }
            else if(this.zeroFramedAry[i][j] < this.zeroFramedAry[i + 1][j + 1]){
                return false;
            }

            
            return true;
        }

        public void localMaxima(){
            for(int i = 1; i < this.numRows - 1; i++){
                for(int j = 1; j < this.numCols - 1; j++){
                    if(isLocalMaxima(i, j) == true){
                        this.skeletonAry[i][j] = this.zeroFramedAry[i][j];
                    }
                    else{
                        this.skeletonAry[i][j] = 0;
                    }
                }
            }
        }

        public void skeletonExtraction(PrintWriter out, PrintWriter debug, PrintWriter skeleton){
            debug.println("Entering skeletonExtraction");

            this.localMaxima();
            out.println("skeletonExtraction");
            this.reformatPrettyPrint(this.skeletonAry, out);
            out.println("\n");

            compression(skeleton);

            debug.println("Leaving skeletonExtraction");
        }

        public void compression(PrintWriter skeleton){
            skeleton.println(this.numRows + " " + this.numCols + " " + this.newMinVal + " " + this.newMaxVal);

            for(int i = 1; i < this.numRows - 1; i++){
                for(int j = 1; j < this.numCols - 1; j++){
                    if(this.skeletonAry[i][j] > 0){
                        skeleton.println(i + " " + j + " " + this.skeletonAry[i][j]);
                    }
                }
            }
        }

        public void deCompression(PrintWriter out, PrintWriter debug, Scanner skeleton){
            debug.println("Entering deCompression Method");

            this.setZero(this.zeroFramedAry);
            this.load(skeleton);
            
            this.expansionPass1();
            out.println("Expansion1");
            this.reformatPrettyPrint(this.zeroFramedAry, out);
            out.println("\n");

            this.expansionPass2();
            out.println("Expansion2");;
            this.reformatPrettyPrint(this.zeroFramedAry, out);
            out.println("\n");

            debug.println("Leaving deCompression Method");
        }

        public void load(Scanner skeleton){
            int i, j, val;

            String header = skeleton.nextLine();

            while(skeleton.hasNextInt()){
                i = skeleton.nextInt();
                j = skeleton.nextInt();
                val = skeleton.nextInt();

                this.zeroFramedAry[i][j] = val;
            }
        }

        public void expansionPass1(){
            for(int i = 1; i < this.numRows - 1; i++){
                for(int j = 1; j < this.numCols - 1; j++){
                    if(this.zeroFramedAry[i][j] == 0){
                        this.zeroFramedAry[i][j] = max(i, j);
                    }
                }
            }
        }

        public int max(int i, int j){

            int[] neighborAry = new int[8]; 

            neighborAry[0] = this.zeroFramedAry[i - 1][j - 1] - 1;
            neighborAry[1] = this.zeroFramedAry[i - 1][j] - 1;
            neighborAry[2] = this.zeroFramedAry[i - 1][j + 1] - 1;
            neighborAry[3] = this.zeroFramedAry[i][j - 1] - 1;
            neighborAry[4] = this.zeroFramedAry[i][j + 1] - 1;
            neighborAry[5] = this.zeroFramedAry[i + 1][j - 1] - 1;
            neighborAry[6] = this.zeroFramedAry[i + 1][j] - 1;
            neighborAry[7] = this.zeroFramedAry[i + 1][j + 1] - 1;

            int mx = this.zeroFramedAry[i][j];
            for(int k = 0; k < 8; k++){
                if(neighborAry[k] > mx)
                {
                    mx = neighborAry[k];
                }
            }

            return mx;
        }

        public void expansionPass2(){
            for(int i = this.numRows; i > 0; i--){
                for(int j = this.numCols; j > 0; j--){
                    int mx = max(i, j);
                    if(this.zeroFramedAry[i][j] < mx){
                        this.zeroFramedAry[i][j] = mx - 1;
                    }
                }
            }
        }

        public void binaryThreshold(){
            for(int i = 0; i < this.zeroFramedAry.length; i++){
                for(int j = 0; j < this.zeroFramedAry[i].length; j++){
                    if(this.zeroFramedAry[i][j] >= 1){
                        this.zeroFramedAry[i][j] = 1;
                    }
                    else{
                        this.zeroFramedAry[i][j] = 0;
                    }
                }
            }
        }

        public void ary2File(PrintWriter out){
            out.println(this.numRows + " " + this.numCols + " " + this.minVal + " " + this.maxVal);

            for(int i = 0; i < this.numRows + 2; i++){
                for(int j = 0; j < this.numCols + 2; j++){
                    if(this.zeroFramedAry[i][j] == 0){
                        out.print(". ");
                    }
                    else{
                        out.print("1 ");
                    }
                }
                out.print("\n");
            }            
        }

        public void reformatPrettyPrint(int[][] a, PrintWriter out){
            for(int i = 0; i < a.length; i++){
                for(int j = 0; j < a[i].length; j++){
                    if(a[i][j] == 0){
                        out.print(". ");
                    }
                    else{
                        out.print(a[i][j] + " ");
                    }
                }
                out.print("\n");
            }
        }
    }

    public static void main(String args[]) throws FileNotFoundException{

        //Opening All Files
        File inFile = new File(args[0]);
        Scanner imgFile = new Scanner(inFile);
        PrintWriter outFile1 = new PrintWriter(args[1]);
        PrintWriter outFile2 = new PrintWriter(args[2]);
        PrintWriter debugFile = new PrintWriter(args[3]);

        //Reading the header for the image file
	    int iRows = imgFile.nextInt();
	    int iCols = imgFile.nextInt();
	    int iMin = imgFile.nextInt();
	    int iMax = imgFile.nextInt();

        //initiating ImageProcessing object and dynamically allocating all the arrays
        ImageProcessing imgPro = new ImageProcessing(iRows, iCols, iMin, iMax);

        String skeletonFileName = args[0] + "_skeleton.txt";
        PrintWriter skeletonFile1 = new PrintWriter(skeletonFileName);

        String deCompressFileName = args[0] + "_decompressed.txt";
        PrintWriter deCompressFile = new PrintWriter(deCompressFileName);

        imgPro.loadImage(imgFile);
        imgPro.Distance8(outFile1, debugFile);
        imgPro.skeletonExtraction(outFile1, debugFile, skeletonFile1);
        skeletonFile1.close();

        File skFile = new File(skeletonFileName);
        Scanner skeletonFile2 = new Scanner(skFile);
        imgPro.deCompression(outFile2, debugFile, skeletonFile2);
        imgPro.binaryThreshold();
        imgPro.ary2File(deCompressFile);
        
        //Close all files
        imgFile.close();
        outFile1.close();
        outFile2.close();
        debugFile.close();
        deCompressFile.close();
        skeletonFile2.close();
    }

};