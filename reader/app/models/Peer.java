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
import io.ebean.annotation.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Peer extends BaseModel {

    @Index
    @Column(length = 150)
    @JsonProperty("name")
    public String name;
    @JsonProperty("host_address")
    public String hostAddress;
    @JsonProperty("host_name")
    public String hostName;
    @JsonProperty("port")
    public String port;
    @JsonProperty("service_type")
    public String serviceType;

    @OneToOne
    public Device device;

	public Peer(String name, String hostAddress, String hostName, String port, String serviceType) {
	    this.name = name;
        this.hostAddress = hostAddress;
        this.port = port;
        this.hostName = hostName;
	    this.serviceType = serviceType;
	}

	public static Finder<Long, Peer> find = new Finder<>(Peer.class);

	public static Peer findByName(String name) {
		return find.query().where().where().eq("name", name).findOne();
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
