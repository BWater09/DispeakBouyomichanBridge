package net.boilingwater.dispeakbouyomichanbridge.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.boilingwater.dispeakbouyomichanbridge.config.Setting;

public class Client {
    public static void sendToBouyomiChan(String text) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            client.sendAsync(
                    HttpRequest
                            .newBuilder()
                            .GET()
                            .uri(
                                    new URI(
                                            "http",
                                            "",
                                            Setting.getSetting("bouyomiChanHost"),
                                            Setting.getSettingAsInteger("bouyomiChanPort"),
                                            "/talk",
                                            "text=" + text,
                                            "")
                            ).build(), HttpResponse.BodyHandlers.ofString());
            Logger.getGlobal().fine("Send:" + text);
        } catch (URISyntaxException e) {
            Logger.getGlobal().log(Level.SEVERE, "Can't sent Message to BouyomiChan", e);
        }
    }
}
