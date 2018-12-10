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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;

import static utils.Utils.convertJsonFormat;

public class CellphoneSightingController extends Controller {


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
        result.set("cellphone_sighting", Json.toJson(ReaderSighting.findById(id)));
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
        result.set("cellphone_sightings", Json.toJson(ReaderSighting.findAll()));
        return ok(Json.toJson(result));
    }

    /**
     * JSON API to log a Reader Device Sighting Metrics
     *
     * @return Result with JSON String ->
     */

    @BodyParser.Of(BodyParser.Json.class)
    public Result metrics() {
        ObjectNode result = Json.newObject();

        Logger.info("Received new cellphone sighting.");

        JsonNode json = request().body().asJson();
        JsonNode reader_seen_time = json.findPath("reader_seen_time");
        int apples = json.get("apple_count").asInt();
        int androids = json.get("android_count").asInt();
        int others = json.get("others_count").asInt();
        int devicesCount = json.get("devices_count").asInt();
        int resolvedDevicesCount = json.get("resolved_devices_count").asInt();

        Logger.info("Fetching sighting metrics.");

        Timestamp readerSeenTime = new Timestamp(reader_seen_time.longValue());

        if (Config.get() == null) {
            Logger.warn("Sighting metrics ignored, READER CONFIGURATION MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        if (Config.get().getDevice() == null) {
            Logger.warn("Sighting metrics ignored, READER CONFIGURATION DEVICE MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        Device readerDevice = Config.get().getDevice();
        Place readerPlace = Config.get().getPlace();

        ReaderVisitReport rvr = new ReaderVisitReport(readerDevice, readerSeenTime, androids, apples, others);
        rvr.setResolvedDevicesCount(resolvedDevicesCount);
        rvr.setDevicesCount(devicesCount);
        rvr.setPlace(readerPlace);
        rvr.save();

        Logger.info("Sightings metrics: Android ="+ androids + ", Apple =" +
                apples + " # of devices =" + devicesCount + ", Others = " + others + ", # of resolved devices =" + resolvedDevicesCount);

        /*
         * Sighted device
         */

        result.put("status", "ok");
        result.put("success", "ok");

        return ok(result);
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result post() {
        ObjectNode result = Json.newObject();

        Logger.info("Received new cellphone sighting.");

        JsonNode json = request().body().asJson();
        JsonNode reader_seen_time = json.findPath("reader_seen_time");
        JsonNode cellphonesJsonNode = json.findPath("cellphones");
        Timestamp readerSeenTime = new Timestamp(reader_seen_time.longValue());

        if (Config.get() == null) {
            Logger.warn("Sighting ignored, READER CONFIGURATION MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        if (Config.get().getDevice() == null) {
            Logger.warn("Sighting ignored, READER CONFIGURATION DEVICE MISSING.");
            result.put("error", "NOT_CONFIGURED");
            return badRequest(result);
        }

        Device readerDevice = Config.get().getDevice();
        Place readerPlace = Config.get().getPlace();

        /*
         * Sighted device
         */
        for (JsonNode sightedDeviceJson : cellphonesJsonNode) {
            try {
                String sightedDeviceId = getCellphoneExternalId(sightedDeviceJson.get("mac").asText(null));
                String mac = sightedDeviceJson.get("mac").asText();
                int rssi = Double.valueOf(sightedDeviceJson.get("rssi").asText()).intValue();
                String company = sightedDeviceJson.get("company").asText();
                String temp = "";
                String distance = String.valueOf(calculateAccuracy(-59,
                        Double.valueOf(rssi)));

                if (sightedDeviceId == null) {
                    result.put("error", "INVALID_SIGHTED_DEVICE");
                    return badRequest(result);
                }

                // If no end point device found then return error
                HttpResponse<com.mashape.unirest.http.JsonNode> response = null;
                Device sightedDevice = Device.findByExternalDeviceId(sightedDeviceId);
                if (sightedDevice == null) {
                    // Synchronously Fetch sighting Record.
                    response = Unirest.get(Utils.getEngageURL() + "/app/devices/" + sightedDeviceId)
                            .header("XAUTHTOKEN", "1234567")
                            .asJson();
                    JSONObject responseJson = response.getBody().getObject();
                    JsonNode deviceJson;
                    if(responseJson.has("device")){
                        deviceJson = convertJsonFormat(responseJson.getJSONObject("device"));
                        sightedDevice = Device.cloneFromJson(deviceJson);
                    } else {
                        Logger.info("Trying to create a new device with external ID: " + sightedDeviceId);
                        deviceJson = createDevice(Utils.nextSessionId(), sightedDeviceId, mac, "1.0", "PHONE");
                        sightedDevice = Device.cloneFromJson(deviceJson);
                    }
                }
                // Create sighting Record
                createSightingRecord(sightedDevice, readerDevice, readerPlace, readerSeenTime, rssi, temp, distance);
                Logger.info("Cellphone sighting logged into database..");
            } catch (UnirestException e) {
                Logger.error("Cellphone sighting had an error - " + e.getMessage() + ".");
            } catch (Exception e) {
                Logger.error("Cellphone sighting had an error - " + e.getMessage() + ".");
            }
        }
        result.put("status", "ok");
        result.put("success", "ok");

        return ok(result);
    }

    private JsonNode createDevice(String deviceName, String deviceExternalId,
                                      String macAddress, String versionNumber, String deviceTypeName) {
        // POST device to server.
        ObjectNode deviceData = Json.newObject();
        deviceData.put("device_name", deviceName);
        deviceData.put("device_external_id", deviceExternalId);
        deviceData.put("device_mac_address", macAddress);
        deviceData.put("device_version_number", versionNumber);
        deviceData.put("device_type_name", deviceTypeName);

        HttpResponse<String> response = null;
        try {
            response = Unirest.post(Utils.getEngageURL() + "/app/devices")
                    .header("XAUTHTOKEN", "1234567")
                    .header("Content-Type", "application/json")
                    .body(deviceData.toString())
                    .asString();
            JsonNode responseJson = Utils.textToJsonNode(response.getBody());
            if(responseJson.has("device")){
                Logger.info("Device posted successfully for device - " + deviceExternalId + ".");
                return responseJson.get("device");
            } else {
                Logger.error("Device post had an error.");
                return null;
            }
        } catch (UnirestException | NullPointerException e) {
            Logger.error("Device post had an error" + e.getMessage() + ".");
            return  null;
        }
    }

    private void createSightingRecord(Device sightedDevice, Device readerDevice,
                                      Place place, Timestamp readerSeenTime, int rssi, String temp, String distance) {
        // Create reader sighting entry
        ReaderSighting sighting = new ReaderSighting();
        if (sightedDevice == null) {
            Logger.warn("Skipping reader sighting as Sighted device is not Fetched/Provided/Resolved.");
            return;
        }
        sighting.setSightedDevice(sightedDevice);
        sighting.setReaderDevice(readerDevice);
        sighting.setSightedUser(sightedDevice.getOwner());
        sighting.setPlace(place);

        sighting.setWhenSeen(readerSeenTime);
        sighting.setRSSI(String.valueOf(rssi));
        sighting.setTemperature(temp);
        sighting.setDistance(distance);
        sighting.setSent(false);
        sighting.save();

    }

    private String getCellphoneExternalId(String macAddress) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(
                macAddress.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }


}
