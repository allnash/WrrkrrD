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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import utils.Utils;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config extends BaseModel {

    @JsonProperty("device_name")
    public String deviceName;

    @JsonProperty("device_mac_address")
    public String deviceMacAddress;

    @Transient
    @JsonProperty("device_external_id")
    public String deviceExternalId;

    @Transient
    @JsonProperty("device_version_number")
    public String deviceVersionNumber;

    @Transient
    @JsonProperty("device_type_name")
    public String deviceTypeName;

    @Transient
    @JsonProperty("device_product_id")
    public String deviceProductId;

    @Transient
    @JsonProperty("device_product_release_id")
    public String deviceProductReleaseId;

    @OneToOne
    public Device device;
    @OneToOne
    public Place place;
    @OneToOne
    public Organization organization;

	public Config(String deviceName, String deviceMacAddress) {
	    this.deviceName = deviceName;
	    this.deviceMacAddress = deviceMacAddress;
	}

	public static Finder<Long, Config> find = new Finder<>(Config.class);

	public static Config get() {
		return find.query().where().findOne();
	}

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void setDeviceMacAddress(String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    // Transient
    public String getDeviceExternalId() {
        return Utils.getDeviceExternalId();
    }

    public String getDeviceVersionNumber() {
        return Utils.getDeviceVersionNumber();
    }

    public String getDeviceTypeName() {
        return Utils.getDeviceTypeName();
    }

    public String getDeviceProductReleaseId() {
        return Utils.getDeviceProductReleaseId();
    }

    public String getDeviceProductId() {
        return  Utils.getDeviceProductId();
    }
}
