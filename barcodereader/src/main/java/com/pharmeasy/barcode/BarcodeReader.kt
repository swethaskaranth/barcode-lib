package com.pharmeasy.barcode

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.pharmeasy.barcode.broadcastreceivers.NewlandBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ProfileBroadCast
import com.pharmeasy.barcode.broadcastreceivers.UrovoBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ZebraBroadcast
import com.pharmeasy.barcode.interfaces.Scanner
import com.pharmeasy.barcode.scanners.NewlandScanner
import com.pharmeasy.barcode.scanners.UrovoScanner
import com.pharmeasy.barcode.scanners.ZebraScanner
import com.pharmeasy.barcode.scanners.bluetoothScanner.DevicesActivity
import com.pharmeasy.barcode.scanners.bluetoothScanner.ScannerActionListener
import com.pharmeasy.barcode.scanners.bluetoothScanner.ScannerService


class BarcodeReader private constructor(context: Context) : DecoratedBarcodeView.TorchListener/*, ScannerActionListener*/ {

    private val TAG: String = BarcodeReader::class.java.simpleName
    private val mContext = context

    var scanner: Scanner
    var broadcastReceiver: BroadcastReceiver
    var intentFilter: IntentFilter = IntentFilter()
    var profileBroadCast: ProfileBroadCast? = null
    lateinit var urovoScannerManager: ScanManager

    var barcodeView: DecoratedBarcodeView? = null
    var beepManager: BeepManager? = null

    private var lastText: String = ""

    private val isZebraDevice = Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")
    private val isUrovo = Build.MANUFACTURER.contains("Urovo")

    var UIView: Boolean? = false

    private val DELAY = 300 // 3 seconds

    private var lastTimestamp: Long = 0


    // private var mode: String? = null

    // private var etScan: EditText? = null

