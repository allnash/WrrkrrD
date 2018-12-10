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
import utils.OUI;

import java.util.ArrayList;
import java.util.List;

public class CapturedDevice {

    @JsonProperty("oui_company_name")
    public String ouiCompanyName;
    @JsonProperty("oui_device_type")
    public OUI.OUIDeviceType ouiDeviceType;
    @JsonProperty("mac_address")
    public String macAddress;
    @JsonProperty("packets")
    public List<CapturedPacket> packets;



	public CapturedDevice(String macAddress) {
	    this.ouiCompanyName = OUI.getCompanyNameForOUI(macAddress);
	    this.ouiDeviceType = OUI.getOUIDeviceTypeForOUI(macAddress);
        this.macAddress = macAddress;
        this.packets = new ArrayList<>();
	}

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void addPacket(CapturedPacket p){
        this.packets.add(p);
    }

    public List<CapturedPacket> getPackets() {
        return packets;
    }

    public void setPackets(List<CapturedPacket> packets) {
        this.packets = packets;
    }

    public String getOuiCompanyName() {
        return ouiCompanyName;
    }

    public void setOuiCompanyName(String ouiCompanyName) {
        this.ouiCompanyName = ouiCompanyName;
    }

    public OUI.OUIDeviceType getOuiDeviceType() {
        return ouiDeviceType;
    }

    public void setOuiDeviceType(OUI.OUIDeviceType ouiDeviceType) {
        this.ouiDeviceType = ouiDeviceType;
    }
}
