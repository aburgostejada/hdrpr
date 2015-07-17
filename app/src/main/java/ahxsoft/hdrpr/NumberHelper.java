package ahxsoft.hdrpr;


public class NumberHelper {
    public static double fromStringFraction(String fraction){
        String[] fractionArray = fraction.split("/");
        try {
            if (fractionArray.length != 2){
                if (fractionArray.length == 1){
                    return Double.parseDouble(fractionArray[0]);
                }
                else {
                    return 0d;
                }
            }
            double b = Double.parseDouble(fractionArray[1]);
            if (b==0d){
                return 0d;
            }
            Double a = Double.parseDouble(fractionArray[0]);
            return a/b;
        }
        catch (NumberFormatException e){
            return 0d;
        }
    }
}
