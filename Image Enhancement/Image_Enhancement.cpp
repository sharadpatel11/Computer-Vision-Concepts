#include <iostream>
#include <fstream>
#include <string>
#include <algorithm>
using namespace std;

class Enhancement{
    private:
        int numRows, numCols, minVal, maxVal;
        int maskRows, maskCols, maskMin, maskMax;
        int thrVal;
        int** mirrorFramedAry;
        int** avgAry;
        int** medianAry;
        int** GaussAry;
        int** thrAry;
        int* neighborAry = new int[25];
        int* maskAry = new int[25];
        int maskWeight  = 0;

    public:
        Enhancement(int nr, int nc, int miv, int mav, int maskR, int maskC, int maskMin, int maskMax, int thrV);
        void binaryThreshold(string a);
        void loadImage(ifstream& iFile);
        void loadMaskAry(ifstream& mFile);
        void loadNeighborAry(int i, int j);
        void mirrorFraming();
        void computeAvg();
        void computeMedian();
        void sort();
        void computeGauss();
        int convolution();
        void imageReformat(string a, ofstream& debug);
        void prettyPrint(ofstream& out);

};

int main(int argc, char** argv){

    //reads the threshold value from console input
    string t = argv[3];
    int thrV = stoi(t);

    //opens all the neccesary files
    ifstream inFile, maskFile;
    ofstream outFile1, avgOutFile, medianOutFile, gaussOutFile, deBugFile, imgOutFile;
    
    inFile.open(argv[1]);
    maskFile.open(argv[2]);
    avgOutFile.open(argv[4]);
    medianOutFile.open(argv[5]);
    gaussOutFile.open(argv[6]);
    deBugFile.open(argv[7]);
    imgOutFile.open(argv[8]);

    //reads the header from the image and the mask
    int iheader[4];
    for(int i = 0; i < 4; i++) {
        inFile >> iheader[i];
    }
    int numRows = iheader[0], numCols = iheader[1], minVal = iheader[2], maxVal = iheader[3];

    int mheader[4];
    for(int i = 0; i < 4; i++) {
        maskFile >> mheader[i];
    }
    int maskRows = mheader[0], maskCols = mheader[1], maskMin = mheader[2], maskMax = mheader[3];

    //Instanciates Enhancement Object
    Enhancement E = Enhancement(numRows, numCols, minVal, maxVal, maskRows, maskCols, maskMin, maskMax, thrV);

    //loads the mask to maskAry
    E.loadMaskAry(maskFile);

    //loads the image to mirrorFramedAry
    E.loadImage(inFile);

    //mirrorFrame the image
    E.mirrorFraming();

    //prints reformatted mirrorFramedAry
    E.imageReformat("mirrorFrameAry", imgOutFile);

    //Using Average
    E.computeAvg();
    E.imageReformat("avgAry", deBugFile);
    E.binaryThreshold("avgAry");
    E.prettyPrint(avgOutFile);

    //Using Median
    E.computeMedian();
    E.imageReformat("medianAry", deBugFile);
    E.binaryThreshold("medianAry");
    E.prettyPrint(medianOutFile);

    //Using Gauss
    E.computeGauss();
    E.imageReformat("GaussAry", deBugFile);
    E.binaryThreshold("GaussAry");
    E.prettyPrint(gaussOutFile);

    //closes all Files
    inFile.close();
    maskFile.close();
    avgOutFile.close();
    medianOutFile.close();
    gaussOutFile.close();
    deBugFile.close();

    return 0;
}

Enhancement::Enhancement(int nr, int nc, int miv, int mav, int maskR, int maskC, int maskMin, int maskMax, int thrV){
    this->numRows = nr;
    this->numCols = nc;
    this->minVal = miv;
    this->maxVal = mav;
    this->maskRows = maskR;
    this->maskCols = maskC;
    this->maskMin = maskMin;
    this->maskMax = maskMax;
    this->thrVal = thrV;

    this->mirrorFramedAry = new int*[this->numRows + 4];
    this->avgAry = new int*[this->numRows + 4];
    this->medianAry = new int*[this->numRows + 4];
    this->GaussAry = new int*[this->numRows + 4];
    this->thrAry = new int*[this->numRows + 4];    

    for(int i = 0; i < this->numRows + 4; i++){
        this->mirrorFramedAry[i] = new int[this->numCols + 4];
        this->avgAry[i] = new int[this->numCols + 4];
        this->medianAry[i] = new int[this->numCols + 4];
        this->GaussAry[i] = new int[this->numCols + 4];
        this->thrAry[i] = new int[this->numCols + 4];
    }

    for(int i = 0; i < this->numRows + 4; i++){
        for(int j = 0; j < this->numCols + 4; j++){
            this->mirrorFramedAry[i][j] = 0;
            this->avgAry[i][j] = 0;
            this->medianAry[i][j] = 0;
            this->GaussAry[i][j] = 0;
            this->thrAry[i][j] = 0;
        }
    }

    for(int i = 0; i < 25; i++){
        this->neighborAry[i] = 0;
        this->maskAry[i] = 0;
    }
}

