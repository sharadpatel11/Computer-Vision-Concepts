#include <iostream>
#include <fstream>
#include<cmath>
using namespace std;

class thresholdSelection{
    private:
        int numRows, numCols, minVal, maxVal;
        int x1, y1, x2, y2;
        int* hisAry;
        int deepestThrVal;
        int BiGaussThrVal;
        int* GaussAry;

    public:
        thresholdSelection(int nr, int nc, int miv, int mav, int x1, int y1, int x2, int y2);
        int loadHist(ifstream& in);
        void dispHist(ofstream& out);
        void setZero();
        int deepestConcavity(ofstream& debug);
        int biGauss(int maxHeight, int min, int max, ofstream& debug);
        double computeMean(int maxHeight, int lIndex, int rIndex, ofstream& debug);
        double computeVar(int lIndex, int rIndex, double mean, ofstream& debug);
        double modifiedGauss(int index, double mean, double var, int maxHeight);
        double fitGauss(int maxHeight, int lIndex, int rIndex, ofstream& debug);

};

int main(int argc, char** argv){

    ifstream inFile1, inFile2;
    ofstream outFile1, deBugFile;
    
    inFile1.open(argv[1]);
    inFile2.open(argv[2]);
    outFile1.open(argv[3]);
    deBugFile.open(argv[4]);

    //Reads the header of inFile1
    int header[4];
    for(int i = 0; i < 4; i++) {
        inFile1 >> header[i];
    }
    int numRows = header[0], numCols = header[1], minVal = header[2], maxVal = header[3];

    //writes the header to outFile1
    outFile1 << numRows << " " << numCols << " " << minVal << " " << maxVal << endl;

    //reads two peak points from inFile2
    int points[4];
    for(int i = 0; i < 4; i++) {
        inFile2 >> points[i];
    }
    int x1 = points[0], y1 = points[1], x2 = points[2], y2 = points[3];

    //instanciates thresholdSelection object and dynamically allocates arrays of the class to 0 with length maxVal + 1
    thresholdSelection thrSelection = thresholdSelection(numRows, numCols, minVal, maxVal, x1, y1, x2, y2);

    //finds the maxHeight of the histogram
    int maxHeight = thrSelection.loadHist(inFile1);

    //displays the histogram on outFile1
    thrSelection.dispHist(outFile1);

    //finds the thrval by using deepest concativity and writes it on the outFile1
    int deepestThrVal = thrSelection.deepestConcavity(deBugFile);
    outFile1 << "The two peak points: (" << x1 << "," << y1 << ") and (" << x2 << "," << y2 << ")" << endl;
    outFile1 << "The deepest concavity auto-selected threshold value is " << deepestThrVal << endl;

    //finds the thrval by using biGaussian and writes it on the outFile1;
    int biGaussThrVal = thrSelection.biGauss(maxHeight, minVal, maxVal, deBugFile);
    outFile1 << "The BiGaussian auto-selected threshold value is " << biGaussThrVal << endl;

    //closing all files
    inFile1.close();
    inFile2.close();
    outFile1.close();
    deBugFile.close();

    return 0;
}

thresholdSelection::thresholdSelection(int nr, int nc, int miv, int mav, int x, int y, int xx, int yy){

        this->numRows = nr;
        this->numCols = nc;
        this->minVal = miv;
        this->maxVal = mav;
        this->x1 = x;
        this->x2 = xx;
        this->y1 = y;
        this->y2 = yy;

        this->hisAry = new int[this->maxVal + 1];
        this->GaussAry = new int[this->maxVal + 1];
        for(int i = 0; i <= this->maxVal; i++){
                this->hisAry[i] = 0;
                this->GaussAry[i] = 0;
        }
}

int thresholdSelection::loadHist(ifstream& in){
    
    int maxHeight = 0;
    int index, hisVal;

    while(in >> index >> hisVal && index <= this->maxVal){
        this->hisAry[index] = hisVal;
        if(maxHeight < hisVal){
            maxHeight = hisVal;
        }
    }

    return maxHeight;
}

void thresholdSelection::dispHist(ofstream& out){

    for(int i = this->minVal; i <= this->maxVal; i++){
        if(i < 10)
            out << i << "  ";
        else    
            out << i << " ";

        if(this->hisAry[i] < 100)
            out << " (" << this->hisAry[i] << "):";
        else
            out << "(" << this->hisAry[i] << "):"; 

        for(int j = 0; j < this->hisAry[i]; j++){
            out << "+";
        }
        out << endl;
    }
}

