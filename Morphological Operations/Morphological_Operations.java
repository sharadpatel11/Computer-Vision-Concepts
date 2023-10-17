import java.io.*;
import java.util.*;

public class Morphological_Operations{

    public static class Morphology{
        private int numImgRows, numImgCols, imgMin, imgMax, numStructRows, numStructCols, structMin, structMax, rowOrigin, colOrigin, rowFrameSize, colFrameSize, extraRows, extraCols, rowSize, colSize;
        protected int[][] zeroFramedAry, morphAry, tempAry, structAry;

        public Morphology(int iRows, int iCols, int iMin, int iMax, int sRows, int sCols, int sMin, int sMax,  int rOrigin, int cOrigin){
            this.numImgRows = iRows;
            this.numImgCols = iCols;
            this.imgMin = iMin;
            this.imgMax = iMax;
            this.numStructRows = sRows;
            this.numStructCols = sCols;
            this.structMin = sMin;
            this.structMax = sMax;
            this.rowOrigin = rOrigin;
            this.colOrigin = cOrigin;
            this.rowFrameSize = sRows/2;
            this.colFrameSize = sCols/2;
            this.extraRows = this.rowFrameSize * 2;
            this.extraCols = this.colFrameSize * 2;
            this.rowSize = this.numImgRows + this.extraRows;
            this.colSize = this.numImgCols + this.extraCols;

            this.zeroFramedAry = new int[this.rowSize][this.colSize];
            this.morphAry = new int[this.rowSize][this.colSize];
            this.tempAry = new int[this.rowSize][this.colSize];
            this.structAry = new int[sRows][sCols];

            for(int i = 0; i < this.rowSize; i++){
                for(int j = 0; j < this.colSize; j++){
                    this.zeroFramedAry[i][j] = 0;
                    this.morphAry[i][j] = 0;
                    this.tempAry[i][j] = 0;
                }
            }

            for(int i = 0; i < sRows; i++){
                for(int j = 0; j < sCols; j++){
                    this.structAry[i][j] = 0;
                }
            }
        }

        public void zero2DAry(int a[][], int row, int col){
            for(int i = 0; i < row; i++){
                for(int j = 0; j < col; j++){
                    a[i][j] = 0;
                }
            }
        }

        public void loadImg(Scanner imgFile){
            for(int i = this.rowOrigin; i <= this.numImgRows; i++){
                for(int j = this.colOrigin; j <= this.numImgCols; j++){
                    this.zeroFramedAry[i][j] = imgFile.nextInt();
                }
            }
        }

        public void loadStruct(Scanner elmFile){
            for(int i = 0; i < this.numStructRows; i++){
                for(int j = 0; j < this.numStructCols; j++){
                    this.structAry[i][j] = elmFile.nextInt();
                }
            }
        }

        public void imgReformat(int a[][]){

        }

        public void prettyPrint(int a[][], PrintWriter outFile){
            for(int i = 0; i < a.length; i++){
                for(int j = 0; j < a[i].length; j++){
                    if(a[i][j] == 0){
                        outFile.print(". ");
                    }
                    else{
                        outFile.print("1 ");
                    }
                }
                outFile.print("\n");
            }
        }

        public void basicOperations(int[][] zfAry, int[][] mAry, int[][] sAry, int[][] tAry, PrintWriter out){
            out.println("\nEntering basicOperations method");

            //Performing dilation
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.computeDilation(zfAry, mAry, sAry);
            out.println("\nPrinting result of computeDilation");
            this.prettyPrint(mAry, out);

            //performing erosion
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.computeErosion(zfAry, mAry, sAry);
            out.println("\nPrinting result of computeErosion");
            this.prettyPrint(mAry, out);

            //performing opening
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.computeOpening(zfAry, mAry, sAry, tAry);
            out.println("\nPrinting result of computeOpening");
            this.prettyPrint(mAry, out);

            //perofming closing
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.zero2DAry(tAry, tAry.length, tAry[0].length);
            this.computeClosing(zfAry, mAry, sAry, tAry);
            out.println("\nPrinting result of computeClosing");
            this.prettyPrint(mAry, out);

            out.println("\nExiting basicOperations method");
        }

        public void complexOperation(int[][] zfAry, int[][] mAry, int[][] sAry, int[][] tAry, PrintWriter out){
            out.println("Entering complexOperations method");

            //performing opening
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.zero2DAry(tAry, tAry.length, tAry[0].length);
            this.computeOpening(zfAry, mAry, sAry, tAry);
            out.println("\nPretty print result of opening");
            this.prettyPrint(mAry, out);
            this.copyArys(zfAry, mAry);

            //perofming closing
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.computeClosing(zfAry, mAry, sAry, tAry);
            out.println("\nPretty print result of opening follow by closing");
            this.prettyPrint(mAry, out);
            this.copyArys(zfAry, mAry);

            //perofming closing
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.zero2DAry(tAry, tAry.length, tAry[0].length);
            this.computeClosing(zfAry, mAry, sAry, tAry);
            out.println("\nPretty print result of closing");
            this.prettyPrint(mAry, out);
            this.copyArys(zfAry, mAry);

            //performing opening
            this.zero2DAry(mAry, mAry.length, mAry[0].length);
            this.computeOpening(zfAry, mAry, sAry, tAry);
            out.println("\nPretty print result of closing follow by opening");
            this.prettyPrint(mAry, out);

            out.println("\nExiting complexOperations method");
        }

