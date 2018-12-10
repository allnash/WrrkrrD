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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Visit extends BaseModel {

    /**
     * Visit Table
     */

    @JsonIgnore
    @ManyToOne
    public Device endpointDevice;

    @Transient
    @JsonProperty("endpoint_device_id")
    String endpointDeviceId;

    @JsonIgnore
    @ManyToOne
    public Device userDevice;

    @Transient
    @JsonProperty("user_device_id")
    String userDeviceId;

    @JsonIgnore
    @ManyToOne
    public User user;

    @Transient
    @JsonProperty("user_id")
    String userId;

    @JsonIgnore
    @ManyToOne
    public Place at;

    @JsonProperty("when_started")
    Timestamp whenStarted;

    @JsonProperty("when_ended")
    Timestamp whenEnded;

    public boolean sent;
    public boolean processed;

    public Visit() {
    }

    public static Finder<String, Visit> find = new Finder<>(Visit.class);


    public static List<Visit> all(Map parameterMap) {
        return find.query().where().allEq(parameterMap).findList();
    }

    public static List<Visit> findAll() {
        return find.all();
    }

    public static List<Visit> findAllNotSent() {
        return find.query().where().eq("sent", false).findList();
    }

    public static List<Visit> findByUser(User user) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("user_id", user.getId());
        return all(parameterMap);
    }

    public static Visit findById(String id) {
        return find.query().where().eq("id", id).findOne();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserDeviceId() {
        if (this.userDevice != null) {
            return this.userDevice.getId();
        } else {
            return null;
        }
    }

    public String getEndpointDeviceId() {
        if (this.endpointDevice != null) {
            return this.endpointDevice.getId();
        } else {
            return null;
        }
    }

    public String getUserId() {
        if (this.user != null) {
            return this.user.getId();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Place getAt() {
        return at;
    }

    public void setAt(Place at) {
        this.at = at;
    }

    public Device getEndpointDevice() {
        return endpointDevice;
    }

    public void setEndpointDevice(Device endpointDevice) {
        this.endpointDevice = endpointDevice;
    }

    public Device getUserDevice() {
        return userDevice;
    }

    public void setUserDevice(Device userDevice) {
        this.userDevice = userDevice;
    }

    public Timestamp getWhenStarted() {
        return whenStarted;
    }

    public void setWhenStarted(Timestamp whenStarted) {
        this.whenStarted = whenStarted;
    }

    public Timestamp getWhenEnded() {
        return whenEnded;
    }

    public void setWhenEnded(Timestamp whenEnded) {
        this.whenEnded = whenEnded;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /*
    *
    *   Visits Report Record
    *
    * */
    public class VisitReport {

        public String endpoint_device_id;
        public String endpoint_device_name;
        public String user_device_type;
        public String user_owner_id;
        public String user_first_name;
        public String user_last_name;
        public String visit_start_time;
        public String visit_end_time;
        public String visit_lat;
        public String visit_lon;
        public String place_type;
        public double dwell_time;

        public VisitReport() {

        }

        public VisitReport(Visit v) {

        }
    }
}