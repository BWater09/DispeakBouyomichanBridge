package net.boilingwater.dispeakbouyomichanbridge.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class FileIO {
    public static String getConfigFileAsString(String fileName) {
        return getFileAllAsString("./" + fileName + ".json");
    }

    public static Map<String, String> getEnvFileAsMap(String filePath) {
        String content = getFileAllAsString(filePath);
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(content)) {
            String[] lines = content.split(System.lineSeparator());
            for (String line : lines) {
                String[] envs = line.split("=");
                if (envs.length < 2) {
                    Logger.getGlobal().warning(
                            String.format(
                                    "File[%s] - Line \"%s\" is unrecognized! Ignored.",
                                    filePath,
                                    line
                            )
                    );
                    continue;
                }
                if (StringUtils.isEmpty(envs[0])) {
                    Logger.getGlobal().warning(
                            String.format(
                                    "File[%s] - Key is NULL! Ignored.(Line \"%s\"'s)",
                                    filePath,
                                    line
                            )
                    );
                    continue;
                }
                map.put(StringUtils.trim(envs[0]), StringUtils.trimToEmpty(envs[1]));
            }
        }
        return map;
    }

    public static String getFileAllAsString(String filePath) {
        String fileString = "";
        try {
            fileString = FileUtils.readFileToString(
                    FileUtils.getFile(filePath),
                    StandardCharsets.UTF_8.toString()
            );
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("File [" + filePath + "] isn't found!");
        }
        return fileString;
    }
}
