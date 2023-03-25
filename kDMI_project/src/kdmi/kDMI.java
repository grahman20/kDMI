/**
 * kDMI employs two levels of horizontal partitioning (based on a decision tree and k-NN algorithm) of a data set, 
 * in order to find the records that are very similar to the one with missing value/s. 
 * Additionally, it uses a novel approach to automatically find the value of k for each record.
 * 
 * <h2>Reference</h2>
 * 
 * Rahman, M. G. and Islam, M. Z. (2013): A Novel Framework Using Two Layers of Missing Value Imputation, In Proc. of the 11th Australasian Data Mining Conference (AusDM 13), Canberra, Australia, 13-15 November 2013
 *  
 * @author Gea Rahman <https://csusap.csu.edu.au/~grahman/>
 */

package kdmi;
import SysFor.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;



/**
 *
 * @author Geaur Rahman
 * 19/05/2011
 */
public class kDMI {

   /** the decision tree builder */
    private DecisionTreeBuilder treeBuilder;
   
    private String gfileDataFileIn;//contains users data file name
    private String gattrInfo; //the attribute information file, used to generate name files
    private String [] attrNames; //contain attributes name
    private String [] attrType; // "n"->numerical, "c"->categorical
    private int [] attrNType; // 1->numerical, 0->categorical, 2->class (categorical)
    private String []treeFile;//contains tree file names
    private int noOfTree;//contains no. of tree created for the dataset
    int []leafLength;//contains file name for each leaf
    private int [] missingAttrs;//1->missing, 0->Available
    private int [] missingAttrsTree;//contains tree index of an attr which has a tree
    private int [] TreeAttrs;//contains atttribute index which has a tree
    private int noOfAttrs; // total no. of attributes of the data file
    private int noOfRecords; // total no. of attributes of the data file
    private int noOfMissingAttrs; // total no. of missing attributes of the data file
    private String [] fStr; //contains format string of each numerical attr
    private String []tempFileList;
    private int tempTotalFile;
    private int [][][]LeafRecords;  //Records belong to each leaf
    private int [][]SizeOfEachLeaf;  //No. of records belong to each leaf
    private int [][]RL;  //contains a leaf id where a record belongs to.
    private int [][]FlgImpStatus;  //0->no imp required, 1->imp require but not not done, 2->imp done!
    private String [][]logicRule;//contains logic rules for each leaf of a tree
    private String [][]MajorityVal;//contains masority value of an attribute based on a leaf, tree
    private String [][]dataset;//data set
    private int []MR;  //Missing records, 0->no missing, 1->Missing
    private int [][]MV;  //Missing values, 0->no missing, 1->Missing
    private int [][]RecClassify;  //-2->no DT, -1->not classify missing,0>= ->classified and leaf id
    private String [][]datasetNormalized;//
     /*
     * this method will impute a data set with DMI and best K
     * the missing values using DMI approach
     * @param attrFile atrtibute file
     * @param dataFile data file having missing values
     * @param outputFile file with imputed data
     */

