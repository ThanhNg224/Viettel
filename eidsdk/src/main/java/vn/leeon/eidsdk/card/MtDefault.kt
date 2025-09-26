package vn.leeon.eidsdk.card

object MtDefault {
    const val DEBUG: Boolean = true
    const val READER_VID: Int = 9176
    const val READER_PID: Int = 36865
    const val CAMERA_PID: Int = 11453
    const val CAMERA_VID: Int = 7119
    const val PREVIEW_WIDTH: Int = 640
    const val PREVIEW_HEIGHT: Int = 480
    const val CAPTURE_CACHE_DIR: String = "/sdcard/DCIM/"
    const val BASE_STORE: String = "/sdcard"
    const val BASE_DIR: String = "/crt900x"
    const val CAPTURE_CACHE_NAME: String = "crt900xcache.jpg"
    const val CHIP_HEAD_CACHE_NAME: String = "head.bmp"
    const val DEVICE_TIMEOUT: Int = 5000
    const val CAPTURE_DELAY: Int = 150
}
