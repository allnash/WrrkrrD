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
import com.google.inject.Inject;
import models.Config;
import org.apache.commons.lang3.SystemUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import play.Logger;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BluetoothTask {

    /*
     *
     *  Runs a Auto update Task to contact the server every minute.
     *   There is an initial 50 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    public static boolean running = true;
    private static HCI bluetoothDevice;

    private enum HCI {
        HCI0,
        HCI1
    }

    @Inject
    public BluetoothTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(5, TimeUnit.SECONDS), // initialDelay
                Duration.create(1, TimeUnit.MINUTES), // interval
                this::startScanner,
                this.executionContext
        );
    }

    private void startScanner() {
        if (Config.get() == null) {
            Logger.info("Skipping bluetooth scanner task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping bluetooth scanner task as Device information record does not exist yet.");
            return;
        }

        try {
            Logger.info("Attempting to run bluetooth scanner task");
            String workingDir = System.getProperty("user.dir");
            File scannerDir = new File(workingDir + Utils.getScannerDirectory());

            String output = new ProcessExecutor().directory(scannerDir)
                    .commandSplit("ps aux")
                    .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                    .readOutput(true).executeNoTimeout().outputUTF8();

            if (output.contains("scan.js")) {
                running = true;
            } else {
                running = false;
            }

            if (!running) {
                // RUN SCANNER SCRIPT AS SUDO.
                Logger.info("Attempting to run scanner script. Please wait.");
                String command = "";
                if (SystemUtils.IS_OS_LINUX) {
                    command = "sudo node scan.js";
                } else {
                    command = "node scan.js";
                }
                new ProcessExecutor().directory(scannerDir)
                        .commandSplit(command)
                        .executeNoTimeout();
                Logger.info("Scanner script started. Please wait.");
            }
        } catch (IOException e) {
            Logger.error("Bluetooth scanner task had an error. Error:" + e.getCause());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}