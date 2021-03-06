package com.pharmeasy.barcode

import android.device.ScanManager
import android.device.scanner.configuration.PropertyID

const val NEWLAND_SCANNER_ACTION="ACTION_BAR_SCANCFG"
const val NEWLAND_SCANNER_DATA="EXTRA_SCAN_POWER"

const val KEY_PLUGIN_NAME="PLUGIN_NAME"
const val VALUE_PLUGIN_NAME_INTENT="INTENT"
const val VALUE_PLUGIN_NAME_BARCODE="BARCODE"

const val KEY_PACKAGE_NAME="PACKAGE_NAME"
const val KEY_ACTIVITY_LIST="ACTIVITY_LIST"
const val KEY_APP_LIST="APP_LIST"

const val KEY_SEND_RESULT="SEND_RESULT"
const val VALUE_SEND_RESULT_TRUE="true"

const val KEY_COMMAND_IDENTIFIER="COMMAND_IDENTIFIER"
const val VALUE_COMMAND_IDENTIFIER="pharmeasy"

const val KEY_RESET_CONFIG="RESET_CONFIG"
const val VALUE_RESET_CONFIG_TRUE="true"

const val KEY_INTENT_OUTPUT_ENABLED="intent_output_enabled"
const val VALUE_INTENT_OUTPUT_ENABLED_TRUE="true"

const val KEY_INTENT_ACTION="intent_action"

const val KEY_INTENT_DELIVERY="intent_delivery"
const val VALUE_INTENT_DELIVERY_BROADCAST="2"

const val KEY_SCANNER_SELECTION="scanner_selection"
const val VALUE_SCANNER_SELECTION_AUTO="auto"

const val KEY_SCANNER_INPUT_ENABLED="scanner_input_enabled"
const val VALUE_SCANNER_INPUT_ENABLED_TRUE="true"

const val KEY_PROFILE_NAME="PROFILE_NAME"
const val VALUE_PROFILE_NAME="Pharmeasy"

const val KEY_PROFILE_ENABLED="PROFILE_ENABLED"
const val VALUE_PROFILE_ENABLED="true"

const val KEY_CONFIG_MODE="CONFIG_MODE"
const val VALUE_CONFIG_MODE="UPDATE"

const val KEY_PLUGIN_CONFIG="PLUGIN_CONFIG"

const val ZEBRA_SCANNER_EXTRA_VALUE_ENABLE_PLUGIN="ENABLE_PLUGIN"
const val ZEBRA_SCANNER_EXTRA_VALUE_DISABLE_PLUGIN="DISABLE_PLUGIN"


const val KEY_PARAM_LIST="PARAM_LIST"

var UROVO_ACTION_BUF = arrayOf(ScanManager.ACTION_DECODE,ScanManager.BARCODE_STRING_TAG)
val UROVO_ID_BUF = intArrayOf(PropertyID.WEDGE_INTENT_ACTION_NAME,PropertyID.WEDGE_INTENT_DATA_STRING_TAG)
val UROVO_IDMODE_BUF= intArrayOf(PropertyID.WEDGE_KEYBOARD_ENABLE, PropertyID.TRIGGERING_MODES, PropertyID.LABEL_APPEND_ENTER)

