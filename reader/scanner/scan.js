///
/// OmegaTrace Inc. ("COMPANY") CONFIDENTIAL
/// Unpublished Copyright (c) 2018 OmegaTrace Inc., All Rights Reserved.
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

var visitType = "BEACON";
var WRRKRR_VERSION = '0.1';
var SCANNING = false;

// Set Env variables
process.env['BLENO_HCI_DEVICE_ID'] = "0";
process.env['NOBLE_HCI_DEVICE_ID'] = "0";
process.env['NOBLE_MULTI_ROLE'] = "1";

var log4js = require('log4js');
var noble = require('noble');
var EddystoneBeaconScanner = require('./lib/eddystone-beacon-scanner');

// GET VARIABLES FROM PROPERTIES
// TODO: MOVE ALL VARIABLES TO PROP FILES.
// TODO: DISABLED FOR NOW
// var properties = PropertiesReader('/home/pi/wrrkrr-reader-' + WRRKRR_VERSION + '/scanner/scan.properties');
// var serviceHost = properties.get('service_host');
// var servicePort = properties.get('service_port');
var serviceHost = 'localhost';
var servicePort = '8000';

// Configure logger.
log4js.configure({
    appenders: [
        {
            type: 'console'
        },
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

var http = require("http");
var options = {
    hostname: serviceHost,
    proto: 'http',
    port: servicePort,
    path: '/json/beacon_sightings',
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    }
};

// PART 2.
// EDDYSTONE BLE BEACON SCANNER

if (!Date.now) {
    Date.now = function () {
        return new Date().getTime();
    }
}

EddystoneBeaconScanner.on('found', function(beacon) {

    if (beacon.hasOwnProperty('last_seen')) {
        // Create these elements dynamically
        beacon['reader_last_seen_time'] = new Date(beacon.last_seen).getTime();
    } else {
        beacon['reader_last_seen_time'] = new Date().getTime();
    }

    if (beacon.hasOwnProperty('url')) {
        // URL is only present when txPower is present
        beacon['sighted_device_id'] = getSecondPart(beacon.url);
        sendSightingData(beacon, 'FOUND');
    }
    console.debug('found card:\n', JSON.stringify(beacon, null, 2));
});

EddystoneBeaconScanner.on('updated', function (beacon) {

    if (beacon.hasOwnProperty('last_seen')) {
        // Create these elements dynamically
        beacon['reader_last_seen_time'] = new Date(beacon.last_seen).getTime();
    } else {
        beacon['reader_last_seen_time'] = new Date().getTime();
    }

    if (beacon.hasOwnProperty('tlm')) {
        // Create these elements dynamically
        beacon['temp'] = beacon.tlm.temp;
    } else {
        beacon['temp'] = null;
    }

    if (beacon.hasOwnProperty('url')) {
        // URL is only present when txPower is present
        beacon['sighted_device_id'] = getSecondPart(beacon.url);
        sendSightingData(beacon, 'UPDATE');
    }
    console.debug('updated card:\n', JSON.stringify(beacon, null, 2));
});

EddystoneBeaconScanner.on('lost', function(beacon) {

    if (beacon.hasOwnProperty('last_seen')) {
        // Create these elements dynamically
        beacon['reader_last_seen_time'] = new Date(beacon.last_seen).getTime();
    } else {
        beacon['reader_last_seen_time'] = new Date().getTime();
    }

    if (beacon.hasOwnProperty('url')) {
        // URL is only present when txPower is present
        beacon['sighted_device_id'] = getSecondPart(beacon.url);
    }
    sendSightingData(beacon, 'LOST');
    console.debug('lost card:\n', JSON.stringify(beacon, null, 2));
});

function sendSightingData(beacon, visitActivity) {
    beacon['visit_type'] = visitType;
    beacon['visit_activity'] = visitActivity;
    try {
        var req = http.request(options, function (res) {
            res.setEncoding('utf8');
            // TODO: Handle better
            /*
            res.on('data', function (body) {

            });
            */

            if (res.statusCode === 200) {
                console.info('OmegaTrace - Reader sighting logged for visit_id - ' + beacon.visit_id);
            }

            if (res.statusCode === 400) {
                //writeToCSV(beacon);
            }

            if (res.statusCode === 404) {
                //writeToCSV(beacon);
            }

        });
        req.on('error', function (e) {
            //writeToCSV(beacon);
        });

        req.on('socket', function (socket) {
            socket.setTimeout(5000);
            socket.on('timeout', function () {
                req.abort();
            });
        });

        req.on("timeout", function () {
            console.error('OmegaTrace - problem with request - timed out.');
        });

        // write data to request body
        req.write(JSON.stringify(beacon));
        req.end();

    }
    catch (err) {
        // Errors are thrown for bad options, or if the data is empty and no fields are provided.
        // Be sure to provide fields if it is possible that your data array will be empty.
        logger.error(err, null);
    }
}

// UNUSED
function writeToCSV(beacon) {
    // log into file
    logger.info(beacon.id + "," +
        beacon.type + "," +
        beacon.url + "," +
        beacon.distance + "," +
        beacon.temp + "," +
        beacon.rssi + "," +
        new Date(beacon.lastSeen).toISOString() + "," +
        new Date(Math.floor(Date.now())).toISOString());
    console.info('OmegaTrace - problem with request, Reader sighting logged to offline CSV.');
}


function getSecondPart(str) {
    return str.split('http://ot.ht/')[1];
}

EddystoneBeaconScanner.startScanning(true);

noble.on("scanStart", function () {
    SCANNING = true;
    console.info("OmegaTrace - Starting OmegaTrace Beacon Scanner");
});

noble.on('scanStop', function () {
    SCANNING = false;
    console.info("OmegaTrace -  Noble Beacon stopped. Please review this event.");
    setInterval(function () {
        console.info("OmegaTrace - Restarting Noble Beacon Scanner");
        if(!SCANNING){
            EddystoneBeaconScanner.startScanning(true);
        }
    }, 5000);
});
