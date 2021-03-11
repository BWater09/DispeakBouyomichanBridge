package net.boilingwater.dispeakbouyomichanbridge.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boilingwater.dispeakbouyomichanbridge.io.FileIO;
import org.apache.commons.lang3.StringUtils;

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
            map = new ObjectMapper().readValue(FileIO.getConfigFileAsString("command"), new TypeReference<>() {
            });
            map.values().forEach(CommandBody::loadEnvFiles);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("JSON isn't parsed CommandBody Object! Exit Program");
            System.exit(1);
        }
        return map;
    }

    public static class CommandBody {
        private Boolean immediate;
        private String regex;
        private String[] replacePattern;
        private String[] runCommand;
        private Map<String, String> env;
        private String[] envFiles;
        private String path;
        private Map<String, String> stdInOut;
        private String executionComment;
        public CommandBody() {
        }
        public CommandBody(String regex, String[] replacePattern, String[] runCommand, Map<String, String> env, String[] envFiles, String path, Boolean immediate) {
            this.regex = regex;
            this.replacePattern = replacePattern;
            this.runCommand = runCommand;
            this.env = env;
            this.envFiles = envFiles;
            this.path = path;
            this.immediate = immediate;
        }

        public Map<String, String> getStdInOut() {
            return stdInOut;
        }

        public void setStdInOut(Map<String, String> stdInOut) {
            this.stdInOut = stdInOut;
        }

        public String getExecutionComment() {
            return executionComment;
        }

        public void setExecutionComment(String executionComment) {
            this.executionComment = executionComment;
        }

        public String[] getEnvFiles() {
            return envFiles;
        }

        public void setEnvFiles(String[] envFiles) {
            this.envFiles = envFiles;
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

        public void loadEnvFiles() {
            Map<String, String> tmpMap = new HashMap<>();
            if (envFiles != null) {
                for (String envFilePath : envFiles) {
                    if (StringUtils.isNotEmpty(envFilePath)) {
                        tmpMap.putAll(FileIO.getEnvFileAsMap(envFilePath));
                    }
                }
            }
            if (env != null) {
                tmpMap.putAll(env);
            }
            env = tmpMap;
        }
    }
}
