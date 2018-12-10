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

// Set Env variables
// process.env['BLENO_HCI_DEVICE_ID'] = "1";
// process.env['NOBLE_HCI_DEVICE_ID'] = "1";
// process.env['NOBLE_MULTI_ROLE'] = "1";

var MAC_ADDRESS = "";

// GET MAC ADDRESS

var mac = require('getmac');

mac.getMac({iface: 'wlan0'}, function (err, macAddress) {
    if (err) {
        console.log(err)
    }
    MAC_ADDRESS = macAddress
});


process.env['BLENO_DEVICE_NAME'] = MAC_ADDRESS;

var bleno = require('bleno');
var log4js = require('log4js');
log4js.configure({
    appenders: [
        {type: 'console'},
        {
            type: 'file',
            filename: 'logs/scan.csv', category: 'scan',
            maxLogSize: 10 * 1024 * 1024, // = 10Mb
            backup: 10,
            compress: true, // compress the backups
            pattern: "yyyy-MM-dd-hh",
            layout: {
                type: 'messagePassThrough'
            }
        }
    ]
});

var logger = log4js.getLogger('scan');
logger.setLevel('INFO');

// PART 1.
// BLE PERIPHERIAL SERVICE

var BlenoPrimaryService = bleno.PrimaryService;

var EngageCharacteristic = require('./characteristic');

console.log('OmegaTrace - engage');

bleno.on('stateChange', function (state) {
    console.log('OmegaTrace - BLE on -> stateChange: ' + state);

    if (state === 'poweredOn') {
        console.log('OmegaTrace - WLAN0 mac address - ' + MAC_ADDRESS);
        bleno.startAdvertising(MAC_ADDRESS, ['ec00']);
    } else {
        bleno.stopAdvertising();
    }
});

bleno.on('advertisingStart', function (error) {
    console.log('OmegaTrace - BLE on -> advertisingStart: ' + (error ? 'error ' + error : 'success'));

    if (!error) {
        bleno.setServices([
            new BlenoPrimaryService({
                uuid: 'ec00',
                characteristics: [
                    new EngageCharacteristic()
                ]
            })
        ]);
    }
});
