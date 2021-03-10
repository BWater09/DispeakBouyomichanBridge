package net.boilingwater.dispeakbouyomichanbridge.config;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boilingwater.dispeakbouyomichanbridge.io.FileIO;

public class Setting {
    private static Setting SETTING = new Setting();
    private final Map<String, String> settingMap;

    private Setting() {
        settingMap = readJson();
    }

    public static void reloadFile() {
        SETTING = new Setting();
    }

    private static Map<String, String> readJson() {
        Map<String, String> map = null;
        try {
            map = new ObjectMapper().readValue(FileIO.getFileAllAsString("setting"), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("JSON wasn't parsed Map Object! Exit Program");
            System.exit(1);
        }
        return map;
    }

    public static String getSetting(String key) {
        return SETTING.settingMap.getOrDefault(key, "");
    }

    public static Integer getSettingAsInteger(String key) {
        return Integer.parseInt(getSetting(key));
    }
}
