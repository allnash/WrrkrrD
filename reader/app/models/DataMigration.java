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
// limitations under the License.

package models;

import com.github.javafaker.Faker;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import play.Logger;
import utils.Utils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.net.NetworkInterface;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


@Entity
public class DataMigration extends Model {

    /**
     * Auto Server start Data Migrations Class.
     */
    @Id
    private String id;
    private String name;
    private String description;
    private String metaData;
    private Timestamp startDate;
    private Timestamp completedDate;
    private DataMigrationState migrationState;

    @Version
    Long version;

    @Transient
    History currentHistory;

    @CreatedTimestamp
    Timestamp whenCreated;

    @UpdatedTimestamp
    Timestamp whenUpdated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompletedDate(Timestamp completedDate) {
        this.completedDate = completedDate;
    }

    public DataMigration.DataMigrationState getMigrationState() {
        return migrationState;
    }

    public void setMigrationState(DataMigration.DataMigrationState migrationState) {
        this.migrationState = migrationState;
    }

    public enum DataMigrationState {
        RUNNING, COMPLETE
    }

    public DataMigration() {
    }

    public void save() {
        super.save();
        Logger.info(this.getClass().getCanonicalName() + " - " + this.getName() + " saved at " + Calendar.getInstance().getTime().toString());
    }

    public DataMigration(String id, String name, String description, String meta_data) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.metaData = meta_data;
        this.startDate = Utils.getCurrentTimestamp();
        this.migrationState = DataMigrationState.RUNNING;
    }

    private static Finder<String, DataMigration> find = new Finder<>(DataMigration.class);

    public static List<DataMigration> all() {
        return find.all();
    }

    // STATIC METHODS

    private static DataMigration findById(String migration_number) {
        return find.query().where().eq("id", migration_number).findOne();
    }

    public static void run_data_mirations() {

        // Load some sample data in non prod env
        if (Utils.isDebugMode()) {

        }
        // Production Data migrations
        // <<<<  Include them after this line <<<<
        bootstrap();
    }

    private static boolean present(String migrationNumber) {
        if (DataMigration.findById(migrationNumber) == null)
            return false;
        else
            return true;
    }

    private static void bootstrap() {
        // MANDATORY INIT
        final String MIGRATION_NUMBER = "1524419399";
        if (present(MIGRATION_NUMBER))
            return;

        String mac = getMacAddressByNetworkInterface("en0");
        // Try wlan0
        if(mac == null){
            mac = getMacAddressByNetworkInterface("wlan0");
        }
        // Try wlan
        if(mac == null){
            mac = getMacAddressByNetworkInterface("wlp1s0");
        }

        if(mac == null){
            Logger.warn("DO NOT IGNORE - Unable to run bootstrap migration.");
            Logger.error("Mac address missing, Tried to search en0 and wlan0 wifi cards. " +
                    "Please make sure they are present or accessible to the process.");
            return;
        }

        // CREATE DATA MIGRATION
        DataMigration d = new DataMigration(MIGRATION_NUMBER, "Boostrap",
                "Bootstrap", "NONE");
        d.save();

        // RUN WHATEVER DATA MIGRATION YOU WANT
        Faker faker = new Faker();

        String deviceName = (faker.ancient().god() +
                "-" + faker.name().lastName() +
                "-" + faker.number().randomNumber(6, false)).toUpperCase();
        Config c = new Config(deviceName,  mac);
        c.save();

        // COMPLETE DATA MIGRATION
        d.setCompletedDate(Utils.getCurrentTimestamp());
        d.setMigrationState(DataMigrationState.COMPLETE);
        d.save();

    }

    /**
     * @return MAC
     */
    private static String getMacAddressByNetworkInterface(String iface) {
        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nis) {
                if (!ni.getName().equalsIgnoreCase(iface)) continue;
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02x:", b));
                    }
                    return res1.deleteCharAt(res1.length() - 1).toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

