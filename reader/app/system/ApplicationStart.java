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

package system;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import models.DataMigration;
import play.Environment;
import play.Logger;
import sun.rmi.runtime.Log;
import utils.OmegaWebView;

/**
 * Created by ngadre
 */
@Singleton
public class ApplicationStart {

    @Inject
    public ApplicationStart(Environment environment) {

        Logger.info("Application has started");
        //////////////////////////////
        // Load TYPE CACHED OBJECTS //
        //////////////////////////////

        // TODO: unused for now.
        Config conf = ConfigFactory.load();

        ////////////////////////////////////////
        // RUN ALL DATA MIGRATIONS AFTER THIS //
        ////////////////////////////////////////
        DataMigration.run_data_mirations();
    }
}