void thresholdSelection::setZero(){

    for(int i = 0; i < this->maxVal; i++){
        this->GaussAry[i] = 0;
    }
}

int thresholdSelection::deepestConcavity(ofstream& debug){
    debug << "Entering deepestConcavity method\n";

    double m = ((double)this->y2 - (double)this->y1) / ((double)this->x2 - (double)this->x1);
    double b = (double)this->y1 - (m * (double)this->x1);
    int maxGap = 0;
    int first = this->x1;
    int second = this->x2;
    int x = first, y, gap;
    int thr = first;

    while(x <= second){
        y = (int)(m*x + b);
        gap = (abs)(this->hisAry[x] - y);
        if(gap > maxGap){
            maxGap = gap;
            thr = x;
        }
        x++;
    }

    debug << "leaving deepestConcavity method, maxGap is: " << maxGap << " and thr is:" << thr << endl; 
    this->deepestThrVal = thr;

    return thr;
}

int thresholdSelection::biGauss(int maxHeight, int min, int max, ofstream& debug){
    debug << "Entering biGaussian method\n";

    double sum1, sum2, total, minSumDiff = 999999.0;
    int offset = (int) (max - min) / 10;
    int dividePt = offset; 
    int bestThr = dividePt;

    while(dividePt < (max - offset)){

        setZero();
        sum1 = fitGauss(maxHeight, 0, dividePt, debug);
        sum2 = fitGauss(maxHeight, dividePt, max, debug);
        total = sum1 + sum2;

        if(total < minSumDiff){
            minSumDiff = total;
            bestThr = dividePt;
        }

        debug << "dividePt: " << dividePt << ", sum1: " << sum1 << ", sum2: " << sum2 << ", total: " << total
            << ", minSumDiff: " << minSumDiff <<  " and bestThr: " << bestThr << endl;

        dividePt++;
    }

    debug << "leaving biGaussian method, minSumDiff: " << minSumDiff << " and bestThr: " << bestThr << endl;
    this->BiGaussThrVal = bestThr;
    return bestThr;
}

double thresholdSelection::computeMean(int maxHeight, int lIndex, int rIndex, ofstream& debug){
    debug << "Entering computeMean method\n";

    maxHeight = 0;
    int sum = 0, numPixels = 0;
    int index = lIndex;

    while(index < rIndex){

        sum += this->hisAry[index] * index;
        numPixels += this->hisAry[index];

        if(this->hisAry[index] > maxHeight){
            maxHeight = this->hisAry[index];
        }
        index++;
    }

    double result = (double) sum / (double) numPixels;

    debug << "Leaving computeMean method, maxHeight: " << maxHeight << " and result: " << result << endl;
    return result;
}

double thresholdSelection::computeVar(int lIndex, int rIndex, double mean, ofstream& debug){
    debug << "Entering computeVar method\n";

    double sum = 0.0;
    int numPixels = 0;
    int index = lIndex;

    while(index < rIndex){

        sum += (double) this->hisAry[index] * (((double) index - mean) * ((double) index - mean));
        numPixels += this->hisAry[index];

        index++;
    }

    double result = sum / (double) numPixels;

    debug << "Leaving computeVar method, result: " << result << endl;
    return result;
}

double thresholdSelection::modifiedGauss(int index, double mean, double var, int maxHeight){

    return (double) (maxHeight * exp(-((((double) index - mean) * ((double) index - mean)) / (2 * var))));
}

double thresholdSelection::fitGauss(int maxHeight, int lIndex, int rIndex, ofstream& debug){
    debug << "Entering fitGauss method\n";

    double mean, var, sum = 0.0, Gval, maxGval;

    mean = computeMean(maxHeight, lIndex, rIndex, debug);
    var = computeVar(lIndex, rIndex, mean, debug);
    int index = lIndex;

    while(index <= rIndex){

        Gval = modifiedGauss(index, mean, var, maxHeight);
        sum += (abs)(Gval - (double)this->hisAry[index]);
        this->GaussAry[index] = (int) Gval;
        index++;
    }

    debug << "leaving fitGauss method, sum is: " << sum << endl;
    return sum;
}