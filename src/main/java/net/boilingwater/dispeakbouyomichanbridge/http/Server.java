package net.boilingwater.dispeakbouyomichanbridge.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.boilingwater.dispeakbouyomichanbridge.Main;
import net.boilingwater.dispeakbouyomichanbridge.config.Command;
import net.boilingwater.dispeakbouyomichanbridge.config.Setting;
import net.boilingwater.dispeakbouyomichanbridge.config.SystemCommand;
import net.boilingwater.dispeakbouyomichanbridge.external.RunCommand;

public class Server {
    private final Logger logger = Logger.getGlobal();
    private HttpServer server;

    public Server() {
        init();
        logger.info("Start Dispeak-BouyomiChan Bridge " + Main.VERSION);
    }

    private Map<String, String> getQueryMap(HttpExchange exchange) {
        String uriQuery = exchange.getRequestURI().getQuery();
        HashMap<String, String> hashMap = new HashMap<>();
        String[] split = uriQuery.split("&");
        for (String pair : split) {
            String[] entry = pair.split("=");
            hashMap.put(entry[0], String.join("=", Arrays.copyOfRange(entry, 1, entry.length)));
        }
        hashMap.forEach((s, s2) -> logger.fine(String.format("Query - %s:%s", s, s2)));
        return hashMap;
    }

    private String getMessageAsString(HttpExchange exchange) {
        return getQueryMap(exchange).getOrDefault("text", "");
    }

    private void init() {
        Setting.reloadFile();
        Command.reloadFile();
        SystemCommand.reloadFile();
        setLogger();
        try {
            if (server != null) {
                server.stop(0);
            }
            server = HttpServer.create(
                    new InetSocketAddress(Setting.getSettingAsInteger("listeningPort")),
                    0
            );
            logger.log(Level.INFO, String.format("Open Port:%s", Setting.getSettingAsInteger("listeningPort")));
        } catch (IOException e) {
            logger.severe(String.format("Can't Open Port:%s\nexit program.", Setting.getSetting("listeningPort")));
            System.exit(1);
        }
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/", this::handle);
        server.start();
        Client.sendToBouyomiChan("ディスピーク 棒読みちゃんブリッジ " + Main.VERSION + "が起動しました！");
        logger.info("Run Server!");
    }

    private void setLogger() {
        Logger.getGlobal().getParent().setLevel(Level.ALL);
        try {
            for (Handler handler : Logger.getGlobal().getHandlers()) {
                handler.flush();
                handler.close();
                Logger.getGlobal().removeHandler(handler);
            }
            for (Handler handler : Logger.getGlobal().getParent().getHandlers()) {
                Logger.getGlobal().getParent().removeHandler(handler);
            }
            System.setProperty("java.util.logging.SimpleFormatter.format", Setting.getSetting("logFormat"));
            Logger.getGlobal().addHandler(new ConsoleHandler() {{
                setLevel(Level.parse(Setting.getSetting("consoleLogLevel")));
                setFormatter(new SimpleFormatter());
            }});
            Logger.getGlobal().addHandler(new FileHandler(Setting.getSetting("logFileName"), true) {{
                setLevel(Level.parse(Setting.getSetting("fileLogLevel")));
                setFormatter(new SimpleFormatter());
            }});
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Client.sendToBouyomiChan("ロガーの設定に失敗しました。");
        }
    }

    /**
     * ハンドル処理
     *
     * @param exchange HttpExchange
     */
    private void handle(HttpExchange exchange) {
        try {
            exchange.sendResponseHeaders(200, 300L);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String input = getMessageAsString(exchange);

        input = handleSystemCommand(input);

        TreeMap<Integer, RunCommand> commandTreeMap = new TreeMap<>();
        TreeMap<Integer, RunCommand> immediateCommandTreeMap = new TreeMap<>();
        Matcher matcher;
        boolean found;
        do {
            found = false;
            for (Map.Entry<String, Command.CommandBody> commandBodySet : Command.getCommandMap().entrySet()) {
                matcher = Pattern.compile(commandBodySet.getValue().getRegex()).matcher(input);
                if (matcher.find()) {
                    Map<String, String> replaceMap = new HashMap<>();
                    for (String replace : commandBodySet.getValue().getReplacePattern()) {
                        replaceMap.put(replace, matcher.group(replace));
                    }
                    if (commandBodySet.getValue().isImmediate()) {
                        immediateCommandTreeMap.put(matcher.start(), new RunCommand(commandBodySet.getValue(), replaceMap, commandBodySet.getKey()));
                    } else {
                        commandTreeMap.put(matcher.start(), new RunCommand(commandBodySet.getValue(), replaceMap, commandBodySet.getKey()));
                    }
                    input = matcher.replaceFirst("");
                    logger.fine("Generate Command - " + commandBodySet.getKey());
                    found = true;
                }
            }
        } while (found);
        Client.sendToBouyomiChan(input);
        exchange.close();
        if (!immediateCommandTreeMap.isEmpty()) {
            RunCommand.ExecuteEmergencyThreadsInOrder(immediateCommandTreeMap.values().toArray(new RunCommand[0]));
        }
        if (!commandTreeMap.isEmpty()) {
            RunCommand.ExecuteThreadsInOrder(commandTreeMap.values().toArray(new RunCommand[0]));
        }
    }

    private String handleSystemCommand(String text) {
        Matcher matcher;
        boolean found;
        do {
            found = false;
            for (Map.Entry<String, String> entry : SystemCommand.getSystemCommandMap().entrySet()) {
                matcher = Pattern.compile(entry.getValue()).matcher(text);
                if (matcher.find()) {
                    text = matcher.replaceFirst("");
                    switch (entry.getKey()) {
                        case "reloadCommand":
                            Client.sendToBouyomiChan("設定を再ロードします");
                            logger.info("Reload SystemConfig");
                            init();
                            break;
                        case "shutdownCommand":
                            Client.sendToBouyomiChan("実行キューをすべて削除します");
                            logger.info("Shutdown CommandThreads");
                            RunCommand.shutdownThreads();
                            break;
                    }
                    logger.info("Run SystemCommand :" + entry.getKey());
                    found = true;
                }
            }
        } while (found);
        return text;
    }
}
