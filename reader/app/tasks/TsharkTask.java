package tasks;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import models.CapturedDevice;
import models.CapturedPacket;
import models.Config;
import models.ReaderVisitReport;
import org.apache.commons.lang3.SystemUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;
import play.Logger;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TsharkTask {

    /*
     *
     *  Runs a Tshark packet capture task to contact the server every minute.
     *   There is an initial 5 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private static Timestamp lastTaskRun;
    // Static share packet capture variables
    private static CapturedPacket cp = null;
    private static String rawPacket = "";
    private static StartedProcess sharkCaptureProcess;
    private static int skippedPackets = 0;
    private static int totalPackets = 0;
    // Static storage of packets
    private static Map<String, CapturedDevice> broadcastDevices = new ConcurrentHashMap<>();
    private static Map<String, CapturedDevice> probeDevices = new ConcurrentHashMap<>();
    private static Map<String, CapturedDevice> RTSandCTS = new ConcurrentHashMap<>();
    // Track masked devices for as many minutes as possible to improve accuracy.
    private static Map<String, ArrayList<Long>> maskedDevice = new ConcurrentHashMap<>();
    // Counters
    private static int apples = 0;
    private static int androids = 0;
    private static int printers = 0;
    private static int skipped = 0;
    private static int laptops = 0;
    private static int smartdevices = 0;
    private static int networkingDevices = 0;
    private static int unclassified = 0;
    private static int masked = 0;

    @Inject
    public TsharkTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(5, TimeUnit.SECONDS), // initialDelay
                Duration.create(65, TimeUnit.SECONDS), // interval
                this::startScanner,
                this.executionContext
        );
    }

    private void startScanner() {
        if (Config.get() == null) {
            Logger.info("Skipping tshark scanner task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping tshark scanner task as Device information record does not exist yet.");
            return;
        }

        try {

            if (lastTaskRun == null) {
                lastTaskRun = Utils.getCurrentTimestamp();
            }

            Logger.info("Attempting to run tshark scanner task @ " + lastTaskRun.toLocalDateTime().toString());

            // RUN SCANNER SCRIPT AS SUDO.
            Logger.info("Attempting to run tshark scanner script. Please wait.");

            // Set defaults
            cp = null;
            skippedPackets = 0;
            totalPackets = 0;
            broadcastDevices = new ConcurrentHashMap<>();
            probeDevices = new ConcurrentHashMap<>();
            RTSandCTS = new ConcurrentHashMap<>();
            rawPacket = "";
            apples = 0;
            androids = 0;
            laptops = 0;
            smartdevices = 0;
            printers = 0;
            networkingDevices = 0;
            masked = 0;
            unclassified = 0;
            skipped = 0;

            String adapter;
            if (SystemUtils.IS_OS_LINUX) {
                adapter = "wlp1s0";
            } else {
                adapter = "en0";
            }
            capture(adapter);

        } catch (Exception e) {
            Logger.error("Tshark scanner task had an error. Error:", e);
        }
    }

    public void capture(String adapter) throws Exception {
        if (sharkCaptureProcess != null) {
            if (sharkCaptureProcess.getProcess().isAlive()) {
                Logger.info("Tshark process is still running. Skipping this run, but here are some stats:");
                Logger.debug("<<<<>>>>");
                Logger.debug("Wifi SSID devices:" + broadcastDevices.size());
                Logger.debug("Probe devices:" + probeDevices.size());
                Logger.debug("Total packets: "+ totalPackets);
                Logger.debug("Skipped packets:" + skippedPackets);
                Logger.debug("<<<<>>>>");
                return;
            }
        }
        // Wild card filter. Get all packets frames.
        sharkCaptureProcess = new ProcessExecutor()
                .command("tshark", "-i", adapter, "-I", "-V", "-l", "-a", "duration:60")
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        parseLine(line);
                    }
                }).start();

        while (true) {
            boolean done = sharkCaptureProcess.getFuture().isDone();
            if (done) {
                Logger.info("Running Tshark packet capture counters");
                runCounters();
                Logger.info("Tshark Capture Sweep completed.");
                Logger.debug(">>>>");
                Logger.debug("Wifi SSID devices:" + broadcastDevices.size());
                Logger.debug("Probe devices:" + probeDevices.size());
                Logger.debug("Total packets: "+ totalPackets);
                Logger.debug("Skipped packets:" + skippedPackets);
                Logger.debug("<<<<");
                Logger.info("-------------------------------------------------------------------------------------------" +
                        "-------------------------");
                Logger.info(
                        "Apple Devices:" + apples +
                                ", Androids:" + androids +
                                ", Printers:" + printers +
                                ", Networking Devices:" + networkingDevices +
                                ", Masked Devices:" + masked +
                                ", Laptops:" + laptops +
                                ", Unclassified:" + unclassified);
                Logger.info("-------------------------------------------------------------------------------------------" +
                        "-------------------------");
                ReaderVisitReport rvp = new ReaderVisitReport(Config.get().getDevice(), Utils.getCurrentTimestamp(),
                        androids, apples, unclassified);
                rvp.setLaptopCounts(laptops);
                rvp.setMaskedDevicesCount(masked);
                rvp.setDevicesCount(apples + androids + laptops);
                rvp.setNetworkingDevicesCount(networkingDevices);
                rvp.save();
                break;
            }
        }
    }

    private void runCounters() {
        for (CapturedDevice device : probeDevices.values()) {
            countDeviceFromMap(device, "PROBE >");
        }
        for (CapturedDevice device : RTSandCTS.values()) {
            countDeviceFromMap(device, "RTS/CTS >");
        }
    }

    private void countDeviceFromMap(CapturedDevice device, String type) {
        if (Utils.isDebugMode()) {
            StringBuilder pac = new StringBuilder();

            for (CapturedPacket cp : device.getPackets()) {
                pac.append("[").append(cp.getChannel()).append("]");
                pac.append("{").append(cp.getRssi()).append("(");
                pac.append(new DecimalFormat("#.##").format(cp.getDistance())).append("m)}");
                pac.append("|").append(cp.getSeqNum()).append("|, ");
            }
            Logger.debug(type + " " + device.getOuiDeviceType().name() + " " + device.getOuiCompanyName() + " " + device.getMacAddress() + " PACKETS=" + pac);
        }
        switch (device.getOuiDeviceType()) {
            case APPLE:
                apples++;
                break;
            case ANDROID:
                androids++;
                break;
            case PRINTERS:
                printers++;
                break;
            case NETWORKING_DEVICE:
                networkingDevices++;
                break;
            case LAPTOP:
                laptops++;
                break;
            case SMART_HOME:
                smartdevices++;
                break;
            case MASKED:
                masked++;
                if (Utils.isDebugMode()) {
                    for (CapturedPacket cp : device.getPackets()) {
                      //  Logger.debug("----------PACKET SEQ " + cp.getSeqNum() + "--------------");
                      //  Logger.debug(device.getPackets().get(0).getRaw());
                      //  Logger.debug("----------------------------------");
                    }
                }
                break;
            case RASPBERRY_PI_OR_IOT:
                smartdevices++;
                break;
            case UNCLASSIFIED:
                unclassified++;
                break;
            default:
                skipped++;
        }
    }

    public static String getMacFromLine(String line) {
        try {
            int start = line.indexOf("(") + 1;
            int end = line.indexOf(")");
            return line.substring(start, end);
        } catch (Exception e) {
            return "ERROR";
        }
    }


    public void superScan(String fileName, Map<String, CapturedDevice> allCapturedDevices) throws Exception {
        // Wlan Probe request filter. Get all packets.
        StartedProcess probeCountTsharkFilter = new ProcessExecutor()
                .command("tshark", "-r", fileName, "-Y",
                        "(wlan.fc.type_subtype == 28) && (wlan.ra == 22:22:22:22:22:22)",
                        "-T", "fields", "-e",
                        "wlan.sa", "-e", "wlan.ssid", "-e", "radiotap.dbm_antsignal")
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        String[] splits = line.split("\t");
                        if (splits.length == 3) {
                            if (splits[0].contains(",")) {
                                splits[0] = splits[0].split(",")[0];
                            }
                            if (!splits[0].isEmpty() && !splits[1].isEmpty() && !splits[2].isEmpty()) {
                                String mac = splits[0];

                            }
                        }
                    }
                }).start();

        while (true) {
            boolean done = probeCountTsharkFilter.getFuture().isDone();
            if (done) {


            }
        }
    }

    public void extractQoSNullDataFrameCounts(String fileName, Map<String, CapturedDevice> allCapturedDevices) throws Exception {
        // QoS Null Data frame filter. Get all packets.
        StartedProcess probeCountTsharkFilter = new ProcessExecutor()
                .command("tshark", "-r", fileName, "-Y", "(wlan.fc.type_subtype == 36) && !_ws.expert",
                        "-T", "fields", "-e",
                        "wlan.sa", "-e", "wlan.bssid", "-e", "radiotap.dbm_antsignal", "-e", "wlan_radio.channel")
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        String[] splits = line.split("\t");
                        if (splits.length == 4) {
                            if (splits[0].contains(",")) {
                                splits[0] = splits[0].split(",")[0];
                            }
                            if (!splits[0].isEmpty() && !splits[1].isEmpty() && !splits[2].isEmpty()) {
                                String mac = splits[0];
                            }
                        }
                    }
                }).start();

        while (true) {
            boolean done = probeCountTsharkFilter.getFuture().isDone();
            if (done) {


            }
        }
    }

    private void parseLine(String line) {
        try {
            if (line.startsWith("Capturing")) {
                return;
            }
            if (line.startsWith("Frame")) {
                if (cp != null) {
                    if(Utils.isDebugMode()){
                        cp.setRaw(rawPacket);
                    }
                    if ((cp.getPacketType() == CapturedPacket.PacketType.BEACON) && (cp.getBSSID() != null)) {
                        CapturedDevice device = broadcastDevices.get(cp.getSrcMac());
                        if (device == null) {
                            device = new CapturedDevice(cp.getSrcMac());
                            device.addPacket(cp);
                            broadcastDevices.put(device.getMacAddress(), device);
                        } else {
                            device.addPacket(cp);
                        }
                    } else if ((cp.getPacketType() == CapturedPacket.PacketType.PROBE_REQUEST) && cp.getRssi() >= -80) {
                        CapturedDevice device = probeDevices.get(cp.getSrcMac());
                        if (device == null) {
                            device = new CapturedDevice(cp.getSrcMac());
                            device.addPacket(cp);
                            probeDevices.put(device.getMacAddress(), device);
                        } else {
                            device.addPacket(cp);
                        }
                    } else if (((cp.getPacketType() == CapturedPacket.PacketType.RTS) ||
                            (cp.getPacketType() == CapturedPacket.PacketType.CTS)) &&
                            cp.getRssi() >= -80) {
                        CapturedDevice device = RTSandCTS.get(cp.getRecv_mac());
                        if (device == null) {
                            device = new CapturedDevice(cp.getRecv_mac());
                            device.addPacket(cp);
                            RTSandCTS.put(device.getMacAddress(), device);
                        } else {
                            device.addPacket(cp);
                        }
                        totalPackets++;
                    } else {
                        skippedPackets++;
                    }
                    // Calculate distance
                    double distance = calculateDistance(cp.getRssi(), cp.getFreqMhz());
                    cp.setDistance(distance);
                }
                rawPacket = "";
                cp = new CapturedPacket();
            } else {
                if (line.contains("Malformed Packet: IEEE 802.11")) {
                    // DROP packet if malformed.
                    // Logger.debug("Malformed packet. Dropping.");
                    cp = null;
                } else if (line.contains("FCS Status: Bad")) {
                    // DROP packet if has bad checksum.
                    // Logger.debug("Malformed packet. Dropping.");
                    cp = null;
                }
                if (cp != null) {
                    // Beacon frame
                    if (line.contains("IEEE 802.11 Beacon frame")) {
                        cp.setPacketType(CapturedPacket.PacketType.BEACON);
                    }
                    // Probe request
                    if (line.contains("IEEE 802.11 Probe Request")) {
                        cp.setPacketType(CapturedPacket.PacketType.PROBE_REQUEST);
                    }
                    // Probe response
                    if (line.contains("IEEE 802.11 Probe Response")) {
                        cp.setPacketType(CapturedPacket.PacketType.BEACON);
                    }
                    // RTS
                    if (line.contains("IEEE 802.11 Request-to-send")) {
                        cp.setPacketType(CapturedPacket.PacketType.RTS);
                    }
                    // CTS
                    if (line.contains("IEEE 802.11 Clear-to-send")) {
                        cp.setPacketType(CapturedPacket.PacketType.CTS);
                    }
                    // seq number
                    if (line.contains("Sequence number:")) {
                        try {
                            String sq = line.split("Sequence number:")[1];
                            int seq = Integer.valueOf(sq.trim());
                            cp.setSeqNum(seq);
                        } catch (Exception e) {
                            // DO NOTHING
                        }
                    }
                    // Radio tap header info
                    if (line.contains("Channel:")) {
                        try {
                            int channel = Integer.valueOf(line.trim().replace("Channel:", "").trim());
                            cp.setChannel(channel);
                        } catch (Exception e) {
                            // DO NOTHING
                        }
                    }
                    if (line.contains("Frequency:")) {
                        try {
                            String freqString = line.trim().replace("Frequency:", "").trim();
                            freqString = freqString.replace("MHz", "");
                            int freq = Integer.valueOf(freqString);
                            cp.setFreqMhz(freq);
                        } catch (Exception e) {
                            // DO NOTHING
                        }
                    }
                    if (line.contains("Signal strength (dBm):")) {
                        int rssi = Integer.valueOf(line.trim().replace("Signal strength (dBm):", "").replace("dBm", "").trim());
                        cp.setRssi(rssi);
                    }
                    // Get mac addresses
                    if (line.contains("Source address:")) {
                        cp.setSrcMac(getMacFromLine(line));
                    }
                    // Get mac addresses
                    if (line.contains("Source address:")) {
                        cp.setSrcMac(getMacFromLine(line));
                    }
                    // Get mac addresses
                    if (line.contains("BSS Id:")) {
                        cp.setBSSID(getMacFromLine(line));
                    }
                    if (line.contains("Destination address:")) {
                        cp.setDestMac(getMacFromLine(line));
                    }
                    if (line.contains("Transmitter address:")) {
                        cp.setTransMac(getMacFromLine(line));
                    }
                    if (line.contains("Receiver address:")) {
                        cp.setRecv_mac(getMacFromLine(line));
                    }
                    if (line.contains("Tag: SSID parameter set:")) {
                        String ssid = line.replace("Tag: SSID parameter set:", "").trim();
                        cp.setSSID(ssid);
                    }
                    // ONLY IN DEBUG MODE ADD RAW PACKET. THIS CAN CAUSE THE HEAP TO GROW BIG BEFORE DESTROYED.
                    // THIS CAN CAUSE THE PROGRAM TO RUN SLOW ON RASPBERRY PI.
                    if(Utils.isDebugMode()){
                         // rawPacket = rawPacket + line + "\n";
                    }
                } else {
                    // DO NOTHING.
                }
            }

        } catch (Exception e) {
            Logger.error("skipping line", e);
        }
    }

    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}