    public void runkDMI(String attrFile, String dataFile,String outputFile)
    {
        initialize(attrFile,dataFile);

        FileManager fileManager=new FileManager();
        String dataDc = fileManager.changedFileName(dataFile, "_DC");
        tempFileList[tempTotalFile]=dataDc;tempTotalFile++;
        String dataDi = fileManager.changedFileName(dataFile, "_DI");
        tempFileList[tempTotalFile]=dataDi;tempTotalFile++;
        fileManager.divideDataset(new File(dataFile),dataDc, dataDi);
        gfileDataFileIn=dataDc;//assign data file name

        CreateDTs();  //buld dt
        recordClassify(); //classify records

        recordImpute(); //impute records
        arrayToFile(dataset, noOfRecords, noOfAttrs, outputFile); //write to output file
        
        //remove tmp and tree files
        fileManager.removeListOfFiles(tempFileList, tempTotalFile);
        fileManager.removeListOfFiles(treeFile, noOfTree);

    }

/*
 * this method is used to write an array into a file
 *
 */

private void arrayToFile(String [][]data, int totRec, int totAttr,String outF)
{
        FileManager fileManager=new FileManager();
        File outFile=new File(outF);

        for(int i=0;i<totRec;i++)
        {
           String rec="";

           for(int j=0;j<totAttr;j++)
           {
                if(attrNType[j]==1 && isMissing(data[i][j])==0)
                {
                    DecimalFormat df = new DecimalFormat(fStr[j]);
                    rec=rec+df.format(Double.parseDouble(data[i][j]))+", ";
                }
                else{
                    rec=rec+data[i][j]+", ";
                }
           }
           if(i<totRec-1)
               rec=rec+"\n";
           if(i==0)
               fileManager.writeToFile(outFile, rec);
           else
               fileManager.appendToFile(outFile, rec);
        }
}
    

/*
 * this method initializes the MVI framework
 */
public void initialize(String attrFile, String dataFile)
    {
        gattrInfo=attrFile;//assign attribute file name
        gfileDataFileIn=dataFile;//assign data file name
        
        FileManager fileManager=new FileManager();
        String [][]tmpAty=fileManager.readFileAs2DArray(new File(attrFile));
        dataset=fileManager.readFileAs2DArray(new File(dataFile));
        noOfRecords=dataset.length;
        noOfAttrs=dataset[0].length;
        attrNType=new int[noOfAttrs];
        attrType=new String[noOfAttrs];
        attrNames=new String[noOfAttrs];
        missingAttrs=new int[noOfAttrs];
        RecClassify=new int[noOfRecords][noOfAttrs];
        MR=new int[noOfRecords];
        MV=new int[noOfRecords][noOfAttrs];

        int totmis=0;
        for(int j=0;j<noOfAttrs;j++)
        {
            missingAttrs[j] = 0;
        }
        for(int i=0;i<noOfRecords;i++)
        {
            MR[i]=0;
            for(int j=0;j<noOfAttrs;j++)
            {
                if(isMissing(dataset[i][j])==1)
                {
                    MV[i][j]=1;MR[i]=1;totmis++;missingAttrs[j]=1;
                }
                else
                {
                    MV[i][j]=0;
                 }
            }
        }
        noOfMissingAttrs=0;
        for(int j=0;j<noOfAttrs;j++)
        {
           if( missingAttrs[j] ==1 )noOfMissingAttrs++;
        }

       
        for(int i=0; i<noOfAttrs;i++)
         {
            attrNames[i]=tmpAty[1][i];
            if(tmpAty[0][i].equals("1"))
             {
                 attrNType[i]=1;
                 attrType[i]="n";
             }
            else
             {
                 
                 attrType[i]="c";
                 attrNType[i]=0;
             }
         }
        int []mDecP=fileManager.attrMaxDecimalPlaces(attrNType,dataset);
        fStr=new String[noOfAttrs];
        for (int c=0;c<noOfAttrs;c++)
        {
            fStr[c]="";
            if (attrNType[c]==1)
            {
                String fs="";
                for(int i=0;i<mDecP[c];i++)
                {
                    fs=fs+"0";
                }
                if (mDecP[c]>0)
                {
                    fStr[c] = "####0." + fs;}
                else
                {
                    fStr[c] = "####0";}
            }
        }

      tempFileList=new String[50];
      tempTotalFile=0;
    }

/*
 * The method builds DT for each attrinbute having missing values.
 */
private void CreateDTs()
{
    FileManager fileManager = new FileManager();
    noOfTree=0;
    treeFile=new String[noOfMissingAttrs];
    missingAttrsTree=new int[noOfAttrs];
    leafLength=new int[noOfMissingAttrs];
    TreeAttrs=new int[noOfAttrs];
    String tmpDataF= fileManager.changedFileName(gfileDataFileIn, "_tmp");
    tempFileList[tempTotalFile]=tmpDataF;tempTotalFile++;
    String nameFile_DT= fileManager.changedFileName(gfileDataFileIn, "_tmpName");
    tempFileList[tempTotalFile]=nameFile_DT;tempTotalFile++;
    String tmpAttrF= fileManager.changedFileName(gattrInfo, "_tmp");
    tempFileList[tempTotalFile]=tmpAttrF;tempTotalFile++;
    
    for(int i=0;i<noOfAttrs;i++)
        {
            if(missingAttrs[i]==1)//1=missing, 0=no missing
            {
                if(attrType[i].equals("n"))
                {
                 //generalize
                    generalise(gfileDataFileIn, tmpDataF, i,noOfAttrs);
                }
                else
                {
                    fileManager.copyFile(gfileDataFileIn, tmpDataF);
                }
                setClassAttribute(gattrInfo, tmpAttrF, i,noOfAttrs);
                String gtest=fileManager.extractNameFileFromDataFile(new File(tmpAttrF),
            new File(tmpDataF),new File(nameFile_DT));
               //creating decision tree
               String nameFile_out= fileManager.changedFileName(gfileDataFileIn, "_"+attrNames[i]+"_DT");
               treeBuilder = new DecisionTreeBuilder(nameFile_DT,
                       tmpDataF, nameFile_out, DecisionTreeBuilder.SEE5);

               treeBuilder.createDecisionTree();
               treeFile[noOfTree]= nameFile_out;
               leafLength[noOfTree]=noOfRules(nameFile_out);
               missingAttrsTree[i]=noOfTree;
               TreeAttrs[noOfTree]=i;
               noOfTree++;
            }
         else{
                missingAttrsTree[i]=-2;
              }
        }

}

/*
 * finds best subset of record having missing values within a leaf
 */
private int[] findBestkNN(int rec, int []leafR)
{
    int N=leafR.length;
    String [][]tmpData=new String [N][noOfAttrs];
    int [][]MV1=new int [N][noOfAttrs];
    int []MR1=new int [N];
    int totMiss=0;
    
    int kk=N;
    int []bestRec=new int[N];
    double rmse=Double.POSITIVE_INFINITY;
    kNN_ary knn=new kNN_ary();
    mviNewEMI nemi=new mviNewEMI();
    //create arrays for the new EMI
    int cRow=-1;
    for(int i=0;i<N;i++)
    {
        int tmprec=leafR[i];
        for(int j=0;j<noOfAttrs;j++)
        {
            tmpData[i][j]=dataset[tmprec][j];
        }
        if(rec==tmprec)cRow=i;
    }

    int mPos=-1;

    for(int j=0;j<noOfAttrs;j++)
    {
        if(attrNType[j]==1 && MV[rec][j]==0)
        {
            mPos=j;

            break;
        }
    }
    double oval;
    if(mPos>=0)
    {
        oval=Double.parseDouble(dataset[rec][mPos]);
        int inv=1,iv=2;
        if(N>5)
        {
            inv=(int)Math.sqrt((double)N);
            iv=inv;
        }

        for(int k=iv;k<N;k+=inv)
        {
            int []krec=new int[k];
            krec=knn.runkNN(attrNType, dataset[rec], tmpData, k, cRow);
            String [][]kData=new String[k+1][noOfAttrs];
            int [][]MVk=new int[k+1][noOfAttrs];
            int []MRk=new int[k+1];
            int tmiss=0;
            for(int i=0;i<k;i++)
            {
                int tmprec=leafR[krec[i]];
                for(int j=0;j<noOfAttrs;j++)
                {
                    kData[i][j]=dataset[tmprec][j];
                    MVk[i][j]=MV[tmprec][j];
                    if(MVk[i][j]==1)
                    {
                       totMiss++;
                    }
                }
                MRk[i]=MR[tmprec];
            }
            for(int j=0;j<noOfAttrs;j++)
            {
                kData[k][j]=dataset[rec][j];
                MVk[k][j]=MV[rec][j];
                if(MVk[k][j]==1)
                    {
                       totMiss++;
                    }
            }
            kData[k][mPos]="?"; totMiss++;MVk[k][mPos]=1;
            MRk[k]=MR[rec];

            //call new EMI to impute
            nemi.runNewEMI(kData, MVk,MRk, attrNType,totMiss, 0,0);
            double ival=0.0;
            if(isMissing(kData[k][mPos])==0)
            {
                ival=Double.parseDouble(kData[k][mPos]);
                double nrmse=Math.pow((ival-oval),2.0);
                nrmse=Math.sqrt(nrmse);

                if(nrmse<rmse)
                {
                    rmse=nrmse;
                    for(int i=0;i<k;i++)
                    {
                        bestRec[i]=leafR[krec[i]];
                    }
                    kk=k;
                }
            }

        }
    }
    else
    {
       for(int i=0;i<N;i++)
         {
            bestRec[i]=leafR[i];
        }
        kk=N;
    }
   int []bestSubset=new int[kk];
   for(int i=0;i<kk;i++)
         {
            bestSubset[i]=bestRec[i];
        }
//   System.out.println("N="+N+", best k="+kk);
   return  bestSubset;
}



//the method finds the records belong to each leaf
private void recordClassify()
{
    FileManager fileManager = new FileManager();
    if(noOfTree>0)
    {
        int maxLeaf=0;
        //find max no. of leaves
        for(int t=0;t<noOfTree;t++)
        {
            if(leafLength[t]>maxLeaf)maxLeaf=leafLength[t];
        }
        LeafRecords=new int[noOfTree][maxLeaf][noOfRecords];//Records belong to each leaf
        SizeOfEachLeaf=new int[noOfTree][maxLeaf];  //No. of records belong to each leaf
        FlgImpStatus=new int[noOfTree][maxLeaf];
        RL=new int[noOfTree][noOfRecords];
        logicRule=new String[noOfTree][maxLeaf];
        MajorityVal=new String[noOfTree][maxLeaf];
        for(int t=0;t<noOfTree;t++)
        {
           if(leafLength[t]>0)
           {
           int atype[]=new int[noOfAttrs];
           for (int i=0;i<noOfAttrs;i++)
                   atype[i]=attrNType[i];
           atype[TreeAttrs[t]]=2;
           String []tmpRules = fileManager.readFileAsArray(new File(treeFile[t]));
           for(int l=0;l<leafLength[t];l++)
           {
                 logicRule[t][l]=tmpRules[l+1];
                 if (attrNType[TreeAttrs[t]]!=1)
                 {
                     MajorityVal[t][l]=findMajorityClassValues(atype,noOfAttrs,logicRule[t][l]);
                 }
                 SizeOfEachLeaf[t][l] = 0;
                 FlgImpStatus[t][l]=0;
            }
          }
        }

        for(int j=0;j<noOfAttrs;j++)
        {
             for(int i=0;i<noOfRecords;i++)
            {
                if(missingAttrsTree[j]==-2)
                {
                    RecClassify[i][j]=missingAttrsTree[j];
                }
                else
                {
                    int t=missingAttrsTree[j];
                    int atype[]=new int[noOfAttrs];
                    for (int c=0;c<noOfAttrs;c++)
                           atype[c]=attrNType[c];
                    atype[TreeAttrs[t]]=2;
                    int leafId=FindLeafId(dataset[i],atype,noOfAttrs,logicRule[t],leafLength[t]);
                    RL[t][i]=leafId;
                    if(leafId>=0)
                    {
                        RecClassify[i][j]=leafId;
                        LeafRecords[t][leafId][SizeOfEachLeaf[t][leafId]]=i;
                        if(MR[i]==1)
                        {
                            if (attrNType[TreeAttrs[t]]==1)
                            {
                                FlgImpStatus[t][leafId] = 1;
                            }
                        }

                        SizeOfEachLeaf[t][leafId]++;
                    }
                    else
                    {
                        RecClassify[i][j]=-1;
                    }
                }
            }
             
        }
    }
}

//Impute numerical missing values belonging to the leaves one by one using EMI
private void recordImpute()
{
    mviNewEMI nemi=new mviNewEMI();
    for(int g=0;g<noOfRecords;g++)
    {
        if(MR[g]==1)
        {
//            System.out.println ("Imputing record= "+g);
            for(int z=0;z<noOfAttrs;z++)
            {
                if(MV[g][z]==1)
                {
                    int flg=-1, flag=-1;
                    int ct=0, cmt=0;
                    if(RecClassify[g][z]<0)
                    {
                        flag=0;
                        for(int s=0;s<noOfAttrs;s++)
                        {
                            if(RecClassify[g][s]>=0)
                                {
                                    ct++;
                                    if(MV[g][s]==1)
                                    {
                                        cmt++;
                                    }
                                }
                        }
                        int ss=z+1;
                        if(ct>0||cmt>0)
                        {
                            for(int s=0;s<noOfAttrs;s++)
                            {
                                if(ss==noOfAttrs) ss=0;
                                if(ss!=z)
                                {
                                    if(RecClassify[g][ss]>=0)
                                    {
                                        if(cmt>0)
                                        {
                                            if(MV[g][ss]==1)
                                            {
                                                flg = ss;
                                                break;
                                            }
                                        }
                                        else
                                        {
                                            flg = ss;
                                            break;
                                        }

                                    }
                                }
                                ss++;
                            }
                        }
                        else
                        {
                            flag=1;
                        }
                    }
                   int tl=noOfRecords;
                   int t=-1, l=-1;
                   if(flag==-1)
                   {
                      t=missingAttrsTree[z];
                      l=RecClassify[g][z];
                      tl=SizeOfEachLeaf[t][l];

                   }
                   else if(flag ==0)
                   {
                      t=missingAttrsTree[flg];
                      l=RecClassify[g][flg];
                      tl=SizeOfEachLeaf[t][l];
                   }
                   int []leafR=new int[tl];
                   if(flag<=0)
                   {
                      for(int m=0;m<tl;m++)
                      {
                          leafR[m]=LeafRecords[t][l][m];
                      }
                   }
                   else 
                   {
                      for(int m=0;m<tl;m++)
                      {
                          leafR[m]=m;
                      }
                   }

                   //best k
                  int []bestNN= findBestkNN(g,leafR);
                  int kk=bestNN.length;
                  if(kk>0)
                  {
                   String [][]tmpData=new String [kk+1][noOfAttrs];
                   int [][]MV1=new int [kk+1][noOfAttrs];
                   int []MR1=new int [kk+1];
                   int totMiss=0;
                   for(int r=0;r<kk;r++)
                   {
                       for(int j=0;j<noOfAttrs;j++)
                        {
                            tmpData[r][j]=dataset[bestNN[r]][j];
                            MV1[r][j]=MV[bestNN[r]][j];
                            if(MV1[r][j]==1)
                            {
                               totMiss++;
                            }
                        }
                        MR1[r]=MR[bestNN[r]];
                   }
                   for(int j=0;j<noOfAttrs;j++)
                    {
                        tmpData[kk][j]=dataset[g][j];
                        MV1[kk][j]=MV[g][j];
                        if(MV1[kk][j]==1)
                        {
                           totMiss++;
                        }
                    }
                    MR1[kk]=MR[g];

                    if(flag==-1)
                    {
                        if(attrNType[z]==1)
                        {
                            nemi.runNewEMI(tmpData, MV1,MR1, attrNType,totMiss, 0,0);
                            if(isMissing(tmpData[kk][z])==0)
                            {
                            dataset[g][z]= tmpData[kk][z];MV[g][z]=0;
                            }
                        }
                        else{
                            dataset[g][z]= MajorityVal[t][l];MV[g][z]=0;
                            }

                   }
                   else if(flag == 0)
                    {
                        if(attrNType[z]==1 || attrNType[flg]==1)
                        {
                            nemi.runNewEMI(tmpData, MV1,MR1, attrNType,totMiss, 0,0);
                            if(attrNType[z]==1)
                            {
                                if(isMissing(tmpData[kk][z])==0)
                                {
                                dataset[g][z]= tmpData[kk][z];MV[g][z]=0;
                                }
                            }
                            else{
                                    dataset[g][z]=findModeValue(bestNN,z);MV[g][z]=0;
                            }

                            if (flg>z)
                            {
                                if(MV[g][flg]==1)
                                {
                                    if(attrNType[flg]==1)
                                    {
                                        if(isMissing(tmpData[kk][flg])==0)
                                        {
                                            dataset[g][flg]= tmpData[kk][flg];MV[g][flg]=0;
                                        }
                                    }
                                    else{
                                         if(RecClassify[g][flg]>=0)
                                         {
                                             dataset[g][flg]=MajorityVal[t][RecClassify[g][flg]];MV[g][flg]=0;
                                         }
                                         else{
                                             dataset[g][flg]=findModeValue(bestNN,flg);MV[g][flg]=0;
                                        }
                                    }
                                }
                                z=flg;
                            }


                        }
                        else{
                               dataset[g][z]=findModeValue(bestNN,z);
                               if(flg>z)
                               {
                               if(MV[g][flg]==1)
                                {
                                if(RecClassify[g][flg]>=0)
                                 {
                                     dataset[g][flg]=MajorityVal[t][RecClassify[g][flg]];MV[g][flg]=0;
                                 }
                                 else{
                                     dataset[g][flg]=findModeValue(bestNN,flg);MV[g][flg]=0;
                                    }
                                 }
                                z=flg;
                                }
                            }
                   }
                   else if(flag == 1)
                    {
                       int gl=-1;
                       for(int j=0;j<noOfAttrs;j++)
                        {
                           if(attrNType[j]==1 && MV[g][j]==1)
                           {
                                gl=1;break;
                           }
                        }
                       if(gl==1)
                           {
                               nemi.runNewEMI(tmpData, MV1,MR1, attrNType,totMiss, 0,0);
                           }
                       for(int j=0;j<noOfAttrs;j++)
                        {
                           if( MV[g][j]==1)
                           {
                               if(attrNType[j]==1)
                               {
                                    dataset[g][j]= tmpData[kk][j];
                               }
                               else{
                                    dataset[g][j]= findModeValue(bestNN,j);
                                }
                               MV[g][j]=0;
                           }
                        }
                       z=noOfAttrs;

                    }

                    }


                }
            }
//         for(int j=0;j<noOfAttrs;j++)
//            System.out.print (dataset[g][j]+", ");
//         System.out.print ("\n");
        }
    }

   
}
/*
 * The method finds the most frequent value of a catgorical
 * attribute within a data set (presented as an array)
 */
private String findModeValue(int[]kID, int attrPos)
{
    String PreVal="?";
    int tmp_rec=kID.length;
    String []tmpDomain=new String[tmp_rec];
    int []tmpCnt=new int[tmp_rec];
    int tmpDS=0;
    for (int i=0;i<tmp_rec;i++)
    {
        if(isMissing(dataset[kID[i]][attrPos])==0)
        {
            int flg=findDomain(tmpDomain,tmpDS,dataset[kID[i]][attrPos]);
            if(flg==-1)
            {
                tmpDomain[tmpDS]=dataset[kID[i]][attrPos];
                tmpCnt[tmpDS]++;
                tmpDS++;
            }
            else
             {
                tmpCnt[flg]++;
             }
        }
    }
    int max=-1, mIndex=-1;
    for (int i=0;i<tmpDS;i++)
    {
        if(tmpCnt[i]>max)
        {
           max =tmpCnt[i];
           mIndex=i;
        }

    }
    if (mIndex>-1) PreVal=tmpDomain[mIndex];
//    System.out.println(PreVal);
    return PreVal;
}
/**
 * The method is used to check whether or not a given value is already in the
 * domain list.
 * @param
 * tmpDomain-contains domain values of an attribute.
 * domainSize-the total number of values of the attribute
 * curVal- the current value is to be checked with existing domain values.
 * @return flag- is an integer value indicating Exist (1) or NOT exist(0)
 */
 private int findDomain(String []tmpDomain,int domainSize, String curVal)
    {
        int flag=-1;
        for(int i=0;i<domainSize;i++)
        {
            if(curVal.equals(tmpDomain[i]))
            {
               flag=i; break;
            }
        }
        return flag;
    }

/*
 * this will find the majority class value of a leaf
 */
public String findMajorityClassValues(int []attrType, int noAttr, String rule)
    {
        String rStr,cv="?";
        String data="";
        StringTokenizer tokenizerRule= new StringTokenizer(rule, " \t\n\r\f");
        for(int i=0;i<noAttr;i++)
        {
            rStr=tokenizerRule.nextToken();
            if(!rStr.equals("-")&&attrType[i]==2)
            {
               data= rStr;
               break;
            }
        }
        String tcv="";
        if(!data.equals(""))
        {
            int max=0,cmax=0;
            String cVal="",temp="";
            StringTokenizer tokenizerData = new StringTokenizer(data, " {};,\t\n\r\f");
            int cnt=tokenizerData.countTokens();
            for(int i=0;i<cnt;i++)
            {
                cVal=tokenizerData.nextToken();
                tcv=cVal;
                i++;
                temp=tokenizerData.nextToken();
                cmax=Integer.parseInt(temp);
                if(cmax>max)
                {
                   max=cmax;
                   cv=cVal;
                }
            }
        }
        if(cv.equals("?"))
        {
            cv=tcv;
        }
       return cv;
    }
/*
 * this will generate data file for each leaf
 */
private int FindLeafId(String []Record, int []attrType, int noAttr,
        String []rules, int noofRules)
    {
        int leafID=-1;
        for(int i=0;i<noofRules;i++)
        {
            if(isThisRecSatisfyRule(Record,attrType,noAttr,rules[i])==1)
            {
                leafID=i;break;
            }
        }
        return leafID;
    }

    
/*
 * this method set attrpos as class attribute in the dtNameFile
 */
public void setClassAttribute(String srcNameFile, String dtNameFile,int attrPos,int totalAttr)
    {
        
        StringTokenizer tokenizer;
        FileManager fileManager_g = new FileManager();

        String [] nameFile_g = fileManager_g.readFileAsArray(new File(srcNameFile));
        File outFile = new File(dtNameFile);
        
        
        String val;
        String []attrInfoG=new String[totalAttr];
        String []attrName=new String[totalAttr];
        for(int i=0; i<2;i++)
         {
            
            tokenizer = new StringTokenizer(nameFile_g[i], " ,\t\n\r\f");
            for(int j=0; j<totalAttr;j++)
                {
                    val=tokenizer.nextToken();
                    if(i==0)
                    {
                    if(val.equals("2"))
                            val="0";
                    if(j==attrPos)
                             val="2";
                    attrInfoG[j]=val;
                    }
                    else
                    {
                      attrName[j]=val;
                    }
                }
            String rec="";
            for(int j=0; j<totalAttr;j++)
                {
                    if(i==0)
                    {
                        if(j==0)
                            rec=attrInfoG[j];
                        else
                            rec=rec+" "+attrInfoG[j];
                    }
                    else
                    {
                      if(j==0)
                            rec=attrName[j];
                        else
                            rec=rec+","+attrName[j];
                    }
                }
          
            if(i==0) 
            {
                rec=rec+"\n";
                fileManager_g.writeToFile(outFile, rec);
             }
            else
                fileManager_g.appendToFile(outFile, rec);
            }
     }

/*
 * this method is used to exchange data between i-th position (attrPos) and the last place
 */
public void exChangeAttrPosition(String dtFile,int attrPos,int totalAttr)
    {
        int i,j;
        StringTokenizer tokenizer;
        FileManager fileManager_g = new FileManager();
        String [] dataFile_g = fileManager_g.readFileAsArray(new File(dtFile));
        File outFile = new File(dtFile);
        int noOfRec=dataFile_g.length;
        String []attrName=new String[totalAttr];
        for(i=0; i<noOfRec;i++)
         {

            tokenizer = new StringTokenizer(dataFile_g[i], " ,\t\n\r\f");
            for(j=0; j<totalAttr;j++)
                 attrName[j]=tokenizer.nextToken();
           String gtemp;
           gtemp=attrName[attrPos];
           attrName[attrPos]=attrName[totalAttr-1];
           attrName[totalAttr-1]=gtemp;
           String rec="";
           for(j=0; j<totalAttr;j++)
               if(j==0)
                     rec=attrName[j];
               else
                     rec=rec+", "+attrName[j];
           rec=rec+"\n";
           if(i==0)
                fileManager_g.writeToFile(outFile, rec);
           else
                fileManager_g.appendToFile(outFile, rec);
         }
    }
/*
 * this method is used to generalise a numerical attribute (attrPos)
 * into log|domainsize of attrPos| categories
 */
public void generalise(String srcFile, String dtFile,int attrPos,int totalAttr)
    {
        int i,j;
        StringTokenizer tokenizer;
        FileManager fileManager = new FileManager();
        String [][] dataFile = fileManager.readFileAs2DArray(new File(srcFile));
        File outFile = new File(dtFile);
        int noOfRec=dataFile.length;
        double []domain=new double[noOfRec];
        int domainSize=0;
        String val;
        double cval;
        for(i=0; i<noOfRec;i++)
        {
            val=dataFile[i][attrPos];
            if(isMissing(val)==0)
            {
                cval= Double.parseDouble(val);
                if(chkDomain(domain,domainSize,cval)==0)
                {
                    domain[domainSize]=cval;
                    domainSize++;
                }
            }
        }
        int NofGroups;
        if(domainSize>2)
//            NofGroups=(int)Math.round(Math.log1p((double)domainSize));
            NofGroups=(int)Math.round(Math.sqrt((double)domainSize));
        else
            NofGroups=domainSize;
        //Sorting by using sort(double[] d) method.
        Arrays.sort(domain,0,domainSize);

       int groupSize=0;
       if(NofGroups>0)
       {
           groupSize=(int)domainSize/NofGroups;
           int rem=domainSize%NofGroups;
           if(rem>0)
           {
              if (rem>(int) NofGroups/2.0)
              {
                  NofGroups++;
                  rem=0;
              }
           }
           double []lowDomain=new double[NofGroups];
           double []highDomain=new double[NofGroups];
           for(i=0,j=0 ;i<domainSize && j<NofGroups;j++)
           {
               lowDomain[j]=domain[i];
               i=i+groupSize-1;
               if(i>=domainSize) i=domainSize-1;
               highDomain[j]=domain[i];
               i++;
           }
      if(rem>0)
            highDomain[j-1]=domain[domainSize-1];
      
      for(i=0; i<noOfRec;i++)
        {
           String rec="";
            val=dataFile[i][attrPos];
            if(isMissing(val)==1)val="0";
            cval= Double.parseDouble(val);
            int fg=-1;
            for(j=0;j<NofGroups;j++)
            {
                if(lowDomain[j]<=cval && cval<=highDomain[j])
                {
                    fg=j;break;
                }
            }
            String rng;
            if(fg==-1)
            {
               rng= cval+"-"+cval;
            }
            else
            {
                rng= lowDomain[fg]+"-"+highDomain[fg];
            }
           dataFile[i][attrPos]=rng;
           for(j=0;j<totalAttr;j++)
               rec=rec+dataFile[i][j]+", ";
           //write record to file
           if(i==0)
           {
               rec=rec+"\n";
               fileManager.writeToFile(outFile, rec);
           }
           else
           {
                if(i<noOfRec-1)  rec=rec+"\n";
                fileManager.appendToFile(outFile, rec);
            }
        }
        }
    }
/**
  * this function will indicate whether or not a value is missing.
  *
  * @param oStr the string to be checked
  * @return ret an integer value 0->No missing, 1->Missing
  */

