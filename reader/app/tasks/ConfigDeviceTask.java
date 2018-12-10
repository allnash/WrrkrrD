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

package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import models.Config;
import models.Device;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ConfigDeviceTask {

    /*
     *
     *  Configure your device in a automated fashion using the MAC address as a unique identifier or Device ID.
     *  There is only one Record of MAC, ID or EXTERNAL ID as they are always treated as unique entries.
     *  Obtain the DEVICE info from the server.
     *
     *  This automated task runs every hour and makes sure new information is fetched and updated.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public ConfigDeviceTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(2, TimeUnit.SECONDS), // initialDelay
                Duration.create(20, TimeUnit.SECONDS), // interval
                this::config,
                this.executionContext
        );
    }

    private void config() {
        if (Config.get() == null) {
            Logger.info("Skipping configure task as record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Configure requires registration and then an update.");
            register();
        } else {
            Logger.info("Configure requires device update.");
            update();
        }

    }

    private void register() {
        // POST registration to server.
        try {
            HttpResponse<String> response = null;
            response = Unirest.post(Utils.getEngageURL() + "/app/devices")
                    .header("XAUTHTOKEN", "1234567")
                    .header("Content-Type", "application/json")
                    .body(Json.toJson(Config.get()).toString())
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if (Device.findByMacAddress(Config.get().getDeviceMacAddress()) == null) {
                Device myDevice = Device.cloneFromJson(responseJson.get("device"));
                if (myDevice != null) {
                    Config myConfig = Config.get();
                    myConfig.setDeviceName(myDevice.getName());
                    myConfig.setDevice(myDevice);
                    myConfig.setPlace(myConfig.getPlace());
                    myConfig.save();
                    updateHostname();
                    Logger.info("Configure registration complete.");
                }
            }

        } catch (UnirestException e) {
            Logger.error("Configure registration task had an error" + e.getMessage() + ".");
        } catch (NullPointerException e){
            Logger.error("Configure registration task had an error" + e.getMessage() + ".");
        } catch (Exception e){
            Logger.error("Configure registration task had an error" + e.getMessage() + ".");
        }
    }

    private void update() {
        Device myDevice = Config.get().getDevice();
        // POST registration to server.
        HttpResponse<String> response = null;
        try {
            response = Unirest.get(Utils.getEngageURL() + "/app/devices/" + myDevice.getId())
                    .header("XAUTHTOKEN", "1234567")
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            myDevice = Device.updateFromJson(myDevice, responseJson.get("device"));
            myDevice.save();
            Logger.info("Configure device update complete.");

        } catch (UnirestException e) {
            Logger.error("Configure device update had an error" + e.getMessage() + ".");
        } catch (NullPointerException e){
            Logger.error("Configure device update had an error" + e.getMessage() + ".");
        }
    }

    private void updateHostname() {
        try {

            File home = new File(Utils.getHomeDirectory());
            Device myDevice = Config.get().getDevice();
            // new ProcessExecutor().directory(home)
            //        .command("sudo", "./hostname.sh").executeNoTimeout();
            //new ProcessExecutor().directory(home)
            //        .command("sudo", "forever", "restartall").executeNoTimeout();
        } catch (Exception e) {
            Logger.error("Error setting new hostname");
        }
    }


}