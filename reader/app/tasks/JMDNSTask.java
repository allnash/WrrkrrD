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
import models.Peer;
import models.ReaderSighting;
import play.Logger;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utils.Utils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JMDNSTask {

    /*
     *
     *  Runs a 💓 Task to contact the server every minute.
     *   There is an initial 30 second delay to kick off the task.
     * */

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private final String JMDNS_SERVICE_TYPE = "_http._tcp.wrrkrr.";
    private static JmDNS jmdnsService;
    private static JmDNS jmdnsListener;

    private static class JMDNSListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            Logger.info("JmDNS service added - " + event.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Logger.info("JmDNS peer removed - " + event.getName());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            try {
                Logger.info("JmDNS discovered peer - " + event.getName() + " at IP:" + event.getDNS().getInetAddress().getHostAddress());
                String peerName = event.getName().split(":")[1];
                String peerHostName = event.getDNS().getHostName();
                String peerHostAddress = event.getDNS().getInetAddress().getHostAddress();
                String peerPort = String.valueOf(event.getInfo().getPort());
                String peerServiceType = event.getType();
                Peer peer = Peer.findByName(event.getName().split(":")[1]);
                if (peer == null) {
                    peer = new Peer(peerName, peerHostAddress, peerHostName, peerPort, peerServiceType);
                    peer.save();
                } else {
                    peer.setName(peerName);
                    peer.setHostName(peerHostName);
                    peer.setHostAddress(peerHostAddress);
                    peer.setPort(peerPort);
                    peer.setServiceType(peerServiceType);
                    peer.save();
                }
            } catch (IOException e) {
                Logger.error("JmDNS peer IP discovery error for - " + event.getName());
            }
        }
    }

    @Inject
    public JMDNSTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(10, TimeUnit.SECONDS), // initialDelay
                Duration.create(10, TimeUnit.SECONDS), // interval
                this::startDNS,
                this.executionContext
        );
        this.actorSystem.scheduler().schedule(
                Duration.create(10, TimeUnit.SECONDS), // initialDelay
                Duration.create(10, TimeUnit.SECONDS), // interval
                this::startDNSListener,
                this.executionContext
        );
    }

    private void startDNS() {
        if (Config.get() == null) {
            Logger.info("Skipping JmDNS start task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping JmDNS start task as Config Device record does not exist yet.");
            return;
        }

        try {
            if (jmdnsService == null) {
                Logger.info("Attempting to run JmDNS start task");
                // Create a JmDNS instance
                jmdnsService = JmDNS.create(InetAddress.getLocalHost());

                // Register a service
                String serviceName = "WRRKRR:" + Config.get().getDeviceName();
                ServiceInfo serviceInfo = ServiceInfo.create(JMDNS_SERVICE_TYPE, serviceName, 8000, "WrrKrr Service");
                jmdnsService.registerService(serviceInfo);
                Logger.info("JmDNS Task started.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            Logger.error("JmDNS Task stopped due to exception - " + e.getMessage() + ".");
        } catch (NullPointerException e) {
            Logger.error("JmDNS Task stopped due to exception - " + e.getMessage() + ".");
        }
    }

    private void startDNSListener() {
        if (Config.get() == null) {
            Logger.info("Skipping JmDNS Listener task as Config record does not exist yet.");
            return;
        }
        if (Config.get().getDevice() == null) {
            Logger.info("Skipping JmDNS Listener task as Config Device record does not exist yet.");
            return;
        }

        try {
            if (jmdnsListener == null) {
                Logger.info("Attempting to run JmDNS Listener task");
                // Create a JmDNS instance
                jmdnsListener = JmDNS.create(InetAddress.getLocalHost());

                // Add a service listener
                jmdnsListener.addServiceListener(JMDNS_SERVICE_TYPE, new JMDNSListener());
                Logger.info("JmDNS Listener started.");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            Logger.error("JmDNS Listener Task stopped due to exception - " + e.getMessage() + ".");
        } catch (NullPointerException e) {
            Logger.error("JmDNS Listener Task stopped due to exception - " + e.getMessage() + ".");
        } catch (Exception e){
            Logger.error("JmDNS Listener Task stopped due to exception - " + e.getMessage() + ".");
        }
    }
}