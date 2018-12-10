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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReaderVisitReport extends BaseTenantModel {

    /**
     * Visit Report Table
     */
    @ManyToOne
    @JsonIgnore
    public Device readerDevice;

    @JsonProperty("reader_device_id")
    @Transient
    public String readerDeviceId;

    @JsonProperty("when_seen")
    public Timestamp whenSeen;

    @JsonProperty("android_count")
    public int androidCount;

    @JsonProperty("apple_count")
    public int appleCount;

    @JsonProperty("laptops_count")
    public int laptopCounts;

    @JsonProperty("networking_devices_count")
    public int networkingDevicesCount;

    @JsonProperty("masked_devices_count")
    public int maskedDevicesCount;

    @JsonProperty("not_in_oui_count")
    public int notInOUICount;

    @JsonProperty("others_count")
    public int othersCount;

    @JsonProperty("devices_count")
    public int devicesCount;

    @JsonProperty("resolved_devices_count")
    public int resolvedDevicesCount;

    public boolean sent;

    @ManyToOne
    public Place place;

    public static Finder<String, ReaderVisitReport> find = new Finder<>(ReaderVisitReport.class);


    public ReaderVisitReport(Device readerDevice, Timestamp whenSeen, int androids, int apples, int others) {
        this.readerDevice = readerDevice;
        this.organization = readerDevice.getOrganization();
        this.whenSeen = whenSeen;
        this.androidCount = androids;
        this.appleCount = apples;
        this.othersCount = others;
        this.sent = false;
    }

    public Device getReaderDevice() {
        return readerDevice;
    }

    public void setReaderDevice(Device readerDevice) {
        this.readerDevice = readerDevice;
    }

    public Timestamp getWhenSeen() {
        return whenSeen;
    }

    public void setWhenSeen(Timestamp whenSeen) {
        this.whenSeen = whenSeen;
    }

    public String getReaderDeviceId() {
        if (this.readerDevice != null) {
            return this.readerDevice.id;
        } else {
            return null;
        }
    }

    public static List<ReaderVisitReport> findAllNotSent() {
        return find.query().where().eq("sent", false).findList();
    }

    public void setReaderDeviceId(String readerDeviceId) {
        this.readerDeviceId = readerDeviceId;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public int getAndroidCount() {
        return androidCount;
    }

    public void setAndroidCount(int androidCount) {
        this.androidCount = androidCount;
    }

    public int getAppleCount() {
        return appleCount;
    }

    public void setAppleCount(int appleCount) {
        this.appleCount = appleCount;
    }

    public int getOthersCount() {
        return othersCount;
    }

    public void setOthersCount(int othersCount) {
        this.othersCount = othersCount;
    }

    public int getDevicesCount() {
        return devicesCount;
    }

    public void setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
    }

    public int getResolvedDevicesCount() {
        return resolvedDevicesCount;
    }

    public void setResolvedDevicesCount(int resolvedDevicesCount) {
        this.resolvedDevicesCount = resolvedDevicesCount;
    }

    public int getLaptopCounts() {
        return laptopCounts;
    }

    public void setLaptopCounts(int laptopCounts) {
        this.laptopCounts = laptopCounts;
    }

    public int getNetworkingDevicesCount() {
        return networkingDevicesCount;
    }

    public void setNetworkingDevicesCount(int networkingDevicesCount) {
        this.networkingDevicesCount = networkingDevicesCount;
    }

    public int getMaskedDevicesCount() {
        return maskedDevicesCount;
    }

    public void setMaskedDevicesCount(int maskedDevicesCount) {
        this.maskedDevicesCount = maskedDevicesCount;
    }

    public int getNotInOUICount() {
        return notInOUICount;
    }

    public void setNotInOUICount(int notInOUICount) {
        this.notInOUICount = notInOUICount;
    }
}
