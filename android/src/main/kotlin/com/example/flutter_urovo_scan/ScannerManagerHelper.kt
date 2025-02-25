package com.example.flutter_urovo_scan

import android.content.Context
import android.content.IntentFilter
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Triggering
import android.util.Log
import io.flutter.plugin.common.MethodChannel


class ScannerManagerHelper(private val context: Context, private val plugin: FlutterUrovoScanPlugin?) {
    private val mScanManager: ScanManager = ScanManager()
    private val scannerUrovoReceiver = ScannerUrovoReceiver(plugin)

    fun getScannerInstance(): ScanManager {
        return mScanManager
    }

    fun getScannerState(result: MethodChannel.Result) {
        try {
            val powerOn = mScanManager.scannerState
            result.success(powerOn)
        } catch (e: Exception) {
            result.error("ERROR", "getScannerState Error Occurred: ${e.toString()}", null)
        }
    }

    fun openScanner(result: MethodChannel.Result) {
        try {
            // mScanManager = ScanManager()
            val powerOn = mScanManager.scannerState
            if (!powerOn) {
                val ret: Boolean = mScanManager.openScanner()
                if (ret) {
                    //open successful
                    result.success(ret)
                } else {
                    result.error("UNAVAILABLE", "Open Scanner failed", null)
                }
            } else {
                result.error("UNAVAILABLE", "Scanner already ON", null)
            }

        } catch (e: Exception) {
            result.error("ERROR", "Open Scanner Error Occurred: ${e.toString()}", null)
        }
    }

    fun closeScanner(result: MethodChannel.Result) {
        try {
            // mScanManager = ScanManager()
            val powerOn = mScanManager.scannerState
            if (powerOn) {
                val ret: Boolean = mScanManager.closeScanner()
                print("RESULT CLOSE: $ret");
                // close successful
                // weird. it always returns false but it successfully turn off na scanner.
                result.success(ret)
                // if (ret) {

                // } else {
                //    result.error("UNAVAILABLE", "Close Scanner failed", null)
                // }

            } else {
                result.error("UNAVAILABLE", "Scanner already OFF", null)
            }


        } catch (e: Exception) {
            result.error("ERROR", "Close Scanner Error Occurred: ${e.toString()}", null)
        }
    }

    fun getOutputMode(result: MethodChannel.Result) {
        try {
            val mode = mScanManager.outputMode
            if (mode == 0) {
                // barcode is sent as intent
                result.success("Barcode is sent as intent")
            } else if (mode == 1) {
                // barcode is sent to the text box in focus
                result.success("Barcode is sent to the text box in focus")
            }
        } catch (e: Exception) {
            result.error("ERROR", "Get Output mode Error Occurred: ${e.toString()}", null)
        }
    }


    fun setOutputMode(mode: Int, result: MethodChannel.Result) {
        try {
            // 0 - barcode is sent as intent
            // 1 - barcode is sent to the text box in focus (default)
            val ret = mScanManager.switchOutputMode(mode)
            if (ret) {
                result.success("Output Mode was set to: $mode")
            }
        } catch (e: Exception) {
            result.error("ERROR", "Set Output mode Error Occurred: ${e.toString()}", null)
        }
    }

    fun getTriggerMode(result: MethodChannel.Result) {
        try {
            val mode = mScanManager.triggerMode
            result.success(mode.toString())
        } catch (e: Exception) {
            result.error("ERROR", "Get Trigger mode Error Occurred: ${e.toString()}", null)
        }
    }

    fun setTriggerMode(mode: String, result: MethodChannel.Result) {
        try {
            var triggerMode = Triggering.HOST
            when (mode) {
                "HOST" -> {
                    triggerMode = Triggering.HOST
                }

                "CONTINUOUS" -> {
                    triggerMode = Triggering.CONTINUOUS
                }

                "PULSE" -> {
                    triggerMode = Triggering.PULSE
                }
            }
            mScanManager.triggerMode = triggerMode
            result.success(triggerMode.toString())
        } catch (e: Exception) {
            result.error("ERROR", "Set Trigger mode Error Occurred: ${e.toString()}", null)
        }
    }

    fun getParameterInts(result: MethodChannel.Result) {
        try {
            // Should scanner be open first?
            val index = intArrayOf(PropertyID.WEDGE_KEYBOARD_ENABLE)
            val value = mScanManager.getParameterInts(index)
            result.success(value.toString())
        } catch (e: Exception) {
            result.error("ERROR", "getParameterInts Error Occurred: ${e.toString()}", null)
        }
    }

    fun startDecode(result: MethodChannel.Result) {
        try {
            val ret = mScanManager.startDecode()
            if (ret) {
                //scanner start decoding
                result.success(ret.toString())
            } else {
                result.success("Start decode: false")
            }

        } catch (e: Exception) {
            result.error("ERROR", "startDecode Error Occurred: ${e.toString()}", null)
        }
    }

    fun stopDecode(result: MethodChannel.Result) {
        try {
            //After calling startDecode, stopDecode is called before timeout to stop scanning
            val ret = mScanManager.stopDecode()
            if (ret) {
                //scanner stop decode successful
                result.success(ret.toString())
            } else {
                //scanner stop decode failed
                result.success("Stop decode failed (ret=false)")
            }
        } catch (e: Exception) {
            result.error("ERROR", "stopDecode Error Occurred: ${e.toString()}", null)
        }
    }

    fun registerReceiver(register: Boolean) {
        Log.d("[TEST]", "registerReceiver 1");
        if (register && mScanManager != null) {
            Log.d("[TEST]", "registerReceiver 2");
            val filter = IntentFilter()
            val idBuf = intArrayOf(
                PropertyID.WEDGE_INTENT_ACTION_NAME,
                PropertyID.WEDGE_INTENT_DATA_STRING_TAG
            )
            val valueBuf = mScanManager.getParameterString(idBuf)
            if (valueBuf != null && valueBuf[0] != null && valueBuf[0] != "") {
                filter.addAction(valueBuf[0])
            } else {
                filter.addAction(Singleton.ACTION_DECODE)
            }
            filter.addAction(Singleton.ACTION_CAPTURE_IMAGE)

            context.registerReceiver(scannerUrovoReceiver, filter)
        } else if (mScanManager != null) {
            Log.d("[TEST]", "registerReceiver 3");
            mScanManager.stopDecode()
            context.unregisterReceiver(scannerUrovoReceiver)
        }
    }
}