void Enhancement::loadImage(ifstream& iFile){
    
    int pixelVal = 0;

    for(int i = 2; i < this->numRows + 2; i++){
        for(int j = 2; j < this->numCols + 2; j++){
            iFile >> pixelVal;
            this->mirrorFramedAry[i][j] = pixelVal;
        }
    }
}

void Enhancement::loadMaskAry(ifstream& mFile){
    
    int pixelVal = 0;

    for(int i = 0; i < 25; i++){
        mFile >> pixelVal;
        this->maskAry[i] = pixelVal;
        this->maskWeight += pixelVal;
    }
}

void Enhancement::loadNeighborAry(int i, int j){

    int rStart = i - 2;
    int index = 0;

    while(rStart <= i + 2){
        int cStart = j - 2;
        while(cStart <= j + 2){
            this->neighborAry[index++] = this->mirrorFramedAry[rStart][cStart];
            cStart++;
        }
        rStart++;
    }
}

void Enhancement::mirrorFraming(){
    for(int i = 0; i < 2; i++){
        for(int j = 0; j < this->numCols + 4; j++){
            if(j < 3){
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[2][2];
            }
            else if(j >= this->numCols + 2){
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[2][this->numCols + 1];
            }
            else{
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[2][j];
            }
        }
    }

    for(int i = this->numRows + 2; i < this->numRows + 4; i++){
        for(int j = 0; j < this->numCols + 4; j++){
            if(j < 3){
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[this->numRows + 1][2];
            }
            else if(j >= this->numCols + 2){
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[this->numRows + 1][this->numCols + 1];
            }
            else{
                this->mirrorFramedAry[i][j] = this->mirrorFramedAry[this->numRows + 1][j];
            }
        }
    }

    for(int i = 2; i < this->numRows + 2; i++){
        for(int j = 0; j < 2; j++){
            this->mirrorFramedAry[i][j] = this->mirrorFramedAry[i][2];
        }
    }

    for(int i = 2; i < this->numRows + 2; i++){
        for(int j = this->numCols + 2; j < this->numCols + 4; j++){
            this->mirrorFramedAry[i][j] = this->mirrorFramedAry[i][this->numCols + 1];
        }
    }
}

void Enhancement::imageReformat(string a, ofstream& debug){
    debug << this->numRows << " " << this->numCols << " " << this->minVal << " " << this->maxVal << endl;

    if(a == "mirrorFrameAry"){
        string str = to_string(maxVal);
        int width = str.length();

        int r = 2;
        while(r < this->numRows + 2){
            int c = 2;
            while(c < this->numCols + 2){

                debug << this->mirrorFramedAry[r][c] << " ";
                str = to_string(this->mirrorFramedAry[r][c]);
                int ww = str.length();
                while(ww < width){
                    debug << " ";
                    ww++;
                }
                c++;
            }
            debug << endl;
            r++;
        }
    }
    else if(a == "avgAry"){
        string str = to_string(maxVal);
        int width = str.length();

        int r = 2;
        while(r < this->numRows + 2){
            int c = 2;
            while(c < this->numCols + 2){

                debug << this->avgAry[r][c] << " ";
                str = to_string(this->avgAry[r][c]);
                int ww = str.length();
                while(ww < width){
                    debug << " ";
                    ww++;
                }
                c++;
            }
            debug << endl;
            r++;
        }
    }
    else if(a == "medianAry"){
        string str = to_string(maxVal);
        int width = str.length();

        int r = 2;
        while(r < this->numRows + 2){
            int c = 2;
            while(c < this->numCols + 2){

                debug << this->medianAry[r][c] << " ";
                str = to_string(this->medianAry[r][c]);
                int ww = str.length();
                while(ww < width){
                    debug << " ";
                    ww++;
                }
                c++;
            }
            debug << endl;
            r++;
        }
    }
    else if(a == "GaussAry"){
        string str = to_string(maxVal);
        int width = str.length();

        int r = 2;
        while(r < this->numRows + 2){
            int c = 2;
            while(c < this->numCols + 2){

                debug << this->GaussAry[r][c] << " ";
                str = to_string(this->GaussAry[r][c]);
                int ww = str.length();
                while(ww < width){
                    debug << " ";
                    ww++;
                }
                c++;
            }
            debug << endl;
            r++;
        }
    }

    debug << endl;
}

