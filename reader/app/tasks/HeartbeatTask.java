package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import models.Config;
import models.Device;
import models.ReaderSighting;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeartbeatTask {

    /*
     *
     *  Runs a 💓 Task to contact the server every minute.
     *   There is an initial 30 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public HeartbeatTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(3, TimeUnit.MINUTES), // initialDelay
                Duration.create(1, TimeUnit.MINUTES), // interval
                this::contactServer,
                this.executionContext
        );
    }

    private void contactServer() {
        Logger.info("attempting to run heartbeat task");
        if (Config.get() == null) {
            Logger.info("Skipping Heartbeat task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping Heartbeat task as Device information record does not exist yet.");
            return;
        }

        try {

            // Get Start Time and End Time.
            Timestamp endTimestamp = Utils.getCurrentTimestamp();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(endTimestamp.getTime());
            cal.add(Calendar.SECOND, -60);
            Timestamp startTimestamp = new Timestamp(cal.getTime().getTime());

            // Fetch Device Entry from configuration.
            Device myDevice = Config.get().getDevice();

            // Fetch metrics.
            List<ReaderSighting> readerSightings = ReaderSighting.find.query().where()
                    .ge("when_created", startTimestamp)
                    .le("when_created", endTimestamp).findList();
            List<ReaderSighting> distinctSightedDevices = ReaderSighting.find.query().where()
                    .ge("when_created", startTimestamp)
                    .le("when_created", endTimestamp)
                    .setDistinct(true).select("sightedDevice").findList();

            // POST heartbeat to server.
            ObjectNode heartbeatData = Json.newObject();
            heartbeatData.put("when_sent", Utils.getCurrentTimestamp().getTime());
            heartbeatData.put("metric_duration_seconds", 60);
            heartbeatData.put("metric_duration_when_started", startTimestamp.toLocalDateTime().toString());
            heartbeatData.put("metric_duration_when_ended", endTimestamp.toLocalDateTime().toString());
            heartbeatData.put("reader_sightings_count", readerSightings.size());
            heartbeatData.put("sighted_devices_count", distinctSightedDevices.size());
            heartbeatData.put("scanner_running", BluetoothTask.running);

            HttpResponse<String> response = null;
            response = Unirest.post(Utils.getEngageURL() + "/app/devices/" + myDevice.getId() + "/heartbeat")
                    .header("XAUTHTOKEN", "1234567")
                    .header("Content-Type", "application/json")
                    .body(heartbeatData.toString())
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if (responseJson.has("success")) {
                Logger.info("Heartbeat posted successfully for device - " + myDevice.getId() + ".");
            }
        } catch (Exception e) {
            Logger.error("Heartbeat task had an error" + e.getMessage() + ".");
        }
    }
}