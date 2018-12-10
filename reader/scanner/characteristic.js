///
/// OmegaTrace Inc. ("COMPANY") CONFIDENTIAL
/// Unpublished Copyright (c) 2017 OmegaTrace Inc., All Rights Reserved.
///
/// NOTICE:  All information contained herein is, and remains the property of COMPANY.
/// The intellectual and technical concepts contained herein are proprietary to COMPANY and may be covered by U.S. and
/// Foreign Patents, patents in process, and are protected by trade secret or copyright law.
/// Dissemination of this information or reproduction of this material is strictly forbidden unless prior written
/// permission is obtained from COMPANY. Access to the source code contained herein is hereby forbidden to anyone except
/// current COMPANY employees, managers or contractors who have executed Confidentiality and Non-disclosure agreements
/// explicitly covering such access.
///
/// The copyright notice above does not evidence any actual or intended publication or disclosure of this source code,
/// which includes information that is confidential and/or proprietary, and is a trade secret, of  COMPANY.
/// ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS
/// SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
/// LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT
/// CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL
/// ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.


var WRRKRR_VERSION = '0.1';

var util = require('util');
var PropertiesReader = require('properties-reader');
var properties = PropertiesReader();
var execSync = require('child_process').execSync;
var bleno = require('bleno');
var BlenoCharacteristic = bleno.Characteristic;

// IMPORTANT FILE PATHS
var omegaTraceWifiInterfaceTemplateFile = '/home/pi/wrrkrr-reader-' + WRRKRR_VERSION + '/scanner/wpa_supplicant.conf.example';
var omegaTraceWifiInterfaceFile = '/home/pi/wrrkrr-reader-' + WRRKRR_VERSION + '/scanner/wpa_supplicant.conf';
var systemWifiInterfaceFile = '/etc/wpa_supplicant/wpa_supplicant.conf';

var EngageCharacteristic = function () {
    EngageCharacteristic.super_.call(this, {
        uuid: 'ec0e',
        properties: ['read', 'write', 'notify'],
        value: null
    });

    this._value = new Buffer(JSON.stringify({"last_command": "clear", "last_command_result": ""}));
    this._command = new Buffer("clear");
    this._updateValueCallback = null;
};

util.inherits(EngageCharacteristic, BlenoCharacteristic);

EngageCharacteristic.prototype.onReadRequest = function (offset, callback) {
    console.log('OmegaTrace - onReadRequest: value = ' + this._value);
    callback(this.RESULT_SUCCESS, this._value);
};

EngageCharacteristic.prototype.onWriteRequest = function (data, offset, withoutResponse, callback) {

    this._command = JSON.parse(data);

    var response = {};
    console.log('OmegaTrace - onWriteRequest: Received Command = ' + this._command);

    if (this._command['command'] == "wifi_status") {
        // Get the Status of the Wifi on Raspberry Pi
        // executes `sudo wpa_cli status`
        // http://nodejs.org/api.html#_child_processes
        wifiStatusProps = properties.read(execSync('sudo wpa_cli -i wlan0 status'));
        response['last_command'] = this._command["command"];
        response['last_command_result'] = {
            "wpa_state": wifiStatusProps.get('wpa_state'),
            "ssid": wifiStatusProps.get('ssid'),
            "freq": wifiStatusProps.get('freq'),
            "ip_address": wifiStatusProps.get('ip_address'),
            "mac": wifiStatusProps.get('address')
        };
        this._value = new Buffer(JSON.stringify(response));
    } else if (this._command['command'] == "wifi_setup") {
        // Set up Wifi on Raspberry Pi
        if (this._command.hasOwnProperty('wpa_ssid') && this._command.hasOwnProperty('wpa_psk')) {
            createWifiInterfaceFile(this._command['wpa_ssid'], this._command['wpa_psk']);
            copyWifiInterfaceFileToSystem();
            rebootSystem();
            response['last_command'] = this._command["command"];
            response['last_command_result'] = {
                "wifi_setup": "SUCCESS",
                "system": "reboot in 10 seconds",
            };
            this._value = new Buffer(JSON.stringify(response));
        } else {
            response['last_command'] = this._command["command"];
            response['last_command_result'] = {
                "wifi_setup": "FAIL",
                "system": "missing wpa_ssid or wpa_psk",
            };
            this._value = new Buffer(JSON.stringify(response));
        }
    } else if (this._command['command'] == "clear") {
        response['last_command'] = this._command["command"];
        response['last_command_result'] = "";
        this._value = new Buffer(JSON.stringify(response));
    } else {
        response['last_command'] = this._command["command"];
        response['last_command_result'] = "invalid_command";
        this._value = new Buffer(JSON.stringify(response));
    }

    if (this._updateValueCallback) {
        console.log('OmegaTrace - onWriteRequest: notifying');
        this._updateValueCallback(this._value);
    }

    callback(this.RESULT_SUCCESS);
};

EngageCharacteristic.prototype.onSubscribe = function (maxValueSize, updateValueCallback) {
    console.log('OmegaTrace - onSubscribe');

    this._updateValueCallback = updateValueCallback;
};

EngageCharacteristic.prototype.onUnsubscribe = function () {
    console.log('OmegaTrace - onUnsubscribe');

    this._updateValueCallback = null;
};

function createWifiInterfaceFile(wpaSsid, wpaPsk) {

    var fs = require('fs');

    console.log("OmegaTrace - Creating Wifi interfaces file");
    var data = fs.readFileSync(omegaTraceWifiInterfaceTemplateFile).toString();
    data = data.replace(/<WPA_SSID>/g, wpaSsid);
    data = data.replace(/<WPA_PSK>/g, wpaPsk);

    fs.writeFileSync(omegaTraceWifiInterfaceFile, data, 'utf8', function (err) {
        if (err) {
            return console.log("OmegaTrace (error) - " + err);
        }
    });

    // Check if file was created and has the correct SSID/PSK
    if (fs.existsSync(omegaTraceWifiInterfaceFile)) {
        console.log("OmegaTrace - Wifi interfaces file Created");
        var data = fs.readFileSync(omegaTraceWifiInterfaceFile).toString();

        console.log("OmegaTrace - Verifying Wifi interfaces file");
        // Check for SSID in file
        if (data.indexOf(wpaSsid) > -1) {
            console.log("OmegaTrace - New WPA_SSID present in Wifi interfaces file");
        } else {
            console.log("OmegaTrace - Missing WPA_SSID in Wifi interfaces file");
        }
        // Check for PSK in file
        if (data.indexOf(wpaPsk) > -1) {
            console.log("OmegaTrace - New WPA_PSK present in Wifi interfaces file");
        } else {
            console.log("OmegaTrace - Missing WPA_PSK in Wifi interfaces file");
        }

    } else {
        console.log("OmegaTrace - Missing Wifi interfaces file");
    }
}

function copyWifiInterfaceFileToSystem() {
    console.log("OmegaTrace - Copying new Wifi interfaces file");
    execSync('sudo cp ' + omegaTraceWifiInterfaceFile + ' ' + systemWifiInterfaceFile);
}

function rebootSystem(inSeconds) {
    if (typeof inSeconds !== 'number') {
        inSeconds = 3;
    }
    console.log("OmegaTrace - System Rebooting in 3 Seconds");
    setTimeout(function () {
        // this code will only run when time has ellapsed
        execSync('sudo reboot')
    }, inSeconds * 1000);
}

module.exports = EngageCharacteristic;
