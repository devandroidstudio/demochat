package com.example.chatapplication.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeDifference {
    public static String findDateDiff(String date){
        String result = "";

        Date dateCurrent = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strDateCurrent = format.format(dateCurrent);
        try {
            Date d1 = format.parse(date);
            Date d2 = format.parse(strDateCurrent);


            long difference_In_Time
                    = d2.getTime() - d1.getTime();
            long difference_In_Seconds
                    = TimeUnit.MILLISECONDS
                    .toSeconds(difference_In_Time)
                    % 60;

            long difference_In_Minutes
                    = TimeUnit
                    .MILLISECONDS
                    .toMinutes(difference_In_Time)
                    % 60;

            long difference_In_Hours
                    = TimeUnit
                    .MILLISECONDS
                    .toHours(difference_In_Time)
                    % 24;
            if (difference_In_Seconds != 0){
                result = String.format("Hoạt động %d giây trước", difference_In_Seconds);
            }
            if (difference_In_Minutes != 0){
                result = String.format("Hoạt động %d phút trước", difference_In_Minutes);
            }
            if (difference_In_Hours != 0){
                result = String.format("Hoạt động %d giờ trước", difference_In_Hours);
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
    public static String findDateDiffStatus(String date){
        String result = "";

        Date dateCurrent = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strDateCurrent = format.format(dateCurrent);
        try {
            Date d1 = format.parse(date);
            Date d2 = format.parse(strDateCurrent);


            long difference_In_Time
                    = d2.getTime() - d1.getTime();
            long difference_In_Seconds
                    = TimeUnit.MILLISECONDS
                    .toSeconds(difference_In_Time)
                    % 60;

            long difference_In_Minutes
                    = TimeUnit
                    .MILLISECONDS
                    .toMinutes(difference_In_Time)
                    % 60;

            long difference_In_Hours
                    = TimeUnit
                    .MILLISECONDS
                    .toHours(difference_In_Time)
                    % 24;
            if (difference_In_Seconds != 0){
                result = String.format("%ds", difference_In_Seconds);
            }
            if (difference_In_Minutes != 0){
                result = String.format("%dp", difference_In_Minutes);
            }
            if (difference_In_Hours != 0){
                result = String.format("%dh", difference_In_Hours);
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
