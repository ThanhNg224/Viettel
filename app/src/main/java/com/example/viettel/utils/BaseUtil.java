package com.example.viettel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BaseUtil {
    public static void disableIPv6() {
        exec("echo 1 > /proc/sys/net/ipv6/conf/all/disable_ipv6");
    }

    public static void copyApkFromAssets(Context context, String apkName, String outputPath) {
        try {
            InputStream inputStream = context.getAssets().open(apkName);
            FileOutputStream outputStream = new FileOutputStream(outputPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getInstalledAppVersionCode(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1; // App chưa cài
        }
    }

    public static boolean exec(String cmd) {
        boolean ret = false;
        try {
            Process p = Runtime.getRuntime().exec("su");
            try (DataOutputStream out = new DataOutputStream(p.getOutputStream())) {
                out.writeBytes(cmd + "\n");
                out.flush();
                out.writeBytes("exit\n");
                out.flush();
                p.waitFor();
                ret = p.exitValue() == 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getLastRebootTime(){
        long currentTime = System.currentTimeMillis();
        long uptime = SystemClock.elapsedRealtime(); // in milliseconds
        long rebootTime = currentTime - uptime;

        Date rebootDate = new Date(rebootTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(rebootDate);
    }

    public static void autoClearCache(){
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "sync; echo 3 > /proc/sys/vm/drop_caches"});
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDefaultDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(1900, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date defaultDate = cal.getTime();

        return date.equals(defaultDate);
    }

    public static String runCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process;

        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    public static void broadcastAction(Context context, String actionName){
        Intent intent = new Intent();
        intent.setAction(actionName);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        context.sendBroadcast(intent);
    }

    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public static boolean checkPoinInsideCircle(int x, int centerX, int y, int centerY, int r){
        int dx = Math.abs(x-centerX);
        int dy = Math.abs(y-centerY);

        if (dx + dy <= r){
            return true;
        }

        if (dx > r){
            return false;
        }

        if (dy > r){
            return false;
        }

        if (dx*dx + dy*dx <= r*r){
            return true;
        }

        return false;
    }

    public static void reboot() throws Exception {
        try {
            Runtime.getRuntime().exec("su");
            Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
            throw new Exception("Device is not support this function");
        }
    }

    public static boolean isConnectedToThisServer(String host) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + host);
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String removeFirstZeroNumber(String input){
        if(input == null || input.length() == 0){
            return "";
        }

        input = input.trim();
        if(input.startsWith("0")){
            input = input.substring(1, input.length());
            removeFirstZeroNumber(input);
        }else{
            return input;
        }

        return  input;
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static String convertByteToBase64(byte[] bytearray){
        String codeBase64 = "";

        try{
            codeBase64 = Base64.encodeToString(bytearray, Base64.NO_WRAP);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return codeBase64;
    }

    public static String convertImageToBase64(File imageFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();

            byte[] imageBytes = outputStream.toByteArray();
            outputStream.close();

            // Encode the image data to Base64
            String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            return base64String;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] convertBase64ToByte(String codeBase64){
        byte[] bytearray = new byte[0];

        try{
            bytearray = Base64.decode(codeBase64, Base64.NO_WRAP);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return bytearray;
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static boolean screenBrightness(int level, Context context) {
        try {
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, level);


            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    level);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static boolean checkMD5(String TAG, String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = BaseUtil.calculateMD5(TAG, updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(String TAG, File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static boolean regexIpv4(String ip) {
        if(ip == null || ip.length()==0)
            return false;

        Pattern p = Pattern.compile("^"
                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
                + "|"
                + "localhost" // localhost
                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip
                + ":"
                + "[0-9]{1,5}$"); // Port

        return p.matcher(ip).matches();
    }

    //Kiểm tra đã cài gói service cho phần nhiệt độ chưa
    public static boolean isInstalledPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    public static boolean downloadImage(String urlLink, File file) throws Exception{
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream out = null;
        Boolean blResult = true;

        try{
            URL url = new URL(urlLink);
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK){
                return false;
            }

            input = connection.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(input);
            out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out); // Compress Image
            out.flush();
        }catch(IOException e){
            blResult = false;
            e.printStackTrace();
        } finally {
            if(out != null){
                out.close();
            }
            if(input != null){
                input.close();
            }

            if(connection != null){
                connection.disconnect();
            }
        }
        return blResult;
    }

    public static boolean saveImage(String imageBase64, File file){
        Boolean blResult = true;

        byte[] decodedBytes = Base64.decode(imageBase64, Base64.NO_WRAP);
        try (OutputStream stream = new FileOutputStream(file)) {
            stream.write(decodedBytes);
        }catch (Exception e){
            blResult = false;
            e.printStackTrace();
        }
        return blResult;
    }

    public static boolean downloadImage(String urlLink, File file, String extension) throws Exception{
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream out = null;
        Boolean blResult = true;
        Bitmap.CompressFormat formatFile = Bitmap.CompressFormat.JPEG;

        try{
            URL url = new URL(urlLink);
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK){
                return false;
            }

            if(extension.toUpperCase().contains("JPEG")){
                formatFile = Bitmap.CompressFormat.JPEG;
            }else if(extension.toUpperCase().contains("PNG")){
                formatFile = Bitmap.CompressFormat.PNG;
            }else if(extension.toUpperCase().contains("WEBP")){
                formatFile = Bitmap.CompressFormat.WEBP;
            }

            input = connection.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(input);
            out = new FileOutputStream(file);
            bm.compress(formatFile, 100, out); // Compress Image
            out.flush();
        }catch(IOException e){
            blResult = false;
            e.printStackTrace();
        } finally {
            if(out != null){
                out.close();
            }
            if(input != null){
                input.close();
            }

            if(connection != null){
                connection.disconnect();
            }
        }
        return blResult;
    }

    public static boolean downloadLogo(String urlLink, File file, String extension) throws Exception{
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream out = null;
        Boolean blResult = true;
        Bitmap.CompressFormat formatFile = Bitmap.CompressFormat.JPEG;

        try{
            URL url = new URL(urlLink);
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK){
                return false;
            }

            if(extension.toUpperCase().contains("JPEG")){
                formatFile = Bitmap.CompressFormat.JPEG;
            }else if(extension.toUpperCase().contains("PNG")){
                formatFile = Bitmap.CompressFormat.PNG;
            }else if(extension.toUpperCase().contains("WEBP")){
                formatFile = Bitmap.CompressFormat.WEBP;
            }

            input = connection.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(input);

            int width = bm.getWidth();
            int height = bm.getHeight();

            if(width > 512 && height > 512){
                int newWidth = 512;
                int newHeight = ( newWidth * height )/width;
                bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
            }else if(width > 512){
                int newWidth = 512;
                int newHeight = ( newWidth * height )/width;
                bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
            }else if (height > 512){
                int newHeight = 512;
                int newWidth = ( newHeight * width )/height;
                bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
            }

            out = new FileOutputStream(file);
            bm.compress(formatFile, 100, out); // Compress Image
            out.flush();
        }catch(IOException e){
            blResult = false;
            e.printStackTrace();
        } finally {
            if(out != null){
                out.close();
            }
            if(input != null){
                input.close();
            }

            if(connection != null){
                connection.disconnect();
            }
        }
        return blResult;
    }

    public static int getMaxSyncId(){
        int syncId = 1;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);
            syncId = Integer.parseInt(StringUtils.convertDateToString(cal.getTime(), "yyMMddHHmm"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return syncId;
    }

    public static int getSyncId(){
        int syncId = 1;
        try {
            syncId = Integer.parseInt(StringUtils.convertDateToString(Calendar.getInstance().getTime(), "yyMMddHHmm"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return syncId;
    }

    public static boolean compareTimeCheckOut (String strNextTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date nextTime = sdf.parse(strNextTime);
            Date currentTime = Calendar.getInstance().getTime();

            if(currentTime.before(nextTime)){
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getLine(StringBuffer buf) throws Exception {
        try {
            int iIndex = buf.indexOf("\n");
            String strReturn;
            if (iIndex < 0) {
                strReturn = buf.toString();
                iIndex = buf.length() - 1;
                if (strReturn.equals("")) {
                    strReturn = null;
                }
            } else {
                strReturn = buf.substring(0, iIndex);
            }
            buf.delete(0, iIndex + 1);
            return strReturn;
        } catch (Exception ex) {
            throw new Exception("Get line: " + ex.toString());
        }
    }

    public static String getLocalIpv4() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return "";
    }

    public static boolean compareLanNetwork(String ip1, String ip2) {
        if (ip1 == null || ip1.equals("") || ip2 == null || ip2.equals(""))
            return false;

        String arrIp1[] = ip1.split("\\.");
        String arrIp2[] = ip2.split("\\.");

        if (arrIp1.length != 4 || arrIp2.length != 4)
            return false;

        String netip1 = arrIp1[0] + "." + arrIp1[1] + "." + arrIp1[2];
        String netip2 = arrIp2[0] + "." + arrIp2[1] + "." + arrIp2[2];

        if (netip1.equals(netip2)) {
            return true;
        }

        return false;
    }

    public String getLocalIpV6() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return "";
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    @SuppressLint("MissingPermission")
    public static String getImeiNumber(Context context) {
        String imeiNumber = "";
        Log.d("THAI", Build.MODEL);

        switch (Build.MODEL){
            default:
                try {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (null != tm) {
                        imeiNumber = tm.getDeviceId();
                    }
                    if (null == imeiNumber || 0 == imeiNumber.length() || "0".equals(imeiNumber)) {
                        imeiNumber = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    }

                    //imeiNumber = tm.getImei();
                }catch (Exception ex){
                    Log.e("IMEI", ex.getMessage());
                }
                break;

        }
        Log.d("THAI", "OUT -> " + imeiNumber);
        return imeiNumber;
    }

    /**
     * Get serial number of device
     * @return the serial number
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static String getHashMD5(String input){
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String jsonObjectToString(JsonObject jsonObject, String fieldName){
        return jsonObject.get(fieldName).toString().replaceAll("\"", "");
    }

    public static Bitmap decodeBase64Profile(String input) {
        Bitmap bitmap = null;
        if (input != null) {
            byte[] decodedByte = Base64.decode(input, 0);
            bitmap = BitmapFactory
                    .decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(availableBlocks * blockSize);
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize);
    }

    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatSize(availableBlocks * blockSize);
        } else {
            return "";
        }
    }

    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatSize(totalBlocks * blockSize);
        } else {
            return "";
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = " GB";
                    size /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static File[] getListFile(String filePath, final String extendFile) {
        File[] files = new File[0];

        try {
            File f = new File(filePath);

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return name.endsWith(extendFile);
                }
            };

            files = f.listFiles(filter);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return files;
    }

    public static void saveImageFile (File fileSave, byte[] data){
        if (fileSave.exists()) {
            fileSave.delete();
        }

        try {
            FileOutputStream fos=new FileOutputStream(fileSave.getPath());
            fos.write(data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
