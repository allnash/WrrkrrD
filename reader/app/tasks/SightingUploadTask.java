package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import models.Config;
import models.ReaderSighting;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SightingUploadTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public SightingUploadTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(10, TimeUnit.SECONDS), // initialDelay
                Duration.create(1, TimeUnit.MINUTES), // interval
                this::config,
                this.executionContext
        );
    }

    private void config() {
        Logger.info("Attempting sighting upload task");
        if (Config.get() == null) {
            Logger.info("Skipping sighting upload task as record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("sighting upload task requires registration and then an update. Retrying after 5 minutes.");
        } else {
            uploadSightings();
        }

    }

    private void uploadSightings() {
        // POST registration to server.
        List<ReaderSighting> sightings = ReaderSighting.findAllNotSent();
        // Batch them up if they are more than 50 sightings.
        if (sightings.size() <= 50) {
            uploadSet(sightings);
        } else {
            List<List<ReaderSighting>> sightingsSplits = chopped(sightings, 50);
            for (List<ReaderSighting> sightingsSplit : sightingsSplits) {
                uploadSet(sightingsSplit);
            }
        }

    }

    private void uploadSet(List<ReaderSighting> sightings) {
        HttpResponse<String> response = null;
        try {
            ObjectNode body = Json.newObject();
            body.set("sightings", Json.toJson(sightings));
            response = Unirest.post(Utils.getEngageURL() + "/app/sightings")
                    .header("XAUTHTOKEN", "1234567")
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if (responseJson.has("success")) {
                for (ReaderSighting uploadedSighting : sightings) {
                    uploadedSighting.setSent(true);
                    uploadedSighting.save();
                }
            }

        } catch (UnirestException e) {
            Logger.error("Sighting upload task had an error" + e.getMessage() + ".");
        } catch (NullPointerException e){
            Logger.error("Sighting upload task had an error" + e.getMessage() + ".");
        } catch (Exception e){
            Logger.error("Sighting upload task had an error" + e.getMessage() + ".");
        }
    }

    static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }


}