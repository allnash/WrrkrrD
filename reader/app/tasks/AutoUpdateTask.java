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
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import play.Logger;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AutoUpdateTask {

    /*
     *
     *  Runs a Auto update Task to contact the server every minute.
     *   There is an initial 50 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public AutoUpdateTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(1, TimeUnit.MINUTES), // initialDelay
                Duration.create(1, TimeUnit.DAYS), // interval
                this::findAndDownloadUpdate,
                this.executionContext
        );
    }

    private void findAndDownloadUpdate() {
        Logger.info("attempting to run auto update task");
        if (Config.get() == null) {
            Logger.info("Skipping update update task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping update update task as Device information record does not exist yet.");
            return;
        }

        // Fetch Device Entry from configuration.
        Config config = Config.get();
        HttpResponse<String> response;

        try {
            String urlPath = "/app/products/" + config.getDeviceProductId() + "/releases/" + config.getDeviceProductReleaseId() + "/upgrade";
            response = Unirest.get(Utils.getEngageURL() + urlPath).header("XAUTHTOKEN", "1234567").asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if (Objects.requireNonNull(responseJson).has("success")) {

                File home = new File(Utils.getHomeDirectory());
                File updateDir = new File(Utils.getHomeDirectory() + File.separator + "update");
                Logger.info("New Product release posted successfully for product - " + config.getDeviceProductId() + ".");
                String output = new ProcessExecutor().directory(home)
                        .command("rm", "-rf", "update")
                        .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                        .readOutput(true).execute().outputUTF8();

                output = new ProcessExecutor().directory(home)
                        .command("mkdir", "update")
                        .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                        .readOutput(true).execute().outputUTF8();

                Logger.info("Downloading software.");

                String downloadUrl = responseJson.get("product_release").get("download_url").asText();
                output = new ProcessExecutor().directory(updateDir)
                        .command("wget", downloadUrl)
                        .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                        .readOutput(true).execute().outputUTF8();
                Logger.info("Download updated.");
            } else {
                Logger.info("No Product release found.");
            }
        } catch (UnirestException | NullPointerException e) {
            Logger.error("Auto update task had an error. Error:" + e.getCause());
        } catch (IOException e) {
            Logger.error("Auto update task had an error running command. Error:" + e.getCause());
        } catch (Exception e) {
            Logger.error("Auto update task had an error running command. Error:" + e.getCause());
        }
    }
}