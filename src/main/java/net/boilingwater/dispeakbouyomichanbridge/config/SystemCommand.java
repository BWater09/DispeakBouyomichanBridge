package net.boilingwater.dispeakbouyomichanbridge.config;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boilingwater.dispeakbouyomichanbridge.io.FileIO;

public class SystemCommand {
    private static SystemCommand SYSTEM_COMMAND = new SystemCommand();
    private final Map<String, String> systemCommandMap;

    private SystemCommand() {
        systemCommandMap = readJson();
    }

    public static void reloadFile() {
        SYSTEM_COMMAND = new SystemCommand();
    }

    private static Map<String, String> readJson() {
        Map<String, String> map = null;
        try {
            map = new ObjectMapper().readValue(FileIO.getConfigFileAsString("system-command"), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("JSON isn't parsed Map Object! Exit Program");
            System.exit(1);
        }
        return map;
    }

    public static Map<String, String> getSystemCommandMap() {
        return SYSTEM_COMMAND.systemCommandMap;
    }

    public static String getSystemCommand(String key) {
        return SYSTEM_COMMAND.systemCommandMap.getOrDefault(key, "");
    }
}