    companion object : SingletonHolder<BarcodeReader, Context>(::BarcodeReader) {
        val barcodeData = MutableLiveData<Event<String>>()

        var mode: String? = null

        var lastMode : String? = null

        var modeListener : ModeSelectedListener? = null

        var actionListener = object : ScannerActionListener{
            override fun onConnected() {
                modeListener?.onModeSelected(ScannerType.BLUETOOTH_SCANNER.displayName)
                lastMode = mode
            }

            override fun onConnecting() {

            }

            override fun onData(barcode: String) {
                barcodeData.value = Event(barcode)
            }

            override fun onDisconnected() {

            }
        }


        fun clearMode() {
            mode = lastMode
            modeListener?.onModeSelected(lastMode?:ScannerType.CAMERA_SCANNER.displayName)
            ScannerService.deregister(actionListener)
        }
    }


    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText || System.currentTimeMillis() - lastTimestamp < DELAY ) {
                // Prevent duplicate scans
                return
            }

            //etScan?.setText(result.text)
            lastTimestamp = System.currentTimeMillis()
            lastText = result.text
            barcodeView?.setStatusText(result.text)
            beepManager?.playBeepSoundAndVibrate()
            BarcodeReader.barcodeData.value = Event(result.text)

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    init {
        Log.d("code", "code here")


        when {
            isZebraDevice -> {
                createProfileForZebraDevice()
                createBarCodeProfileForZebraDevice()
                configureOutputMethodForZebraDevice()
                switchToProfile(VALUE_PROFILE_NAME)
                scanner = ZebraScanner()
                broadcastReceiver = ZebraBroadcast()
                profileBroadCast = ProfileBroadCast()
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                intentFilter.addAction(context.getString(R.string.activity_intent_filter_action))
                UIView = true
            }
            isUrovo -> {
                scanner = UrovoScanner()
                broadcastReceiver = UrovoBroadCast()
                urovoScannerManager = ScanManager()
                initialiseUrovo()
                val action = urovoScannerManager.getParameterString(UROVO_ID_BUF)
                Log.d(TAG, "Action::::${action.get(0)}")
                intentFilter.addAction(action.get(0))
                UIView = true
            }
            else -> {
                Log.d("barcode", "camera")
                scanner = NewlandScanner()
                broadcastReceiver = NewlandBroadCast()
                intentFilter.addAction(context.getString(R.string.newland_scanner_result))

            }
        }

    }

    fun initializeScanner(activity: Activity, intent: Intent, i: Int, editText: EditText?, listener: ModeSelectedListener) {

        modeListener = listener
        //  etScan = editText
        if (mode == null) {
            setupZxingScanner(activity, intent, i)
            listener.onModeSelected(ScannerType.CAMERA_SCANNER.displayName)
        } else {
            when (mode) {
                ScannerType.OTG_SCANNER.displayName -> {
                    setupOTGScanner(editText, activity)
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    setupZxingScanner(activity, intent, i)
                }
            }
            listener.onModeSelected(mode!!)
        }
    }

    fun selectScanner(activity: Activity, intent: Intent, i: Int, editText: EditText?, listener: ModeSelectedListener) {
        modeListener = listener
        showAlertDialog(activity, intent, i, editText, listener)
    }



    private fun showAlertDialog(activity: Activity, intent: Intent, i: Int, editText: EditText?, listener: ModeSelectedListener) {
        val items = ScannerType.toStringList().toTypedArray()

        val builder = AlertDialog.Builder(activity)//ERROR ShowDialog cannot be resolved to a type
        builder.setTitle("Choose Scanner")
        builder.setSingleChoiceItems(items, if (mode != null) items.indexOf(mode) else 2) { dialog, item ->
            mode = items[item]
        }


        builder.setPositiveButton("OK") { dialog, id ->

            if (mode == null)
                mode = items[2]
            when (mode) {
                ScannerType.BLUETOOTH_SCANNER.displayName -> {
                    ScannerService.register(actionListener)
                    startDevicesActivity(activity)
                }
                ScannerType.OTG_SCANNER.displayName -> {
                    setupOTGScanner(editText, activity)
                    listener.onModeSelected(mode!!)
                    ScannerService.deregister(actionListener)
                    lastMode = mode
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    setupZxingScanner(activity, intent, i)
                    listener.onModeSelected(mode!!)
                    ScannerService.deregister(actionListener)
                    lastMode = mode
                }
            }


        }

        val alert = builder.create()
        alert.show()
    }

    fun clearMode(){
        mode = null
    }

    fun registerBroadcast(context: Context) {
        context.registerReceiver(broadcastReceiver, intentFilter)
        if (isZebraDevice) {
            context.registerReceiver(
                    profileBroadCast,
                    IntentFilter(context.getString(R.string.datawedge_intent_key_result_action))
            )
        }
    }

    fun unregisterBroadcast(context: Context) {
        context.unregisterReceiver(broadcastReceiver)
        if (isZebraDevice) {
            context.unregisterReceiver(
                    profileBroadCast
            )
        }

    }

    fun enableScanner() {
        this.scanner.enableScanner(mContext)
    }

    fun disableScanner() {
        this.scanner.disableScanner(mContext)
    }

    fun onResume() {
        barcodeView?.resume()
    }

    fun onPause() {
        barcodeView?.pause()
    }

    override fun onTorchOn() {

    }

    override fun onTorchOff() {

    }

    private fun createBarCodeProfileForZebraDevice() {

        // Configuration for barcode reader
        val barcodeBundle = Bundle()
        barcodeBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_BARCODE)
        barcodeBundle.putString(KEY_RESET_CONFIG, VALUE_RESET_CONFIG_TRUE)

        val barcodeBundleParams = Bundle()
        barcodeBundle.putBundle(KEY_PARAM_LIST, barcodeBundleParams)

        val appConfig = Bundle()
        appConfig.putString(KEY_PACKAGE_NAME, mContext.packageName)
        appConfig.putStringArray(KEY_ACTIVITY_LIST, arrayOf("*"))

        val mainBundle = Bundle()
        mainBundle.putString(KEY_PROFILE_NAME, VALUE_PROFILE_NAME)
        // will enable this profile.
        mainBundle.putString(KEY_PROFILE_ENABLED, VALUE_PROFILE_ENABLED)
        // this profile will be updated on top of the existing profile.
        mainBundle.putString(KEY_CONFIG_MODE, VALUE_CONFIG_MODE)
        mainBundle.putBundle(KEY_PLUGIN_CONFIG, barcodeBundle)
        mainBundle.putParcelableArray(KEY_APP_LIST, arrayOf(appConfig))

        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
        intent.putExtra(mContext.getString(R.string.datawedge_api_intent_extra_config), mainBundle)
        intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
        mContext.sendBroadcast(intent)

    }

    /**
     * Configures the output barcode to be sent via broadcast
     */

    private fun configureOutputMethodForZebraDevice() {

        val outputBundleParams = Bundle()
        // Enable the intent output
        outputBundleParams.putString(KEY_INTENT_OUTPUT_ENABLED, VALUE_INTENT_OUTPUT_ENABLED_TRUE)
        // Set the action string to which the broadcast listener listens to get barcode
        outputBundleParams.putString(KEY_INTENT_ACTION, mContext.getString(R.string.activity_intent_filter_action).toString())
        // Deliver via broadcast the barcode which was read.
        outputBundleParams.putString(KEY_INTENT_DELIVERY, VALUE_INTENT_DELIVERY_BROADCAST)

        val outputBundle = Bundle()
        outputBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_INTENT)
        outputBundle.putString(KEY_RESET_CONFIG, VALUE_RESET_CONFIG_TRUE)
        outputBundle.putBundle(KEY_PARAM_LIST, outputBundleParams)


        val mainBundle = Bundle()
        mainBundle.putString(KEY_PROFILE_NAME, VALUE_PROFILE_NAME)
        // will enable this profile.
        mainBundle.putString(KEY_PROFILE_ENABLED, VALUE_PROFILE_ENABLED)
        // this profile will be updated on top of the existing profile.
        mainBundle.putString(KEY_CONFIG_MODE, VALUE_CONFIG_MODE)
        mainBundle.putBundle(KEY_PLUGIN_CONFIG, outputBundle)

        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
        intent.putExtra(mContext.getString(R.string.datawedge_api_intent_extra_config), mainBundle)
        intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
        mContext.sendBroadcast(intent)

    }

    /**
     * Create a profile with a profile name VALUE_PROFILE_NAME
     */
    private fun createProfileForZebraDevice() {
        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
        intent.putExtra(mContext.getString(R.string.datawedge_intent_key_create_profile), VALUE_PROFILE_NAME)
        intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
        intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
        mContext.sendBroadcast(intent)
    }

    /**
     * Switch to the profile with the name @param profileName
     */

    private fun switchToProfile(profileName: String) {
        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_intent_key_switch_profile))
        intent.putExtra(mContext.getString(R.string.datawedge_intent_key_extra_profilename), profileName)
        intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
        intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
        mContext.sendBroadcast(intent)
    }

    private fun initialiseUrovo() {
        val idmode = urovoScannerManager?.getParameterInts(UROVO_IDMODE_BUF)
        UROVO_ACTION_BUF = urovoScannerManager?.getParameterString(UROVO_ID_BUF) as Array<String>
        //ZERO IS SET FOR INTENT MODE OUTPUT
        idmode?.set(0, 0)
        urovoScannerManager?.setParameterInts(UROVO_IDMODE_BUF, idmode)
    }

    private fun startDevicesActivity(activity: Activity) {
        activity.startActivity(Intent(mContext, DevicesActivity::class.java))
    }

  /*  override fun onConnected() {

    }

    override fun onConnecting() {

    }

    override fun onData(barcode: String) {
        barcodeData.value = Event(barcode)
    }

    override fun onDisconnected() {

    }*/

    private fun setupOTGScanner(editText: EditText?, activity: Activity) {
        val handler = Handler()

        val task = Runnable {
            barcodeData.value = Event(editText?.text.toString().trim())
            editText?.text?.clear()
            editText?.requestFocus()
        }

        editText?.requestFocus()

        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(editText?.windowToken, 0)

        editText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val r = imm?.hideSoftInputFromWindow(editText.windowToken, 0)
                handler.removeCallbacks(task)
            }

            override fun afterTextChanged(s: Editable?) {
                //  val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                val res = imm?.hideSoftInputFromWindow(editText.windowToken, 0)
                if (s != null && s.isNotEmpty()) {
                    handler.postDelayed(task, 500)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //   val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                val res = imm?.hideSoftInputFromWindow(editText.windowToken, 0)
            }
        })

      /*  editText?.setOnKeyListener { v, keyCode, event ->
            Log.d("KEYCODE","Keycode $keyCode")
            if(keyCode == 66)
                setupOTGScanner(editText,activity)
           // editText.requestFocus()
            false
        }*/



       /* editText?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            editText?.requestFocus()
            Toast.makeText(activity,"Focus changed "+hasFocus,Toast.LENGTH_SHORT).show()
            val imm1 = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm1.hideSoftInputFromWindow(editText?.windowToken, 0)
        }*/
    }

    fun focusScanner(editText: EditText?) {
        if (mode == ScannerType.OTG_SCANNER.displayName) {
            editText?.requestFocus()
           // Toast.makeText(mContext,"Focus changed manually",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupZxingScanner(activity: Activity, intent: Intent, i: Int) {
        barcodeView = activity.findViewById(i)
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView?.barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView?.initializeFromIntent(intent)
        barcodeView?.decodeContinuous(callback)

        barcodeView?.setTorchListener(this@BarcodeReader)
        beepManager = BeepManager(activity)

        lastText = ""

        onResume()
    }

}