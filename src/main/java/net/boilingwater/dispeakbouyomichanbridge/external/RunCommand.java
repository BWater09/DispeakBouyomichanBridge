package net.boilingwater.dispeakbouyomichanbridge.external;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.boilingwater.dispeakbouyomichanbridge.config.Command;

public class RunCommand extends Thread {
    private static final List<Process> processList = new LinkedList<>();
    private static ExecutorService service;
    private static ExecutorService emergencyService;
    private final Logger logger = Logger.getGlobal();
    private final ProcessBuilder builder;
    private final String title;

    public RunCommand(Command.CommandBody command, Map<String, String> replaceMap, String title) {
        this.title = title;
        builder = new ProcessBuilder();
        builder.command(replaceCommand(command, replaceMap));
        builder.redirectErrorStream(true);
        builder.directory(new File(command.getPath()));
        builder.environment().clear();
        builder.environment().putAll(command.getEnv());
        logger.fine("Set Environment Variable " + Arrays.toString(command.getEnv().entrySet().toArray()));
    }

    public static String[] replaceCommand(Command.CommandBody command, Map<String, String> replaceMap) {
        String[] replaced = Arrays.copyOf(command.getRunCommand(), command.getRunCommand().length);
        for (int i = 0; i < replaced.length; i++) {
            for (Map.Entry<String, String> set : replaceMap.entrySet()) {
                replaced[i] = replaced[i].replace(escapeWithUnderBar(set.getKey()), set.getValue());
            }
        }
        return replaced;
    }

    private static String escapeWithUnderBar(String s) {
        return String.format("__%s__", s);
    }

    public static void ExecuteThreadsInOrder(Thread... threads) {
        if (service == null || service.isTerminated()) {
            service = Executors.newSingleThreadExecutor();
        }
        for (Thread t : threads) {
            service.execute(t);
        }
        Logger.getGlobal().info("Add Command Execute Que.");
    }

    public static void ExecuteEmergencyThreadsInOrder(Thread... threads) {
        if (emergencyService == null || emergencyService.isTerminated()) {
            emergencyService = Executors.newSingleThreadExecutor();
        }
        for (Thread t : threads) {
            emergencyService.execute(t);
        }
        Logger.getGlobal().info("Add Emergency Command Execute Que.");
    }

    public static void shutdownThreads() {
        if (service != null) {
            service.shutdown();
            service.shutdownNow();
        }
        if (emergencyService != null) {
            emergencyService.shutdown();
            emergencyService.shutdownNow();
        }
        destroyProcess();
    }

    private static void destroyProcess() {
        processList.forEach(p -> {
            if (p != null && p.isAlive()) {
                Logger.getGlobal().info("Destroy Process");
                p.descendants().forEach(ProcessHandle::destroy);
                p.destroy();
            }
        });
    }

    @Override
    public void run() {
        Process p = null;
        try {
            p = builder.start();
            synchronized (processList) {
                processList.add(p);
            }
            logger.info("Run the Command.");
            Scanner redirect = new Scanner(new InputStreamReader(p.getInputStream(), "SHIFT-JIS"));
            while (redirect.hasNextLine()) {
                logger.finer(title + " - " + redirect.nextLine());
            }
            redirect.close();
            int exit = p.waitFor();
            logger.info("Finish the Command. - " + exit);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't run the Command", e);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            if (p != null) {
                synchronized (processList) {
                    processList.remove(p);
                }
            }
        }
    }
}
