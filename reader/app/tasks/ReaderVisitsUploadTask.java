package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import models.Config;
import models.Visit;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReaderVisitsUploadTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public ReaderVisitsUploadTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(30, TimeUnit.SECONDS), // initialDelay
                Duration.create(1, TimeUnit.MINUTES), // interval
                this::config,
                this.executionContext
        );
    }

    private void config() {
        Logger.info("Attempting reader visit upload task");
        if (Config.get() == null) {
            Logger.info("Skipping reader visit upload task as record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("reader visit upload task requires registration and then an update. Retrying after 5 minutes.");
        } else {
            uploadReaderVisits();
        }

    }

    private void uploadReaderVisits() {
        // POST registration to server.
        List<Visit> visits = Visit.findAllNotSent();
        // Batch them up if they are more than 50 sightings.
        if (visits.size() <= 50) {
            uploadSet(visits);
        } else {
            List<List<Visit>> visitSplits = chopped(visits, 50);
            for (List<Visit> visitSplit : visitSplits) {
                uploadSet(visitSplit);
            }
        }

    }

    private void uploadSet(List<Visit> visitsList) {
        HttpResponse<String> response = null;
        try {
            ObjectNode body = Json.newObject();
            body.set("reader_visits", Json.toJson(visitsList));
            response = Unirest.post(Utils.getEngageURL() + "/app/reader_visits")
                    .header("XAUTHTOKEN", "1234567")
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if (responseJson.has("success")) {
                for (Visit visit : visitsList) {
                    visit.setSent(true);
                    visit.setProcessed(true);
                    visit.save();
                }
            }

        } catch (Exception e) {
            Logger.error("Reader visit upload task had an error" + e.getMessage() + ".");
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