package com.example.viettel.utils;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class StringUtils {
    public static  String getThrowCause(Exception ex){
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static String currentDateSQLiteformat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String currentDateLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String currentDatetimeSQLiteformat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String currentDatetimeMilisecondSQLiteformat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String datetimeSQLiteformat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static Date stringToDateSQLiteformat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return sdf.parse(date);
        }catch (Exception ex){
            return new Date();
        }
    }

    public static Date dateSQLiteformat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try{
            return sdf.parse(currentDateSQLiteformat());
        }catch (Exception ex){
            return new Date();
        }
    }

    public static long getBetweenSecond(String startDate, String endDate){
        Date dtStartDate = convertStringToDate(startDate, "yyyy-MM-dd HH:mm:ss");
        Date dtEndDate = convertStringToDate(endDate, "yyyy-MM-dd HH:mm:ss");
        long along = (dtEndDate.getTime() - dtStartDate.getTime());
        return along;
    }

    public static long getBetweenSecond(Date dtStartDate, Date dtEndDate){
        long along = (dtEndDate.getTime() - dtStartDate.getTime());
        return along;
    }

    public static String Nvl(String obj){
        if(obj == null ){
            return "";
        }
        return obj;
    }

    public static String Nvl(String obj, String replace){
        if(obj == null){
            return replace;
        }
        return obj;
    }

    public static String encodeParams(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while (itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    public static String convertDateToString(Date date, String format){
        DateFormat dateFormat = new SimpleDateFormat(format);
        try{
            return dateFormat.format(date);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static String convertDateToString(Date date){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try{
            return dateFormat.format(date);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static Date convertStringToDate(String strDate){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
             return formatter.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNameOfDate (Date date){
        try {
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
            return dayOfWeek;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date convertStringToDate(String strDate, String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String plusDayToDate (String date, int addDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try{
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(date));
            c.add(Calendar.DAY_OF_MONTH, addDay);

            //Date after adding the days to the given date
            date = sdf.format(c.getTime());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return date;
    }

    public static Date plusDayToDate (Date date, int addDay) {
        try{
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DAY_OF_MONTH, addDay);

            //Date after adding the days to the given date
            date = c.getTime();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return date;
    }

    public static Date plusTimeToDate (Date date, int plusTime, int timeType) {
        try{
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(timeType, plusTime);

            //Date after adding the days to the given date
            date = c.getTime();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return date;
    }

    public static String nvl(String input){
        if(input == null){
            return "";
        }

        return input;
    }

    public static String nvl(String input, String replaceValue){
        if(input == null){
            return replaceValue;
        }

        return input;
    }

    public static int parseInteger (String input){
        if (input == null || input == ""){
            return 0;
        }

        return Integer.parseInt(input);
    }

    public static String getDateTime(String format) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            Date date = Calendar.getInstance().getTime();
            String datetime = dateFormat.format(date);
            return datetime;
        }catch (Exception e){
            throw e;
        }
    }

    public static String getDayAndTime() {
        String value = "";
        try {
            value = getWeekdays() +  StringUtils.getDateTime("',' dd 'tháng' MM '/' HH:mm:ss");
        }catch (Exception e){
            value = "";
        }
        return value;
    }

    public static String getTime() {
        String value = "";
        try {
            value = StringUtils.getDateTime("HH:mm:ss");
        }catch (Exception e){
            value = "";
        }
        return value;
    }

    public static String getWeekdays(){
        String weekDay = "";

        try{
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

            switch (dayOfWeek){
                case Calendar.MONDAY:
                    weekDay = "Thứ hai";
                    break;
                case Calendar.TUESDAY:
                    weekDay = "Thứ ba";
                    break;
                case Calendar.WEDNESDAY:
                    weekDay = "Thứ tư";
                    break;
                case Calendar.THURSDAY:
                    weekDay = "Thứ năm";
                    break;
                case Calendar.FRIDAY:
                    weekDay = "Thứ sáu";
                    break;
                case Calendar.SATURDAY:
                    weekDay = "Thứ bảy";
                    break;
                case Calendar.SUNDAY:
                    weekDay = "Chủ nhật";
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return weekDay;
    }
}