 private int isMissing(String oStr)
    {
       int ret=0;
       if(oStr.equals("")||oStr.equals("?")||oStr.equals("�")||oStr.equals("NaN")||oStr.equals("  NaN"))
                     {
                         ret=1;
                    }
       return ret;
    }
private int chkDomain(double []domain,int domainSize, double curVal)
    {
        int flag=0;
        for(int i=0;i<domainSize;i++)
        {
            if(curVal==domain[i])
            {
               flag=1; break;
            }
        }
        return flag;
    }

/*
 * this will check whether a record satisfy a rule
 */
public int isThisRecSatisfyRule(String []Record, int []attrType, int noAttr, String rule)
    {
        int flag=0;
        String dStr,rStr;
        int match=0, condition=0;
        StringTokenizer tokenizerRule= new StringTokenizer(rule, " \t\n\r\f");
        for(int i=0;i<noAttr;i++)
        {
            dStr=Record[i];
            rStr=tokenizerRule.nextToken();
            if(!rStr.equals("-")&&attrType[i]!=2)
            {
                condition++;
                if(isMissing(dStr)==0)
                {
                if(attrType[i]==0)   //for categorical
                {
                    if(rStr.equals(dStr))
                    {
                        match++;
                    }

                }
                else if(attrType[i]==1)  //for numerical
                {
                  double dVal=Double.parseDouble(dStr);
                  String dh;
                  dh=rStr.substring(1, rStr.length());
                  if(rStr.startsWith("G"))
                  {
                      double drul=Double.parseDouble(dh);
                      if(dVal>drul)match++;

                  }
                  else if(rStr.startsWith("L"))
                  {
                      double drul=Double.parseDouble(dh);
                      if(dVal<=drul)match++;
                  }
                  else if(rStr.startsWith("R"))
                  {
                      int indexOfComma = dh.lastIndexOf(",");
                      double leftDh=Double.parseDouble(dh.substring(0, indexOfComma-1));
                      double rightDh=Double.parseDouble(dh.substring(indexOfComma+1, dh.length()));
                      if(leftDh==rightDh)
                      {
                            if(dVal==rightDh)match++;
                      }
                     else if(leftDh<rightDh)
                      {
                          if(dVal>=leftDh && dVal<=rightDh)match++;
                     }
                      else
                      {
                           if(dVal>=rightDh && dVal<=leftDh)match++;
                      }

                  }
                }
                }
            }
        }
       if(match==condition) flag=1;   //record satisfied the rule
       return flag;
    }

/*
 * this will return the no. of rules for a specific tree
 */
public int noOfRules(String treeFile)
    {
        FileManager fileManager_g = new FileManager();
        String [] rules = fileManager_g.readFileAsArray(new File(treeFile));
        int noR=rules.length-1;
        if(noR<0) noR=0;
        return noR;
    }
}
