package net.boilingwater.dispeakbouyomichanbridge.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class FileIO {
    public static String getFileAllAsString(String fileName) {
        String fileString = "";
        try {
            fileString = FileUtils.readFileToString(
                    FileUtils.getFile("./" + fileName + ".json"),
                    StandardCharsets.UTF_8.toString()
            );
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe(fileName + ".json wasn't found! Exit Program");
            System.exit(1);
        }
        return fileString;
    }
}
