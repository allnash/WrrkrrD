import threading
import sys
import os
import platform
import subprocess
import json
import time
import requests
import datetime

import netifaces
import click
from requests import ConnectionError

from analysis import analyze_file
from oui import oui
from colors import *

if os.name != 'nt':
    from pick import pick


def which(program):
    """Determines whether program exists
    """

    def is_exe(fpath):
        return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

    fpath, fname = os.path.split(program)
    if fpath:
        if is_exe(program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            path = path.strip('"')
            exe_file = os.path.join(path, program)
            if is_exe(exe_file):
                return exe_file
    raise Exception("Error in which program.")


def iftttpost(iphones, androids):
    """Posts data to an IFTTT channel to save in Google Sheets"""
    # by Andy Maxwell 6/13/2018 
    report = {}
    report["value1"] = datetime.datetime.now().strftime("%m-%d-%Y %H:%M:%S")
    report["value2"] = iphones
    report["value3"] = androids
    # print(requests.post('https://maker.ifttt.com/trigger/xyz/with/key/khiN5Xs3nUOmx0ZGKrY8t',
    #                    data=report).text)


def localhost_report(apple, androids, others, devicesCount, resolvedDevicesCount):
    """Posts data to localhost server."""
    # By Nash Gadre (github: @allnash)
    unix_time = long(time.time() * 1000)
    report = {"apple_count": apple,
              "android_count": androids,
              "others_count": others,
              "devices_count": devicesCount,
              "resolved_devices_count": resolvedDevicesCount,
              "reader_seen_time": unix_time}
    try:
        requests.post('http://localhost:8000/json/cellphone_sightings_metrics', json=report)
        print("Cellphone probe request data posted")
    except ConnectionError:
        print("Error posting cellphone probe request data")


def localhost_report_real(json):
    """Posts Probe request data to localhost server."""
    # By Nash Gadre (github: @allnash)
    unix_time = long(time.time() * 1000)
    report = {"cellphones": json, "reader_seen_time": unix_time}
    try:
        requests.post('http://localhost:8000/json/cellphone_sightings', json=report)
        print("Cellphone sighting data posted")
    except ConnectionError:
        print("Error posting cellphone sighting data")


def showTimer(timeleft):
    """Shows a countdown timer"""
    total = int(timeleft) * 10
    for i in range(total):
        sys.stdout.write('\r')
        # the exact output you're looking for:
        timeleft_string = '%ds left' % int((total - i + 1) / 10)
        if (total - i + 1) > 600:
            timeleft_string = '%dmin %ds left' % (
                int((total - i + 1) / 600), int((total - i + 1) / 10 % 60))
        sys.stdout.write("[%-50s] %d%% %15s" %
                         ('=' * int(50.5 * i / total), 101 * i / total, timeleft_string))
        sys.stdout.flush()
        time.sleep(0.1)
    print("")


def fileToMacSet(path):
    with open(path, 'r') as f:
        maclist = f.readlines()
    return set([x.strip() for x in maclist])


@click.command()
@click.option('-a', '--adapter', default='', help='adapter to use')
@click.option('-z', '--analyze', default='', help='analyze file')
@click.option('-s', '--scantime', default='60', help='time in seconds to scan')
@click.option('-o', '--out', default='', help='output cellphone data to file')
@click.option('-v', '--verbose', help='verbose mode', is_flag=True)
@click.option('--number', help='just print the number', is_flag=True)
@click.option('-j', '--jsonprint', help='print JSON of cellphone data', is_flag=True)
@click.option('-n', '--nearby', help='only quantify signals that are nearby (rssi > -80) and '
                                     'distinct devices (rssi > -120)', is_flag=True)
@click.option('--allmacaddresses',
              help='do not check MAC addresses against the OUI database to only recognize known cellphone manufacturers',
              is_flag=True)  # noqa
@click.option('--nocorrection', help='do not apply correction', is_flag=True)
@click.option('--loop', help='loop forever', is_flag=True)
@click.option('--port', default=8001, help='port to use when serving analysis')
@click.option('--sort', help='sort cellphone data by distance (rssi)', is_flag=True)
@click.option('--targetmacs', help='read a file that contains target MAC addresses', default='')
def main(adapter, scantime, verbose, number, nearby, jsonprint, out, allmacaddresses, nocorrection, loop, analyze, port,
         sort, targetmacs):
    if analyze != '':
        analyze_file(analyze, port)
        return
    if loop:
        while True:
            adapter = scan(adapter, scantime, verbose, number,
                           nearby, jsonprint, out, allmacaddresses, nocorrection, loop, sort, targetmacs)
    else:
        scan(adapter, scantime, verbose, number,
             nearby, jsonprint, out, allmacaddresses, nocorrection, loop, sort, targetmacs)


def scan(adapter, scantime, verbose, number, nearby, jsonprint, out, allmacaddresses, nocorrection, loop, sort,
         targetmacs):
    """Monitor wifi signals to count the number of people around you"""

    # print("OS: " + os.name)
    # print("Platform: " + platform.system())

    try:
        tshark = which("tshark")
    except:
        if platform.system() != 'Darwin':
            print('tshark not found, install using\n\napt-get install tshark\n')
        else:
            print('wireshark not found, install using: \n\tbrew install wireshark')
            print(
                'you may also need to execute: \n\tbrew cask install wireshark-chmodbpf')
        return

    if jsonprint:
        number = True
    if number:
        verbose = False

    if len(adapter) == 0:
        if os.name == 'nt':
            print('You must specify the adapter with   -a ADAPTER')
            print('Choose from the following: ' +
                  ', '.join(netifaces.interfaces()))
            return
        title = 'Please choose the adapter you want to use: '
        adapter, index = pick(netifaces.interfaces(), title)

    # print("Using %s adapter and scanning for %s seconds..." %
    #      (adapter, scantime))

    if not number:
        # Start timer
        t1 = threading.Thread(target=showTimer, args=(scantime,))
        t1.daemon = True
        t1.start()

    # Scan with tshark
    command = [tshark, '-I', '-i', adapter, '-a',
               'duration:' + scantime, '-w', '/tmp/tshark-temp']
    if verbose:
        print(' '.join(command))
    run_tshark = subprocess.Popen(
        command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    stdout, nothing = run_tshark.communicate()
    if not number:
        t1.join()

    # Read tshark output for chatter coming from cellphones.
    full_spectrum_command = [
        tshark, '-r',
        '/tmp/tshark-temp',
        '-T',
        'fields', '-e',
        'wlan.sa', '-e',
        'wlan.bssid', '-e',
        'radiotap.dbm_antsignal'
    ]

    # Read tshark output for probe requests.
    probe_spectrum_command = [
        tshark, '-r',
        '/tmp/tshark-temp',
        '-Y', 'wlan.fc.type == 0 && wlan.fc.type_subtype == 4',
        '-T',
        'fields', '-e',
        'wlan.sa', '-e',
        'wlan.bssid', '-e',
        'radiotap.dbm_antsignal'
    ]

    # Read tshark output for QoS data Null frames.
    device_resolution_command = [
        tshark, '-r',
        '/tmp/tshark-temp',
        # '-Y', 'wlan.fc.type_subtype == 0x0000 || wlan.fc.type_subtype == 0x000A',
        '-Y',
        'wlan.fc.type_subtype == 44 || wlan.fc.type_subtype == 36',
        '-T',
        'fields', '-e',
        'wlan.sa', '-e',
        'wlan.da', '-e',
        'radiotap.dbm_antsignal'
    ]

    run_tshark_full_spectrum = subprocess.Popen(
        full_spectrum_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output_full_sepctrum, nothing = run_tshark_full_spectrum.communicate()

    run_tshark_probe = subprocess.Popen(
        probe_spectrum_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output_probe, nothing = run_tshark_probe.communicate()

    run_tshark_device_resolution = subprocess.Popen(
        device_resolution_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output_resolution, nothing = run_tshark_device_resolution.communicate()

    # read target MAC address
    targetmacset = set()
    if targetmacs != '':
        targetmacset = fileToMacSet(targetmacs)

    foundFullSpectrumMacs = {}
    foundProbeMacs = {}
    foundRealDeviceMacs = {}

    for line in output_full_sepctrum.decode('utf-8').split('\n'):
        if verbose:
            print(line)
        if line.strip() == '':
            continue
        mac = line.split()[0].strip().split(',')[0]
        dats = line.split()
        if len(dats) == 3:
            if ':' not in dats[0] or len(dats) != 3:
                continue
            if mac not in foundFullSpectrumMacs:
                foundFullSpectrumMacs[mac] = []
            dats_2_split = dats[2].split(',')
            if len(dats_2_split) > 1:
                rssi = float(dats_2_split[0]) / 2 + float(dats_2_split[1]) / 2
            else:
                rssi = float(dats_2_split[0])
            foundFullSpectrumMacs[mac].append(rssi)

    for line in output_probe.decode('utf-8').split('\n'):
        if verbose:
            print(line)
        if line.strip() == '':
            continue
        mac = line.split()[0].strip().split(',')[0]
        dats = line.split()
        if len(dats) == 3:
            if ':' not in dats[0] or len(dats) != 3:
                continue
            if mac not in foundProbeMacs:
                foundProbeMacs[mac] = []
            dats_2_split = dats[2].split(',')
            if len(dats_2_split) > 1:
                rssi = float(dats_2_split[0]) / 2 + float(dats_2_split[1]) / 2
            else:
                rssi = float(dats_2_split[0])
            foundProbeMacs[mac].append(rssi)

    for line in output_resolution.decode('utf-8').split('\n'):
        if verbose:
            print(line)
        if line.strip() == '':
            continue
        mac = line.split()[0].strip().split(',')[0]
        dats = line.split("\t")
        if len(dats) == 3 and len(dats[1]) > 0:
            if ':' not in dats[0] or len(dats) != 3:
                continue
            if mac not in foundRealDeviceMacs:
                foundRealDeviceMacs[mac] = []
            dats_2_split = dats[2].split(',')
            if len(dats_2_split) > 1:
                rssi = float(dats_2_split[0]) / 2 + float(dats_2_split[1]) / 2
            else:
                try:
                    rssi = float(dats_2_split[0])
                except ValueError:
                    rssi = 0
            foundRealDeviceMacs[mac].append(rssi)

    if not foundFullSpectrumMacs:
        print("Found no macs, check the 'tshark' command and make sure Wifi card: %s supports monitor mode." % adapter)
        return

    if not foundRealDeviceMacs:
        print("No real macs were found. Ignoring.")

    for key, value in foundFullSpectrumMacs.items():
        foundFullSpectrumMacs[key] = float(sum(value)) / float(len(value))

    for key, value in foundRealDeviceMacs.items():
        foundRealDeviceMacs[key] = float(sum(value)) / float(len(value))

    for key, value in foundProbeMacs.items():
        foundProbeMacs[key] = float(sum(value)) / float(len(value))

    # Find target MAC address in foundMacs
    if targetmacset:
        sys.stdout.write(RED)
        for mac in foundFullSpectrumMacs:
            if mac in targetmacset:
                print("Found MAC address: %s" % mac)
                print("rssi: %s" % str(foundFullSpectrumMacs[mac]))
        sys.stdout.write(RESET)

    cellphone = [
        'Motorola Mobility LLC, a Lenovo Company',
        'GUANGDONG OPPO MOBILE TELECOMMUNICATIONS CORP.,LTD',
        'Huawei Symantec Technologies Co.,Ltd.',
        'Microsoft',
        'HTC Corporation',
        'Samsung Electronics Co.,Ltd',
        'SAMSUNG ELECTRO-MECHANICS(THAILAND)',
        'BlackBerry RTS',
        'LG ELECTRONICS INC',
        'Murata Manufacturing Co., Ltd.',
        'Nokia Corporation',
        'Apple, Inc.',
        'BLU Products Inc.',
        'vivo Mobile Communication Co., Ltd.',
        'Alcatel-Lucent Shanghai Bell Co., Ltd',
        'BlackBerry RTS',
        'LG Electronics',
        'OnePlus Tech (Shenzhen) Ltd',
        'OnePlus Technology (Shenzhen) Co., Ltd',
        'LG Electronics (Mobile Communications)',
        'OnePlus Tech (Shenzhen) Ltd',
        'Xiaomi Communications Co Ltd',
        'LG Electronics (Mobile Communications)',
        'Google, Inc.',
        'zte corporation',
        'Sony Corporation',
        'Sony Mobile Communications AB',
        'GUANGDONG OPPO MOBILE TELECOMMUNICATIONS CORP.,LTD',
        'Gionee Communication Equipment Co.,Ltd.',
        'Lenovo Mobile Communication Technology Ltd.'
        'Xiaomi Communications Co Ltd'
        'HUAWEI TECHNOLOGIES CO.,LTD']

    cellphone_people = []
    cellphone_macs = []
    cellphone_probe_macs = []
    androids = 0
    iphones = 0
    others = 0

    for mac in foundFullSpectrumMacs:
        oui_id = 'Not in OUI'
        if mac[:8] in oui:
            oui_id = oui[mac[:8]]
        if verbose:
            print(mac, oui_id, oui_id in cellphone)
        if allmacaddresses or oui_id in cellphone:
            if not nearby or (nearby and foundFullSpectrumMacs[mac] > 80):
                cellphone_people.append(
                    {'company': oui_id, 'rssi': foundFullSpectrumMacs[mac], 'mac': mac})
                if oui_id == 'Apple, Inc.':
                    iphones += 1
                elif oui_id in cellphone:
                    androids += 1
                elif oui_id != 'Not in OUI':
                    others += 1
                else:
                    ""

    for mac in foundProbeMacs:
        oui_id = 'Not in OUI'
        if mac[:8] in oui:
            oui_id = oui[mac[:8]]
        if verbose:
            print(mac, oui_id, oui_id in cellphone)
        if not nearby or (nearby and foundProbeMacs[mac] > -80):
            cellphone_probe_macs.append(
                {'company': oui_id, 'rssi': foundProbeMacs[mac], 'mac': mac})

    for mac in foundRealDeviceMacs:
        oui_id = 'Not in OUI'
        if mac[:8] in oui:
            oui_id = oui[mac[:8]]
        if verbose:
            print(mac, oui_id, oui_id in cellphone)
        if not nearby or (nearby and foundRealDeviceMacs[mac] > -120):
            cellphone_macs.append(
                {'company': oui_id, 'rssi': foundRealDeviceMacs[mac], 'mac': mac})

    if sort:
        cellphone_people.sort(key=lambda x: x['rssi'], reverse=True)
    if verbose:
        print(json.dumps(cellphone_people, indent=2))

    # US / Canada: https://twitter.com/conradhackett/status/701798230619590656
    percentage_of_people_with_phones = 0.7
    if nocorrection:
        percentage_of_people_with_phones = 1

    num_devices = int(round(len(cellphone_probe_macs) / percentage_of_people_with_phones))
    num_resolved_devices = int(round(len(cellphone_macs)))

    if number and not jsonprint:
        print("Total: {}".format(num_devices))
        print("iPhones: {}  Androids: {}".format(iphones, androids))
        # print(cellphone_people)
        # adding IFTTT post
        # iftttpost(iphones, androids)
    elif jsonprint:
        # print(json.dumps(cellphone_people, indent=2))
        localhost_report(iphones, androids, others, num_devices, num_resolved_devices)
        localhost_report_real(cellphone_macs)
    else:
        if num_devices == 0:
            print("No one around (not even you!).")
        elif num_devices == 1:
            print("No one around, but you.")
        else:
            print("There are about %d people around." % num_devices)

    if out:
        with open(out, 'a') as f:
            data_dump = {'cellphones': cellphone_people, 'time': time.time()}
            f.write(json.dumps(data_dump) + "\n")
        if verbose:
            print("Wrote %d records to %s" % (len(cellphone_people), out))
    os.remove('/tmp/tshark-temp')
    return adapter


if __name__ == '__main__':
    main()
    # oui = {}
    # with open('data/oui.txt','r') as f:
    #     for line in f:
    #         if '(hex)' in line:
    #             data = line.split('(hex)')
    #             key = data[0].replace('-',':').strip()
    #             company = data[1].strip()
    #             oui[key] = company
    # with open('oui.json','w') as f:
    #     f.write(json.dumps(oui,indent=2))
