/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kdmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Main {
        /** command line reader */
    BufferedReader stdIn;
        /** class name, used in logging errors */
    static String className = kdmi.Main.class.getName();
    
    public Main()
    {
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main terminal=new Main();
        String fileAttrInfo = terminal.inputFileName("Please enter the name of the file containing the 2 line attribute information.(example: c:\\data\\attrinfo.txt?)");
        String fileDataFileIn= terminal.inputFileName("Please enter the name of the data file having missing values: (example: c:\\data\\data.txt?)");
        String fileOutput = terminal.inputFileName("Please enter the name of the output file: (example: c:\\data\\out.txt?)");
        //call kDMI
        kDMI kdmi=new kDMI();
        kdmi.runkDMI(fileAttrInfo, fileDataFileIn, fileOutput);
        System.out.println("\nImputation by kDMI is done. The completed data set is written to: \n"+fileOutput);
    }
      

    /**
     * Given a message to display to the user, ask user to enter a file name.
     *
     * @param message message to user prompting for filename
     * @return filename entered by user
     */
    private String inputFileName(String message)
    {
        String fileName = "";
        try
        {
            System.out.println(message);
            fileName = stdIn.readLine();
        }
        catch (IOException ex)
        {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
        }
        return fileName;
    }

}
