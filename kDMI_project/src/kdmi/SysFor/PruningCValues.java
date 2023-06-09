

package SysFor;

/**
 * This class manages the calculation of the 'c' values using in the pruning
 * approximation calculations.
 *
 * @author helengiggins
 * @version 1.0 11/2/2011
 */
public class PruningCValues {

    /** instance variables */

    private static final double [][] cLookUp =
 {{Double.POSITIVE_INFINITY, 3.0902, 2.8782, 2.7478, 2.6521, 2.5758, 2.5151, 2.4573, 2.4089, 2.3656},
        {2.3263, 2.2904, 2.2571, 2.2262, 2.1973, 2.1701, 2.1444, 2.1201, 2.0969, 2.0749},
        {2.0537, 2.0335, 2.0141, 1.9954, 1.9774, 1.9600, 1.9431, 1.9268, 1.9110, 1.8957},
        {1.8808, 1.8663, 1.8522, 1.8384, 1.8250, 1.8119, 1.7991, 1.7866, 1.7744, 1.7624},
        {1.7507, 1.7392, 1.7279, 1.7169, 1.7060, 1.6954, 1.6849, 1.6747, 1.6646, 1.6546},

        {1.6449, 1.6352, 1.6258, 1.6164, 1.6072, 1.5982, 1.5893, 1.5805, 1.5718, 1.5632},
        {1.5548, 1.5464, 1.5382, 1.5301, 1.5220, 1.5141, 1.5063, 1.4985, 1.4909, 1.4833},
        {1.4758, 1.4684, 1.4611, 1.4538, 1.4466, 1.4395, 1.4325, 1.4255, 1.4187, 1.4118},
        {1.4051, 1.3984, 1.3917, 1.3852, 1.3787, 1.3722, 1.3658, 1.3595, 1.3532, 1.3469},
        {1.3405, 1.3346, 1.3285, 1.3225, 1.3165, 1.3105, 1.3047, 1.2988, 1.2930, 1.2873},

        {1.2816, 1.2759, 1.2702, 1.2646, 1.2591, 1.2536, 1.2481, 1.2426, 1.2372, 1.2319},
        {1.2265, 1.2212, 1.2160, 1.2107, 1.2055, 1.2004, 1.1952, 1.1901, 1.1850, 1.1800},
        {1.1750, 1.1700, 1.1650, 1.1601, 1.1552, 1.1503, 1.1455, 1.1407, 1.1359, 1.1311},
        {1.1264, 1.1217, 1.1170, 1.1123, 1.1077, 1.1031, 1.0985, 1.0939, 1.0893, 1.0848},
        {1.0803, 1.0758, 1.0714, 1.0669, 1.0625, 1.0581, 1.0537, 1.0494, 1.0450, 1.0407},

        {1.0364, 1.0322, 1.0279, 1.0237, 1.0194, 1.0152, 1.0110, 1.0069, 1.0027, 0.9986},
        {0.9945, 0.9904, 0.9863, 0.9822, 0.9782, 0.9741, 0.9701, 0.9661, 0.9621, 0.9581},
        {0.9542, 0.9502, 0.9463, 0.9424, 0.9385, 0.9346, 0.9307, 0.9269, 0.9230, 0.9192},
        {0.9154, 0.9116, 0.9078, 0.9040, 0.9002, 0.8965, 0.8927, 0.8890, 0.8853, 0.8816},
        {0.8779, 0.8742, 0.8705, 0.8669, 0.8633, 0.8596, 0.8560, 0.8524, 0.8488, 0.8452},

        {0.8416, 0.8381, 0.8345, 0.8310, 0.8274, 0.8230, 0.8204, 0.8169, 0.8134, 0.8099},
        {0.8064, 0.8030, 0.7995, 0.7961, 0.7926, 0.7892, 0.7858, 0.7824, 0.7790, 0.7756},
        {0.7722, 0.7688, 0.7655, 0.7621, 0.7588, 0.7554, 0.7521, 0.7488, 0.7454, 0.7421},
        {0.7388, 0.7356, 0.7323, 0.7290, 0.7257, 0.7225, 0.7192, 0.7160, 0.7128, 0.7095},
        {0.7063, 0.7031, 0.6999, 0.6967, 0.6935, 0.6903, 0.6871, 0.6840, 0.6808, 0.6776},

        {0.6745, 0.6713, 0.6682, 0.6651, 0.6620, 0.6588, 0.6557, 0.6526, 0.6495, 0.6464},
        {0.6433, 0.6403, 0.6372, 0.6341, 0.6311, 0.6280, 0.6250, 0.6219, 0.6189, 0.6158},
        {0.6128, 0.6098, 0.6068, 0.6038, 0.6008, 0.5978, 0.5948, 0.5918, 0.5888, 0.5858},
        {0.5828, 0.5799, 0.5769, 0.5740, 0.5710, 0.5681, 0.5651, 0.5622, 0.5592, 0.5563},
        {0.5534, 0.5505, 0.5476, 0.5446, 0.5417, 0.5388, 0.5359, 0.5330, 0.5302, 0.5273},

        {0.5244, 0.5215, 0.5187, 0.5158, 0.5129, 0.5101, 0.5072, 0.5044, 0.5015, 0.4987},
        {0.4959, 0.4930, 0.4902, 0.4874, 0.4845, 0.4817, 0.4789, 0.4761, 0.4733, 0.4705},
        {0.4677, 0.4649, 0.4621, 0.4593, 0.4565, 0.4538, 0.4510, 0.4482, 0.4454, 0.4427},
        {0.4399, 0.4372, 0.4344, 0.4316, 0.4289, 0.4261, 0.4234, 0.4207, 0.4179, 0.4152},
        {0.4125, 0.4097, 0.4070, 0.4043, 0.4016, 0.3989, 0.3961, 0.3934, 0.3907, 0.3880},

        {0.3853, 0.3826, 0.3799, 0.3772, 0.3745, 0.3719, 0.3692, 0.3665, 0.3638, 0.3611},
        {0.3585, 0.3558, 0.3531, 0.3505, 0.3478, 0.3451, 0.3425, 0.3398, 0.3372, 0.3345},
        {0.3319, 0.3292, 0.3266, 0.3239, 0.3213, 0.3186, 0.3160, 0.3134, 0.3107, 0.3081},
        {0.3055, 0.3029, 0.3002, 0.2976, 0.2950, 0.2924, 0.2898, 0.2871, 0.2845, 0.2819},
        {0.2793, 0.2767, 0.2741, 0.2715, 0.2689, 0.2663, 0.2637, 0.2611, 0.2585, 0.2559},

        {0.2533, 0.2508, 0.2482, 0.2456, 0.2430, 0.2404, 0.2378, 0.2353, 0.2327, 0.2301},
        {0.2275, 0.2250, 0.2224, 0.2198, 0.2173, 0.2147, 0.2121, 0.2096, 0.2070, 0.2045},
        {0.2019, 0.1993, 0.1968, 0.1942, 0.1917, 0.1891, 0.1866, 0.1840, 0.1815, 0.1789},
        {0.1764, 0.1738, 0.1713, 0.1687, 0.1662, 0.1637, 0.1611, 0.1586, 0.1560, 0.1535},
        {0.1510, 0.1484, 0.1459, 0.1434, 0.1408, 0.1383, 0.1358, 0.1332, 0.1307, 0.1282},

        {0.1257, 0.1231, 0.1206, 0.1181, 0.1156, 0.1130, 0.1105, 0.1080, 0.1055, 0.1030},
        {0.1004, 0.0979, 0.0954, 0.0929, 0.0904, 0.0878, 0.0853, 0.0828, 0.0803, 0.0778},
        {0.0753, 0.0728, 0.0702, 0.0677, 0.0652, 0.0627, 0.0602, 0.0577, 0.0552, 0.0527},
        {0.0502, 0.0476, 0.0451, 0.0426, 0.0401, 0.0376, 0.0351, 0.0326, 0.0301, 0.0276},
        {0.0251, 0.0226, 0.0201, 0.0175, 0.0150, 0.0125, 0.0100, 0.0075, 0.0050, 0.0025},
        {0.0000}};

