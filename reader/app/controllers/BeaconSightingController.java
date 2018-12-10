package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import models.*;
import org.json.JSONObject;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.sql.Timestamp;

import static utils.Utils.convertJsonFormat;

public class BeaconSightingController extends Controller {


    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result get(String id) {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.put("success", "ok");
        result.set("reader_sighting", Json.toJson(ReaderSighting.findById(id)));
        return ok(Json.toJson(result));
    }


    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result all() {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.put("success", "ok");
        result.set("reader_sightings", Json.toJson(ReaderSighting.findAll()));
        return ok(Json.toJson(result));
    }

    /**
     * JSON API to log a Reader Device Sighting
     *
     * @return Result with JSON String ->
     */

    @BodyParser.Of(BodyParser.Json.class)
    public Result post() {
        ObjectNode result = Json.newObject();

        Logger.info("Received new Beacon sighting.");

        JsonNode json = request().body().asJson();
        String sightedDeviceId = json.findPath("sighted_device_id").textValue();
        JsonNode tlm = json.findPath("tlm");
        JsonNode reader_seen_time = json.findPath("reader_last_seen_time");

        Timestamp readerSeenTime = new Timestamp(Long.valueOf(reader_seen_time.asText()));
        String visitId = json.findPath("visit_id").textValue();
        String visitActivity = json.findPath("visit_activity").textValue();
        String rssi = String.valueOf(json.findPath("rssi").intValue());
        String temp = String.valueOf(tlm.findPath("temp").intValue());
        String batt = String.valueOf(tlm.findPath("vbatt"));
        String distance = String.valueOf(json.findPath("distance").doubleValue());

        if (Config.get() == null) {
            Logger.warn("Beacon sighting ignored, READER CONFIGURATION MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        if (Config.get().getDevice() == null) {
            Logger.warn("Beacon sighting ignored, READER CONFIGURATION DEVICE MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        Device readerDevice = Config.get().getDevice();
        Place readerPlace = Config.get().getPlace();

        /*
         * Sighted device
         */

        if (sightedDeviceId == null) {
            result.put("error", "INVALID_SIGHTED_DEVICE");
            return badRequest(result);
        }

        // If no end point device found then return error
        HttpResponse<com.mashape.unirest.http.JsonNode> response = null;
        try {
            Device sightedDevice = Device.findByExternalDeviceId(sightedDeviceId);
            if (sightedDevice == null) {
                // Synchronously Fetch sighting Record.
                response = Unirest.get(Utils.getEngageURL() + "/app/devices/" + sightedDeviceId)
                        .header("XAUTHTOKEN", "1234567")
                        .asJson();
                JSONObject responseJson = response.getBody().getObject();
                if(responseJson.has("device")){
                    JsonNode deviceJson = convertJsonFormat(responseJson.getJSONObject("device"));
                    sightedDevice = Device.cloneFromJson(deviceJson);
                } else {
                    if(Utils.isAutoRegistrationEnabled()){
                        HttpResponse<String> registrationResponse = null;
                        // Create Device object and register.
                        ObjectNode beaconDevice = Json.newObject();
                        beaconDevice.put("device_external_id", sightedDeviceId);
                        beaconDevice.put("device_name", sightedDeviceId);
                        beaconDevice.put("device_version_number", "0");
                        beaconDevice.set("device_mac_address", null);
                        beaconDevice.put("device_type_name", "GENERIC");
                        registrationResponse = Unirest.post(Utils.getEngageURL() + "/app/devices")
                                .header("XAUTHTOKEN", "1234567")
                                .header("Content-Type", "application/json")
                                .body(Json.toJson(beaconDevice).toString())
                                .asString();
                        JsonNode registrationResponseJson = Utils.textToJsonNode(registrationResponse.getBody());
                        sightedDevice = Device.cloneFromJson(registrationResponseJson.get("device"));
                    } else {
                        Logger.error("Beacon sighting not logged. Device record not found on the server.");
                    }
                }
            }
            // Create sighting Record
            ReaderSighting sighting =  createSightingRecord(sightedDevice, readerDevice, readerPlace, readerSeenTime, rssi, batt, temp, distance);
            // Create or update visit Record
            updateVisitRecord(visitId, visitActivity, sighting);
            Logger.info("Beacon sighting logged into database..");
        } catch (UnirestException e) {
            Logger.error("Beacon sighting not logged. Error - " + e.getMessage() + ".");
        }
        result.put("status", "ok");
        result.put("success", "ok");

        return ok(result);
    }

    private ReaderSighting createSightingRecord(Device sightedDevice, Device readerDevice,
                                      Place place, Timestamp readerSeenTime, String rssi, String batt, String temp, String distance) {

        // Create reader sighting entry
        ReaderSighting readerSighting = new ReaderSighting();
        if (sightedDevice == null) {
            Logger.warn("Skipping beacon sighting as Sighted device is not Fetched/Provided/Resolved.");
            return null;
        }

        readerSighting.setSightedDevice(sightedDevice);
        readerSighting.setReaderDevice(readerDevice);
        readerSighting.setSightedUser(sightedDevice.getOwner());
        readerSighting.setPlace(place);
        readerSighting.setBatteryLevel(batt);
        readerSighting.setWhenSeen(readerSeenTime);
        readerSighting.setRSSI(rssi);
        readerSighting.setTemperature(temp);
        readerSighting.setDistance(distance);
        readerSighting.setSent(false);

        readerSighting.save();

        return readerSighting;
    }


    private void updateVisitRecord(String visitId, String visitActivity, ReaderSighting readerSighting) {
        if(readerSighting == null){
            Logger.warn("Skipping visit record as Sighting was not logged.");
            return;
        }
        // Create visit entry if missing
        Visit visit = Visit.findById(visitId);
        if(visit == null){
            visit =  new Visit();
            // OVERRIDE VISIT ID with the ID provided here.
            visit.setId(visitId);
            visit.setEndpointDevice(Config.get().getDevice());
            visit.setUserDevice(readerSighting.getSightedDevice());
            visit.setUser(readerSighting.getSightedDevice().getOwner());
            visit.setWhenStarted(readerSighting.getWhenSeen());
            visit.setAt(readerSighting.getPlace());
            visit.setProcessed(false);
            visit.setSent(false);
            visit.save();
            Logger.info("Create visit record - " + visitId + " for activity - " + visitActivity);
        } else {
            visit.setWhenEnded(readerSighting.getWhenSeen());
            visit.setProcessed(false);
            visit.setSent(false);
            visit.save();
            Logger.info("Updating visit record - " + visitId + " for activity - " + visitActivity);
        }
    }

}
