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

import com.google.inject.AbstractModule;
import services.ApplicationTimer;
import services.AtomicCounter;
import services.Counter;
import system.ApplicationStart;
import system.ApplicationStop;
import tasks.*;

import java.time.Clock;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 * <p>
 * Play will automatically use any class called `AppModule` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class AppModule extends AbstractModule {

    public static String domain = "";

    @Override
    public void configure() {
        // Use the system clock as the default implementation of Clock
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        // Ask Guice to create an instance of ApplicationTimer when the
        // application starts.
        bind(ApplicationTimer.class).asEagerSingleton();
        // Set AtomicCounter as the implementation for Counter.
        bind(Counter.class).to(AtomicCounter.class);
        // Set Config Device Task
        bind(ConfigDeviceTask.class).asEagerSingleton();
        // Set Heartbeat Task to send pings every min.
        bind(HeartbeatTask.class).asEagerSingleton();
        // Set JMDNS Task to send pings every min to make sure DNS service is active.
        bind(JMDNSTask.class).asEagerSingleton();
        // Set Auto Update Task to send pings every min software is up to date.
        bind(AutoUpdateTask.class).asEagerSingleton();

        // TODO: Disabling for now.
        // Set Auto Update Task to send pings every min software is up to date.
        // bind(BluetoothTask.class).asEagerSingleton();
        // TODO: Disabling for now.
        // TODO: Currently only single device supported that processes and creates a local VISIT report.
        // TODO: For more than one device during "Cluster" mode we may need to triangulate and send the position to the
        // TODO: server for creating a visit record on the server.
        // Sighting Upload Task to send pings every min software is up to date.
        // bind(SightingUploadTask.class).asEagerSingleton();

        // Reader Visit Report Task to send ReaderVisitReport records
        bind(ReaderVisitReportUploadTask.class).asEagerSingleton();
        // Reader Visits Task to send updated Visit records
        bind(ReaderVisitsUploadTask.class).asEagerSingleton();
        // TShark scanner task.
        bind(TsharkTask.class).asEagerSingleton();
        // Dynamic display task.
        bind(DynamicMessageTask.class).asEagerSingleton();

        // Applications Starts
        bind(ApplicationStart.class).asEagerSingleton();
        bind(ApplicationStop.class).asEagerSingleton();
    }

}
