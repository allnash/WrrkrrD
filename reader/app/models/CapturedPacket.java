// Copyright 2018 OmegaTrace Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License

package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CapturedPacket {

    public enum PacketType{
        BEACON,
        ASSOCIATION_REQUEST,
        REASSOCIATION_REQUEST,
        PROBE_REQUEST,
        PROBE_RESPONSE,
        NULL_DATA,
        RTS,
        CTS,
        DATA,
    }

    @JsonProperty("src_mac")
    public String srcMac;
    @JsonProperty("dest_mac")
    public String destMac;
    @JsonProperty("trans_mac")
    public String transMac;
    @JsonProperty("recv_mac")
    public String recv_mac;
    @JsonProperty("seq_num")
    public int seqNum;
    public int channel;
    public int freqMhz;
    public int rssi;
    public double distance;
    public String BSSID;
    public String SSID;
    public PacketType packetType;
    public String raw;

    public CapturedPacket() {
	}

    public String getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getDestMac() {
        return destMac;
    }

    public void setDestMac(String destMac) {
        this.destMac = destMac;
    }

    public String getTransMac() {
        return transMac;
    }

    public void setTransMac(String transMac) {
        this.transMac = transMac;
    }

    public String getRecv_mac() {
        return recv_mac;
    }

    public void setRecv_mac(String recv_mac) {
        this.recv_mac = recv_mac;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getFreqMhz() {
        return freqMhz;
    }

    public void setFreqMhz(int freqMhz) {
        this.freqMhz = freqMhz;
    }
}
