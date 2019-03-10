package com.mahc.custombottomsheet.util;

public class AppUtils {
   public static int getMarkerColor(int i){
        if(i%2==0) {
            return AppConstants.RED;
        }//    markers.put(hamburg.getId(), "http://img.india-forums.com/images/100x100/37525-a-still-image-of-akshay-kumar.jpg");
        else if(i%3==0) {
            return AppConstants.GREEN;
        }
        else if(i%5==0) {
            return AppConstants.VIOLET;
        }
        else {
            return AppConstants.BLUE;
        }
    }
}