        public void copyArys(int[][] zfAry, int[][] mAry){
            for(int i = 0; i < this.rowSize; i++){
                for(int j = 0; j < this.colSize; j++){
                    zfAry[i][j] = mAry[i][j];
                }
            }
        }

        public void computeDilation(int[][] inAry, int[][] outAry, int[][] sAry){
            for(int i = this.rowFrameSize; i < this.rowSize; i++){
                for(int j = this.colFrameSize; j < this.colSize; j++){
                    if(inAry[i][j] > 0){
                        this.onePixelDilation(i, j, inAry, outAry, sAry);
                    }
                }
            }
        }

        public void onePixelDilation(int i, int j, int[][] inAry, int[][] outAry, int[][] sAry){
            int iOffset = i - this.rowOrigin;
            int jOffset = j - this.colOrigin;
            
            int rIndex = 0;
            
            while(rIndex < this.numStructRows){
                int cIndex = 0;
                while(cIndex < this.numStructCols){
                    
                    if(sAry[rIndex][cIndex] > 0){
                        outAry[iOffset + rIndex][jOffset + cIndex] = 1;
                    }

                    cIndex++;
                }
                rIndex++;
            }
        }

        public void computeErosion(int[][] inAry, int[][] outAry, int[][] sAry){
            for(int i = this.rowFrameSize; i < this.rowSize; i++){
                for(int j = this.colFrameSize; j < this.colSize; j++){
                    if(inAry[i][j] > 0){
                        this.onePixelErosion(i, j, inAry, outAry, sAry);
                    }
                }
            }
        }

        public void onePixelErosion(int i, int j, int[][] inAry, int[][] outAry, int[][] sAry){
            int iOffset = i - this.rowOrigin;
            int jOffset = j - this.colOrigin;
            boolean matchFlag = true;

            int rIndex = 0;

            while(matchFlag == true && rIndex < this.numStructRows){
                int cIndex = 0;
                while(matchFlag == true && cIndex < this.numStructCols){
                    
                    if(sAry[rIndex][cIndex] > 0 && inAry[iOffset + rIndex][jOffset + cIndex] <= 0){
                        matchFlag = false;
                    }
                    cIndex++;
                }
                rIndex++;
            }

            if(matchFlag == true){
                outAry[i][j] = 1;
            }
            else{
                outAry[i][j] = 0;
            }
        }

        public void computeOpening(int[][] zfAry, int[][] mAry, int[][] sAry, int[][] tAry){
            this.computeErosion(zfAry, tAry, sAry);
            this.computeDilation(tAry, mAry, sAry);
        }

        public void computeClosing(int[][] zfAry, int[][] mAry, int[][] sAry, int[][] tAry){
            this.computeDilation(zfAry, tAry, sAry);
            this.computeErosion(tAry, mAry, sAry);
        }
    }

    public static void main(String args[]) throws FileNotFoundException{
      
        //Opening all the files
        File inFile1 = new File(args[0]);
        File inFile2 = new File(args[1]);
        Scanner imgFile = new Scanner(inFile1);
        Scanner elmFile = new Scanner(inFile2);
		PrintWriter outFile1 = new PrintWriter(args[2]);
        PrintWriter outFile2 = new PrintWriter(args[3]);

        //Reading the header for the image and element file
	    int iRows = imgFile.nextInt(), iCols = imgFile.nextInt(), iMin = imgFile.nextInt(), iMax = imgFile.nextInt();

	    int sRows = elmFile.nextInt();
	    int sCols = elmFile.nextInt();
	    int sMin = elmFile.nextInt();
	    int sMax = elmFile.nextInt();

        //reading the origin of the element
	    int rOrigin = elmFile.nextInt();
	    int cOrigin = elmFile.nextInt();

        //initiating morph object and dynamically allocating all the  arrays
        Morphology morph = new Morphology(iRows, iCols, iMin, iMax, sRows, sCols, sMin, sMax, rOrigin, cOrigin); 

        //loading and printing the image
        morph.zero2DAry(morph.zeroFramedAry, morph.zeroFramedAry.length, morph.zeroFramedAry[0].length);
        morph.loadImg(imgFile);
        //morph.imgReformat(morph.zeroFramedAry);
        outFile1.println("Printing zeroFramedAry");
        morph.prettyPrint(morph.zeroFramedAry, outFile1);        

        //loading and printing the element
        morph.zero2DAry(morph.structAry, morph.structAry.length, morph.structAry[0].length);
        morph.loadStruct(elmFile);
        outFile1.println("\nPrinting structAry");
        morph.prettyPrint(morph.structAry, outFile1);

        //perorming morphology operations
        morph.basicOperations(morph.zeroFramedAry, morph.morphAry, morph.structAry, morph.tempAry, outFile1);
        morph.complexOperation(morph.zeroFramedAry, morph.morphAry, morph.structAry, morph.tempAry, outFile2);

        //closing all files
        imgFile.close();
        elmFile.close();
        outFile1.close();
        outFile2.close();
    }
}