    /**
     * Using the table lookup the c value for the passed double.
     *
     * @param lookup table index we are looking up the c value for in range [0.0,0.50]
     * @return c value from table
     */
    public static double getCValue(double lookup)
    {
        /** firstly round the lookup value to 3 decimal places
         * The 5th character will be the column lookup index, and
         * the 3rd and 4th character give the row lookup index.
         */
        double rounded = GeneralFunctions.roundToDecimals(lookup,3);
        String roundedStr = String.valueOf(rounded);
        try{

            //System.out.println(roundedStr);
            /** check length of rounded string, for cases where we don't have 5 chars *
             * e.g. 0.5, 0.12 etc.
             */
            int yIndex =0;
            if(roundedStr.length()==5) //e.g. 0.1234
            {
                yIndex = Integer.parseInt(roundedStr.substring(4, 5));
            }
            int xIndex = 0;
            if(roundedStr.length()>=4) //e.g. 0.123
            {
                xIndex = Integer.parseInt(roundedStr.substring(2, 4));
            }
            if(roundedStr.length()==3) //e.g 0.5
            {
               int xMultiplier =  xIndex = Integer.parseInt(roundedStr.substring(2, 3));
               xIndex = xMultiplier*10; //need to times by 10 to get correct index.
            }
            double value = cLookUp[xIndex][yIndex];
            //System.out.println(" x: " + xIndex + " y: " +yIndex + " value: " + value);
            return value;
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e);
            return -1.0;
        }
    }

}
