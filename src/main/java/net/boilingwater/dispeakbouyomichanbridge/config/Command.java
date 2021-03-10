package net.boilingwater.dispeakbouyomichanbridge.config;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boilingwater.dispeakbouyomichanbridge.io.FileIO;

public class Command {
    private static Command COMMAND = new Command();
    private final Map<String, CommandBody> commandMap;

    private Command() {
        commandMap = readJson();
    }

    public static void reloadFile() {
        COMMAND = new Command();
    }

    public static Map<String, CommandBody> getCommandMap() {
        return COMMAND.commandMap;
    }

    private static Map<String, CommandBody> readJson() {
        Map<String, CommandBody> map = null;
        try {
            map = new ObjectMapper().readValue(FileIO.getFileAllAsString("command"), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("JSON wasn't parsed CommandBody Object! Exit Program");
            System.exit(1);
        }
        return map;
    }


    public static class CommandBody {
        private String regex;
        private String[] replacePattern;
        private String[] runCommand;
        private Map<String, String> env;
        private String path;
        private Boolean immediate;

        public CommandBody() {
        }

        public CommandBody(String regex, String[] replacePattern, String[] runCommand, Map<String, String> env, String path, Boolean immediate) {
            this.regex = regex;
            this.replacePattern = replacePattern;
            this.runCommand = runCommand;
            this.env = env;
            this.path = path;
            this.immediate = immediate;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public String[] getRunCommand() {
            return runCommand;
        }

        public void setRunCommand(String[] runCommand) {
            this.runCommand = runCommand;
        }

        public Map<String, String> getEnv() {
            return env;
        }

        public void setEnv(Map<String, String> env) {
            this.env = env;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }


        public String[] getReplacePattern() {
            return replacePattern;
        }

        public void setReplacePattern(String[] replacePattern) {
            this.replacePattern = replacePattern;
        }

        public Boolean isImmediate() {
            return immediate;
        }

        public void setImmediate(Boolean immediate) {
            this.immediate = immediate;
        }
    }
}