void Enhancement::computeAvg(){
    this->minVal = 9999;
    this->maxVal = 0;

    int i = 2;

    while(i < this->numRows + 2){
        int j = 2;
        while(j < this->numCols + 2){
            loadNeighborAry(i, j);
            int sum = 0;
            for(int k = 0; k < 25; k++){
                sum += this->neighborAry[k];
            }
            this->avgAry[i][j] = sum/25;
            if(this->avgAry[i][j] < this->minVal){
                this->minVal = this->avgAry[i][j];
            }

            if(this->avgAry[i][j] > this->maxVal){
                this->maxVal = this->avgAry[i][j];
            }
            j++;
        }
        i++;
    }   
}

void Enhancement::sort(){
    for(int i = 0; i < 25; i++) {
        for(int j = i+1; j < 25; j++)
        {
            if(this->neighborAry[j] < this->neighborAry[i]) {
                int temp = this->neighborAry[i];
                this->neighborAry[i] = this->neighborAry[j];
                this->neighborAry[j] = temp;
            }
        }
    }
}

void Enhancement::computeMedian(){
    this->minVal = 9999;
    this->maxVal = 0;

    int i = 2;

    while(i < this->numRows + 2){
        int j = 2;
        while(j < this->numCols + 2){
            loadNeighborAry(i, j);
            sort();
            this->medianAry[i][j] = this->neighborAry[12];
            if(this->medianAry[i][j] < this->minVal){
                this->minVal = this->medianAry[i][j];
            }

            if(this->medianAry[i][j] > this->maxVal){
                this->maxVal = this->medianAry[i][j];
            }
            j++;
        }
        i++;
    }
}

int Enhancement::convolution(){
    int result = 0;

    for(int i = 0; i < 25; i++){
        result += this->neighborAry[i] * this->maskAry[i];
    }

    return (result/this->maskWeight);
}

void Enhancement::computeGauss(){
    this->minVal = 9999;
    this->maxVal = 0;

    int i = 2;

    while(i < this->numRows + 2){
        int j = 2;
        while(j < this->numCols + 2){
            loadNeighborAry(i, j);
            this->GaussAry[i][j] = convolution();
            if(this->GaussAry[i][j] < this->minVal){
                this->minVal = this->GaussAry[i][j];
            }

            if(this->avgAry[i][j] > this->maxVal){
                this->maxVal = this->GaussAry[i][j];
            }
            j++;
        }
        i++;
    }
}

void Enhancement::binaryThreshold(string a){
    
    if(a == "avgAry"){
        for(int i = 2; i < this->numRows + 2; i++){
            for(int j = 2; j < this->numCols + 2; j++){
                if(this->avgAry[i][j] < this->thrVal){
                    this->thrAry[i][j] = 0;
                }
                else{
                    this->thrAry[i][j] = 1;
                }
            }
        }
    }
    else if(a == "medianAry"){
        for(int i = 2; i < this->numRows + 2; i++){
            for(int j = 2; j < this->numCols + 2; j++){
                if(this->medianAry[i][j] < this->thrVal){
                    this->thrAry[i][j] = 0;
                }
                else{
                    this->thrAry[i][j] = 1;
                }
            }
        }
    }
    else if( a == "GaussAry"){
        for(int i = 2; i < this->numRows + 2; i++){
            for(int j = 2; j < this->numCols + 2; j++){
                if(this->GaussAry[i][j] < this->thrVal){
                    this->thrAry[i][j] = 0;
                }
                else{
                    this->thrAry[i][j] = 1;
                }
            }
        }
    }
}

void Enhancement::prettyPrint(ofstream& out){

    for(int i = 2; i < this->numRows + 2; i++){
        for(int j = 2; j < this->numCols + 2; j++){
            if(this->thrAry[i][j] > 0){
                out << this->thrAry[i][j] << " ";
            }
            else{
                out << "  ";
            }
        }
        out << endl;
    }
}