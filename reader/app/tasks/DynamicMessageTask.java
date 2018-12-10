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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import models.Config;
import models.Device;
import models.ReaderSighting;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.OmegaWebView;
import utils.Utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DynamicMessageTask {

    /*
     *
     *  Runs a Dynamic message Task to contact the server every minute.
     *   There is an initial 30 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public DynamicMessageTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(1, TimeUnit.MINUTES), // initialDelay
                Duration.create(60, TimeUnit.MINUTES), // interval
                this::showMessage,
                this.executionContext
        );
    }

    private void showMessage() {
        Logger.info("attempting to run Dynamic Message task");
        if (Config.get() == null) {
            Logger.info("Skipping Dynamic Message task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping Dynamic Message task as Device information record does not exist yet.");
            return;
        }

        ////////////////////////////////////////
        // RUN DYNAMIC DISPLAY MESSAGING.     //
        ////////////////////////////////////////
        try {
            // Set dynamic content here.
            String[] args = new String[0];
            OmegaWebView.main(args);
        } catch (Exception e){
            Logger.error("Unable to run dynamic display.");
            e.printStackTrace();
        }
    }
}