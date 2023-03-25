# kDMI

kDMI employs two levels of horizontal partitioning (based on a decision tree and k-NN algorithm) of a data set, in order to find the records that are very similar to the one with missing value/s. Additionally, it uses a novel approach to automatically find the value of k for each record.

# Reference

Rahman, M. G. and Islam, M. Z. (2013): kDMI: A Novel Method for Missing Values Imputation Using Two Levels Horizontal Partitioning in a Data set, In Proc. of the 9th International Conference on Advanced Data Mining and Applications(ADMA 13), Hangzhou, China, 14-16 December 2013, pp. 250-263. 

## BibTeX
```
@inproceedings{rahman2013kdmi,
  title={kDMI: A novel method for missing values imputation using two levels of horizontal partitioning in a data set},
  author={Rahman, Md Geaur and Islam, Md Zahidul},
  booktitle={Advanced Data Mining and Applications: 9th International Conference, ADMA 2013, Hangzhou, China, December 14-16, 2013, Proceedings, Part II 9},
  pages={250--263},
  year={2013},
  organization={Springer}
}
```

@author Gea Rahman <https://csusap.csu.edu.au/~grahman/>
  
# Two folders:
 
 1. kDMI_project (NetBeans project)
 2. SampleData 
 
kDMI is developed based on Java programming language (jdk1.8.0_211) using NetBeans IDE (8.0.2). 
 
# How to run:
 
	1. Open project in NetBeans
	2. Run the project

# Sample input and output:
run:
Please enter the name of the file containing the 2 line attribute information.(example: c:\data\attrinfo.txt)

C:\SampleData\attrinfo.txt

Please enter the name of the data file having missing values: (example: c:\data\data.txt)

C:\SampleData\data.txt

Please enter the name of the output file: (example: c:\data\out.txt)

C:\SampleData\output.txt


Imputation by kDMI is done. The completed data set is written to: 

C:\SampleData\output.txt