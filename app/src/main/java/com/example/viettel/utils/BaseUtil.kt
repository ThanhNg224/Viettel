package com.example.viettel.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.reflect.Method
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object BaseUtil {
    fun disableIPv6() {
        exec("echo 1 > /proc/sys/net/ipv6/conf/all/disable_ipv6")
    }

    fun copyApkFromAssets(context: Context, apkName: String, outputPath: String) {
        runCatching {
            context.assets.open(apkName).use { inputStream ->
                FileOutputStream(outputPath).use { outputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
        }.onFailure { it.printStackTrace() }
    }

    fun getInstalledAppVersionCode(context: Context, packageName: String): Int {
        return try {
            val info = context.packageManager.getPackageInfo(packageName, 0)
            info.versionCode
        } catch (_: PackageManager.NameNotFoundException) {
            -1
        }
    }

    fun exec(cmd: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { out ->
                out.writeBytes("$cmd\n")
                out.flush()
                out.writeBytes("exit\n")
                out.flush()
                process.waitFor()
                process.exitValue() == 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    fun getLastRebootTime(): String {
        val currentTime = System.currentTimeMillis()
        val uptime = SystemClock.elapsedRealtime()
        val rebootTime = currentTime - uptime
        val rebootDate = Date(rebootTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(rebootDate)
    }

    fun autoClearCache() {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "sync; echo 3 > /proc/sys/vm/drop_caches"))
            process.waitFor()
        }.onFailure { it.printStackTrace() }
    }

    fun isDefaultDate(date: Date): Boolean {
        val cal = Calendar.getInstance().apply {
            set(1900, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return date == cal.time
    }

    fun runCommand(command: String): String {
        val output = StringBuilder()
        runCatching {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }
        }.onFailure { it.printStackTrace() }
        return output.toString()
    }

    fun broadcastAction(context: Context, actionName: String) {
        Intent().apply {
            action = actionName
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }.also(context::sendBroadcast)
    }

    @Throws(IOException::class)
    fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) return
        if (fileToZip.isDirectory) {
            val entryName = if (fileName.endsWith("/")) fileName else "$fileName/"
            zipOut.putNextEntry(ZipEntry(entryName))
            zipOut.closeEntry()
            fileToZip.listFiles()?.forEach { child ->
                zipFile(child, "$fileName/${child.name}", zipOut)
            }
            return
        }
        FileInputStream(fileToZip).use { fis ->
            val zipEntry = ZipEntry(fileName)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            var length: Int
            while (fis.read(bytes).also { length = it } >= 0) {
                zipOut.write(bytes, 0, length)
            }
        }
    }

    fun checkPointInsideCircle(x: Int, centerX: Int, y: Int, centerY: Int, r: Int): Boolean {
        val dx = kotlin.math.abs(x - centerX)
        val dy = kotlin.math.abs(y - centerY)

        if (dx + dy <= r) return true
        if (dx > r || dy > r) return false
        return dx * dx + dy * dy <= r * r
    }

    @Throws(Exception::class)
    fun reboot() {
        try {
            Runtime.getRuntime().exec("su")
            Runtime.getRuntime().exec("reboot")
        } catch (e: IOException) {
            throw Exception("Device is not support this function")
        }
    }

    fun isConnectedToThisServer(host: String): Boolean {
        return try {
            val ipProcess = Runtime.getRuntime().exec("/system/bin/ping -c 1 $host")
            ipProcess.waitFor() == 0
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    fun removeFirstZeroNumber(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val trimmed = input.trim()
        return if (trimmed.startsWith("0")) removeFirstZeroNumber(trimmed.substring(1)) else trimmed
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetworkInfo
        return network != null && network.isConnected
    }

    fun convertByteToBase64(byteArray: ByteArray): String {
        return runCatching { Base64.encodeToString(byteArray, Base64.NO_WRAP) }.getOrElse {
            it.printStackTrace()
            ""
        }
    }

    fun convertImageToBase64(imageFile: File): String? {
        return try {
            FileInputStream(imageFile).use { fileInputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun convertBase64ToByte(codeBase64: String): ByteArray {
        return runCatching { Base64.decode(codeBase64, Base64.NO_WRAP) }
            .onFailure { it.printStackTrace() }
            .getOrDefault(ByteArray(0))
    }

    fun dpToPx(dp: Int, context: Context): Float {
        val density = context.resources.displayMetrics.density
        return kotlin.math.round(dp * density)
    }

    fun screenBrightness(level: Int, context: Context): Boolean {
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
            )
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level)
            true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("TrulyRandom")
    fun handleSSLHandshake() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                },
            )
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _: String?, _: SSLSession? -> true }
        } catch (_: Exception) {
        }
    }

    fun checkMD5(tag: String, md5: String?, updateFile: File?): Boolean {
        if (md5.isNullOrBlank() || updateFile == null) {
            Log.e(tag, "MD5 string empty or updateFile null")
            return false
        }

        val calculatedDigest = calculateMD5(tag, updateFile) ?: run {
            Log.e(tag, "calculatedDigest null")
            return false
        }
        return calculatedDigest.equals(md5, ignoreCase = true)
    }

    fun calculateMD5(tag: String, updateFile: File): String? {
        val digest = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            Log.e(tag, "Exception while getting digest", e)
            return null
        }

        val inputStream: InputStream = try {
            FileInputStream(updateFile)
        } catch (e: FileNotFoundException) {
            Log.e(tag, "Exception while getting FileInputStream", e)
            return null
        }

        return try {
            val buffer = ByteArray(8192)
            var read: Int
            while (inputStream.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            val md5sum = digest.digest()
            val bigInt = BigInteger(1, md5sum)
            var output = bigInt.toString(16)
            output = String.format("%32s", output).replace(' ', '0')
            output
        } catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                Log.e(tag, "Exception on closing MD5 input stream", e)
            }
        }
    }

    fun regexIpv4(ip: String?): Boolean {
        if (ip.isNullOrEmpty()) return false
        val pattern = Pattern.compile(
            "^" +
                "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" +
                "|" +
                "localhost" +
                "|" +
                "(([0-9]{1,3}\\.){3})[0-9]{1,3})" +
                ":" +
                "[0-9]{1,5}$",
        )
        return pattern.matcher(ip).matches()
    }

    fun isInstalledPackage(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(0)
        return packages.any { it.packageName.equals(packageName, ignoreCase = true) }
    }

    @Throws(Exception::class)
    fun downloadImage(urlLink: String, file: File): Boolean {
        return downloadImage(urlLink, file, "JPEG")
    }

    @Throws(Exception::class)
    fun downloadImage(urlLink: String, file: File, extension: String): Boolean {
        var result = true
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var out: FileOutputStream? = null
        var format = Bitmap.CompressFormat.JPEG

        try {
            connection = URL(urlLink).openConnection() as HttpURLConnection
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false
            }

            format = when {
                extension.uppercase(Locale.getDefault()).contains("PNG") -> Bitmap.CompressFormat.PNG
                extension.uppercase(Locale.getDefault()).contains("WEBP") -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            input = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            out = FileOutputStream(file)
            bitmap.compress(format, 100, out)
            out.flush()
        } catch (e: IOException) {
            result = false
            e.printStackTrace()
        } finally {
            out?.close()
            input?.close()
            connection?.disconnect()
        }
        return result
    }

    fun downloadLogo(urlLink: String, file: File, extension: String): Boolean {
        var result = true
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var out: FileOutputStream? = null
        var format = Bitmap.CompressFormat.JPEG

        try {
            connection = URL(urlLink).openConnection() as HttpURLConnection
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false
            }

            format = when {
                extension.uppercase(Locale.getDefault()).contains("PNG") -> Bitmap.CompressFormat.PNG
                extension.uppercase(Locale.getDefault()).contains("WEBP") -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            input = connection.inputStream
            var bitmap = BitmapFactory.decodeStream(input)
            val width = bitmap.width
            val height = bitmap.height
            if (width > 512 || height > 512) {
                val newWidth: Int
                val newHeight: Int
                if (width >= height) {
                    newWidth = 512
                    newHeight = newWidth * height / width
                } else {
                    newHeight = 512
                    newWidth = newHeight * width / height
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
            }
            out = FileOutputStream(file)
            bitmap.compress(format, 100, out)
            out.flush()
        } catch (e: IOException) {
            result = false
            e.printStackTrace()
        } finally {
            out?.close()
            input?.close()
            connection?.disconnect()
        }
        return result
    }

    fun getMaxSyncId(): Int {
        return try {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, 1)
            StringUtils.convertDateToString(cal.time, "yyMMddHHmm")?.toInt() ?: 1
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }
    }

    fun getSyncId(): Int {
        return try {
            StringUtils.convertDateToString(Calendar.getInstance().time, "yyMMddHHmm")?.toInt() ?: 1
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }
    }

    fun compareTimeCheckOut(strNextTime: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val nextTime = sdf.parse(strNextTime)
            val currentTime = Calendar.getInstance().time
            nextTime != null && currentTime.before(nextTime)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Throws(Exception::class)
    fun getLine(buf: StringBuffer): String? {
        try {
            val index = buf.indexOf("\n")
            val result: String
            val deleteUntil: Int
            if (index < 0) {
                result = buf.toString()
                deleteUntil = buf.length
                if (result.isEmpty()) {
                    return null
                }
            } else {
                result = buf.substring(0, index)
                deleteUntil = index + 1
            }
            buf.delete(0, deleteUntil)
            return result
        } catch (ex: Exception) {
            throw Exception("Get line: ${ex}")
        }
    }

    fun getLocalIpv4(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->
                intf.inetAddresses.toList().forEach { address ->
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress.orEmpty()
                    }
                }
            }
            ""
        } catch (ex: Exception) {
            Log.e("IP Address", ex.toString())
            ""
        }
    }

    fun compareLanNetwork(ip1: String?, ip2: String?): Boolean {
        if (ip1.isNullOrBlank() || ip2.isNullOrBlank()) return false
        val arrIp1 = ip1.split(".")
        val arrIp2 = ip2.split(".")
        if (arrIp1.size != 4 || arrIp2.size != 4) return false
        val netIp1 = "${arrIp1[0]}.${arrIp1[1]}.${arrIp1[2]}"
        val netIp2 = "${arrIp2[0]}.${arrIp2[1]}.${arrIp2[2]}"
        return netIp1 == netIp2
    }

    fun getLocalIpV6(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->
                intf.inetAddresses.toList().forEach { address ->
                    if (!address.isLoopbackAddress && address is Inet6Address) {
                        return address.hostAddress?.toString().orEmpty()
                    }
                }
            }
            ""
        } catch (ex: Exception) {
            Log.e("IP Address", ex.toString())
            ""
        }
    }

    fun getMacAddress(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in interfaces) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                return macBytes.joinToString(":") { byte -> String.format("%02x", byte) }
            }
            ""
        } catch (_: Exception) {
            ""
        }
    }

    @SuppressLint("MissingPermission")
    fun getImeiNumber(context: Context): String {
        var imeiNumber = ""
        Log.d("THAI", Build.MODEL)
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            imeiNumber = tm?.deviceId.orEmpty()
            if (imeiNumber.isEmpty() || imeiNumber == "0") {
                imeiNumber = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }
        } catch (ex: Exception) {
            Log.e("IMEI", ex.message.orEmpty())
        }
        Log.d("THAI", "OUT -> $imeiNumber")
        return imeiNumber
    }

    fun getSerialNumber(): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)
            get.invoke(systemProperties, "ro.serialno") as? String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getHashMD5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val no = BigInteger(1, messageDigest)
            var hashtext = no.toString(16)
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }
            hashtext
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun jsonObjectToString(jsonObject: JsonObject, fieldName: String): String {
        return jsonObject.get(fieldName).toString().replace("\"", "")
    }

    fun decodeBase64Profile(input: String?): Bitmap? {
        if (input == null) return null
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun getBytesFromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getAvailableInternalMemorySize(): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return formatSize(availableBlocks * blockSize)
    }

    fun getTotalInternalMemorySize(): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)
    }

    fun getAvailableExternalMemorySize(): String {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            formatSize(availableBlocks * blockSize)
        } else {
            ""
        }
    }

    fun getTotalExternalMemorySize(): String {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            formatSize(totalBlocks * blockSize)
        } else {
            ""
        }
    }

    fun formatSize(size: Long): String {
        var value = size.toDouble()
        var suffix: String? = null
        if (value >= 1024) {
            suffix = " KB"
            value /= 1024.0
            if (value >= 1024) {
                suffix = " MB"
                value /= 1024.0
                if (value >= 1024) {
                    suffix = " GB"
                    value /= 1024.0
                }
            }
        }
        val resultBuffer = StringBuilder(value.toLong().toString())
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun getListFile(filePath: String, extendFile: String): Array<File> {
        return try {
            val directory = File(filePath)
            val filter = FilenameFilter { _, name -> name.endsWith(extendFile) }
            directory.listFiles(filter) ?: emptyArray()
        } catch (e: Exception) {
            System.err.println(e.message)
            emptyArray()
        }
    }

    fun saveImageFile(fileSave: File, data: ByteArray) {
        if (fileSave.exists()) {
            fileSave.delete()
        }
        try {
            FileOutputStream(fileSave.path).use { fos ->
                fos.write(data)
            }
        } catch (e: IOException) {
            Log.e("PictureDemo", "Exception in photoCallback", e)
        }
    }

    private const val DEFAULT_BUFFER_SIZE = 1024
}
