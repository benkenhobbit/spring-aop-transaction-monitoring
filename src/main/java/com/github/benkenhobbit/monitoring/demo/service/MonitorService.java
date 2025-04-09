/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2025, A. Aquila
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.benkenhobbit.monitoring.demo.service;

import com.github.benkenhobbit.monitoring.aspect.TransactionMonitoringAspect;
import com.github.benkenhobbit.monitoring.config.TransactionMonitoringConfiguration;
import com.github.benkenhobbit.monitoring.demo.controller.DatabaseInterface;
import com.github.benkenhobbit.monitoring.demo.model.Instrument;
import com.github.benkenhobbit.monitoring.model.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class MonitorService {

    public enum TransactionType {
        NOT_SUPPORTED, REQUIRED, REQUIRES_NEW, NEVER, DEFAULT, NONE
    }

    public static final String LOG_PREFIX = "[STM]";

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final TransactionMonitoringConfiguration monitoringConfiguration;

    private final DatabaseInterface databaseInterface;

    private final TransactionMonitoringAspect monitoringAspect;

    private final NestedMonitorService nestedMonitorService;

    /**
     * Constructor injection
     * @param monitoringConfiguration
     * @param databaseInterface
     * @param monitoringAspect
     */
    @Autowired
    public MonitorService(TransactionMonitoringConfiguration monitoringConfiguration,
                          DatabaseInterface databaseInterface,
                          TransactionMonitoringAspect monitoringAspect,
                          NestedMonitorService nestedMonitorService) {
        this.monitoringConfiguration = monitoringConfiguration;
        this.databaseInterface = databaseInterface;
        this.monitoringAspect = monitoringAspect;
        this.nestedMonitorService = nestedMonitorService;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Instrument> getInstrumentNotSupportedNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Instrument> getInstrumentRequiredNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Instrument> getInstrumentRequiresNewNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Transactional(propagation = Propagation.NEVER)
    public List<Instrument> getInstrumentNeverNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Transactional
    public List<Instrument> getInstrumentDefaultNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    public List<Instrument> getInstrumentNoneNotSupported() {
        return databaseInterface.getInstrumentNotSupported();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Future<List<Instrument>> getAsyncInstrumentsNotSupported() {
        return nestedMonitorService.getAsyncInstrumentsNotSupported();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Instrument> getInstrumentRequiresNewNested() {
        return nestedMonitorService.getInstrumentNestedRequiresNew();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Future<List<Instrument>> getAsyncInstrumentsRequired() {
        return nestedMonitorService.getAsyncInstrumentsRequired();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void getAsyncInstrumentsRequiresNew() {
        nestedMonitorService.getAsyncInstrumentNestedRequiresNew();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Instrument> getInstrumentNotSupportedRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Instrument> getInstrumentRequiredRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Instrument> getInstrumentRequiresNewRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    @Transactional(propagation = Propagation.NEVER)
    public List<Instrument> getInstrumentNeverRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    @Transactional
    public List<Instrument> getInstrumentDefaultRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    public List<Instrument> getInstrumentNoneRequired() {
        return databaseInterface.getInstrumentRequired();
    }

    public List<Instrument> getInstrumentsNotSupportedInController(TransactionType type) {
        List<Instrument> list = null;
        switch (type) {
            case NOT_SUPPORTED -> list = getInstrumentNotSupportedNotSupported();
            case REQUIRED -> list = getInstrumentRequiredNotSupported();
            case REQUIRES_NEW -> list = getInstrumentRequiresNewNotSupported();
            case NEVER -> list = getInstrumentNeverNotSupported();
            case DEFAULT -> list = getInstrumentDefaultNotSupported();
            case NONE -> list = getInstrumentNoneNotSupported();
        }
        return list;
    }

    public List<Instrument> getInstrumentsRequiredInController(TransactionType type) {
        List<Instrument> list = null;
        switch (type) {
            case NOT_SUPPORTED -> list = getInstrumentNotSupportedRequired();
            case REQUIRED -> list = getInstrumentRequiredRequired();
            case REQUIRES_NEW -> list = getInstrumentRequiresNewRequired();
            case NEVER -> list = getInstrumentNeverRequired();
            case DEFAULT -> list = getInstrumentDefaultRequired();
            case NONE -> list = getInstrumentNoneRequired();
        }
        return list;
    }

    /**
     * Execute test
     */
    public void executeTest() {
        clearAll();

        enable();

        /**
         *
         */
        List<Instrument> list = getInstrumentsNotSupportedInController(TransactionType.NOT_SUPPORTED);
        log.info("---  1. Found " + list.size() + " elements");
        list = getInstrumentsNotSupportedInController(TransactionType.REQUIRED);
        log.info("---  2. Found " + list.size() + " elements");
        list = getInstrumentsNotSupportedInController(TransactionType.REQUIRES_NEW);
        log.info("---  3. Found " + list.size() + " elements");
        list = getInstrumentsNotSupportedInController(TransactionType.NEVER);
        log.info("---  4. Found " + list.size() + " elements");
        list = getInstrumentsNotSupportedInController(TransactionType.NONE);
        log.info("---  5. Found " + list.size() + " elements");

        Future<List<Instrument>> futureList = getAsyncInstrumentsNotSupported();
        while(!futureList.isDone()) {
            System.out.println("Loading...");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {}
        }
        try {
            log.info("---  6. Found " + futureList.get().size() + " elements");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        list = getInstrumentRequiresNewNested();
        log.info("---  7. Found " + list.size() + " elements");

        getAsyncInstrumentsRequiresNew();

        /**
         *
         */
        list = getInstrumentsRequiredInController(TransactionType.NOT_SUPPORTED);
        log.info("---  8. Found " + list.size() + " elements");
        list = getInstrumentsRequiredInController(TransactionType.REQUIRED);
        log.info("---  9. Found " + list.size() + " elements");
        list = getInstrumentsRequiredInController(TransactionType.REQUIRES_NEW);
        log.info("--- 10. Found " + list.size() + " elements");
        list = getInstrumentsRequiredInController(TransactionType.NEVER);
        log.info("--- 11. Found " + list.size() + " elements");
        list = getInstrumentsRequiredInController(TransactionType.NONE);
        log.info("--- 12. Found " + list.size() + " elements");

        futureList = getAsyncInstrumentsRequired();
        while(!futureList.isDone()) {
            System.out.println("Loading...");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {}
        }
        try {
            log.info("--- 13. Found " + futureList.get().size() + " elements");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        printLog();

        printShortLog();

        printStatsByThread();

        printStatsByMethod();

        disable();
    }

    /**
     * Get all events
     */
    public List<TransactionEvent> getEvents() {
        return monitoringAspect.getAllEvents();
    }

    /**
     * Get at most counter events
     */
    public List<TransactionEvent> getEvents(int count) {
        return monitoringAspect.getEvents(count);
    }

    /**
     * Print timeline info.
     */
    public void printLog() {
        printTransactionTimeline();
    }

    /**
     * Print short timeline info.
     */
    public void printShortLog() {
        printShortTransactionTimeline();
    }

    /**
     * Enable transaction monitoring.
     */
    public void enable() {
        monitoringConfiguration.setEnabled(true);
    }

    /**
     * Disable transaction monitoring.
     */
    public void disable() {
        monitoringConfiguration.setEnabled(false);
    }

    /**
     * Clear all generated monitoring data.
     */
    public void clearAll() {
        monitoringAspect.resetStats();
    }

    private void printTransactionTimeline() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        List<TransactionEvent> eventsList = monitoringAspect.getAllEvents();

        List<TransactionEvent> sortedEvents = new ArrayList<>(eventsList);
        sortedEvents.sort(Comparator.comparing(TransactionEvent::getStartTime));

        int logIndent = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n=== Transaction Timeline ===", LOG_PREFIX));
        for (TransactionEvent event : sortedEvents) {
            String timestamp = sdf.format(new Date(event.getStartTime()));
            String eventType = event.getEventType().toString();

            if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.START) {
                logIndent++;
                eventType += "   ";
            } else if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.ERROR) {
                eventType += "   ";
            }

            String prefix1 = logIndent > 1 ? " ".repeat((logIndent - 1)*2 + 1) + "└>" : "";
            String prefix2 = logIndent > 1 ? " ".repeat((logIndent - 1)*2 + 3) : "";

            String message = String.format("\n    [%s]%s %s | Thread: %s (ID: %d) | Transaction: [%s] | Method: %s",
                    timestamp, prefix1, eventType, event.getThreadName(),
                    event.getThreadId(), event.getCurrentTransactionId(), event.getMethodName());

            // Informazioni aggiuntive in base al tipo di evento
            if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.COMPLETE) {
                message = String.format("%s | Execution time: %d ms", message, event.getExecutionTime());
                String jvmInfo = String.format("| CPU Time: %f ms | User Time: %f ms | Allocated Memory: %s | Total Loaded Classes: %d |",
                        event.getCpuTime(), event.getUserTime(), event.getAllocatedMemory(), event.getTotalLoadedClassCount());
                String sep = "-".repeat(jvmInfo.length());
                message = String.format("%s\n%s                                       %s", message, prefix2, sep);
                message = String.format("%s\n%s                                       %s", message, prefix2, jvmInfo);
                message = String.format("%s\n%s                                       %s", message, prefix2, sep);
                logIndent--;
            } else if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.ERROR) {
                message = String.format("%s | Exception: %s", message, event.getException().getMessage());
                logIndent--;
            }

            sb.append(message);

            // Stampa stacktrace se disponibile
            if (event.getStackTrace() != null && event.getEventType() == TransactionMonitoringAspect.TransactionEventType.START) {
                sb.append("\n    Stack trace:");
                for (StackTraceElement element : event.getStackTrace()) {
                    sb.append("\n      " + element.toString());
                }
            }
        }

        log.info(sb.toString());
    }

    private void printShortTransactionTimeline() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        List<TransactionEvent> eventsList = monitoringAspect.getAllEvents();
        synchronized(eventsList) {
            // Sort events by timestamp if necessary
            // (actually they should already be in order, but just to be safe)
            eventsList.sort(Comparator.comparing(TransactionEvent::getStartTime));

            int logIndent = 0;

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s\n=== Transaction Timeline ===", LOG_PREFIX));
            for (TransactionEvent event : eventsList) {
                String timestamp = sdf.format(new Date(event.getStartTime()));
                String eventType = "";
                switch (event.getEventType().toString()) {
                    case "START":
                        eventType = "S";
                        break;
                    case "COMPLETE":
                        eventType = "C";
                        break;
                    case "ERROR":
                        eventType = "E";
                        break;
                }

                if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.START) {
                    logIndent++;
                }

                String prefix1 = logIndent > 1 ? " ".repeat((logIndent - 1)*2 + 1) + "└>" : "";
                String prefix2 = logIndent > 1 ? " ".repeat((logIndent - 1)*2 + 3) : "";

                String message = String.format("\n    [%s]%s %s | Th: %s (ID: %d) | Tr: [%s] | Me: %s",
                        timestamp, prefix1, eventType, event.getThreadName(),
                        event.getThreadId(), event.getCurrentTransactionId(), event.getMethodName());

                // Additional information based on the type of event
                if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.COMPLETE) {
                    message = String.format("%s | Et: %d ms", message, event.getExecutionTime());
                    String jvmInfo = String.format("| CT: %f ms | UT: %f ms | AM: %s | TLC: %d |",
                            event.getCpuTime(), event.getUserTime(), event.getAllocatedMemory(), event.getTotalLoadedClassCount());
                    String sep = "-".repeat(jvmInfo.length());
                    message = String.format("%s\n%s                                %s", message, prefix2, sep);
                    message = String.format("%s\n%s                                %s", message, prefix2, jvmInfo);
                    message = String.format("%s\n%s                                %s", message, prefix2, sep);
                    logIndent--;
                } else if (event.getEventType() == TransactionMonitoringAspect.TransactionEventType.ERROR) {
                    message = String.format("%s | Et: %s", message, event.getException().getMessage());
                    logIndent--;
                }

                sb.append(message);

                // Print stacktrace if available
                if (event.getStackTrace() != null && event.getEventType() == TransactionMonitoringAspect.TransactionEventType.START) {
                    sb.append("\n    Stack trace:");
                    for (StackTraceElement element : event.getStackTrace()) {
                        sb.append("\n      " + element.toString());
                    }
                }
            }

            log.info(sb.toString());
        }
    }

    public void printStatsByThread() {
        Map<String, Map<String, long[]>> statsMap = monitoringAspect.getStatsByThread();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[STM]\n=== Transaction Statistics ==="));

        final AtomicInteger maxLength1 = new AtomicInteger();
        final AtomicInteger maxLength2 = new AtomicInteger();
        final AtomicInteger maxLength3 = new AtomicInteger();
        final AtomicInteger maxLength4 = new AtomicInteger();
        statsMap.forEach((threadNameId, subMap) -> {
            subMap.forEach((methodName, stats) -> {
                if (methodName.length() > maxLength1.get()) maxLength1.set(methodName.length());

                long totalCount = stats[0];
                long totalTime = stats[1];
                long avgTime = stats[2];

                String num1 = String.format("%d", totalCount);
                if (num1.length() > maxLength2.get()) maxLength2.set(num1.length());

                String num2 = String.format("%d", totalTime);
                if (num2.length() > maxLength3.get()) maxLength3.set(num2.length());

                String num3 = String.format("%d", avgTime);
                if (num3.length() > maxLength4.get()) maxLength4.set(num3.length());
            });
        });

        statsMap.forEach((threadNameId, subMap) -> {
            String[] tokens = threadNameId.split(";");
            sb.append(String.format("\nThread ID: %s (Name: %s)", tokens[1], tokens[0]));
            // For each method in that thread
            subMap.forEach((methodName, stats) -> {
                long totalCount = stats[0];
                long totalTime = stats[1];
                long avgTime = stats[2];

                String prefix1 = "";
                if (methodName.length() < maxLength1.get()) {
                    prefix1 = " ".repeat(maxLength1.get() - methodName.length());
                }

                String num1 = String.format("%d", totalCount);
                if (num1.length() < maxLength2.get()) {
                    String prefix2 = " ".repeat(maxLength2.get() - num1.length());
                    num1 = String.format("%s%d", prefix2, totalCount);
                }

                String num2 = String.format("%d", totalTime);
                if (num2.length() < maxLength3.get()) {
                    String prefix3 = " ".repeat(maxLength3.get() - num2.length());
                    num2 = String.format("%s%d", prefix3, totalTime);
                }

                String num3 = String.format("%d", avgTime);
                if (num3.length() < maxLength4.get()) {
                    String prefix4 = " ".repeat(maxLength4.get() - num3.length());
                    num3 = String.format("%s%d", prefix4, avgTime);
                }
                sb.append(String.format("\n    %s%s : Count = %s, Tot Time = %s ms, Avg Time = %s ms", methodName, prefix1, num1, num2, num3));
            });
        });

        if (!sb.isEmpty()) {
            log.info(sb.toString());
        }

        log.info("[STM]\n==============================");
    }

    public void printStatsByMethod() {
        // Map to hold method -> [count, totalTime]
        Map<String, long[]> statsMap = monitoringAspect.getStatsByMethod();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[STM]\n=== Aggregated Transaction Statistics By Method ==="));

        final AtomicInteger maxLength1 = new AtomicInteger();
        final AtomicInteger maxLength2 = new AtomicInteger();
        final AtomicInteger maxLength3 = new AtomicInteger();
        final AtomicInteger maxLength4 = new AtomicInteger();
        statsMap.forEach((methodName, stats) -> {
            if (methodName.length() > maxLength1.get()) maxLength1.set(methodName.length());

            long totalCount = stats[0];
            long totalTime = stats[1];
            long avgTime = stats[2];

            String num1 = String.format("%d", totalCount);
            if (num1.length() > maxLength2.get()) maxLength2.set(num1.length());

            String num2 = String.format("%d", totalTime);
            if (num2.length() > maxLength3.get()) maxLength3.set(num2.length());

            String num3 = String.format("%d", avgTime);
            if (num3.length() > maxLength4.get()) maxLength4.set(num3.length());
        });

        // Print aggregated stats
        statsMap.forEach((methodName, stats) -> {
            long totalCount = stats[0];
            long totalTime = stats[1];
            long avgTime = stats[2];
            String prefix1 = "";
            if (methodName.length() < maxLength1.get()) {
                prefix1 = " ".repeat(maxLength1.get() - methodName.length());
            }

            String num1 = String.format("%d", totalCount);
            if (num1.length() < maxLength2.get()) {
                String prefix2 = " ".repeat(maxLength2.get() - num1.length());
                num1 = String.format("%s%d", prefix2, totalCount);
            }

            String num2 = String.format("%d", totalTime);
            if (num2.length() < maxLength3.get()) {
                String prefix3 = " ".repeat(maxLength3.get() - num2.length());
                num2 = String.format("%s%d", prefix3, totalTime);
            }

            String num3 = String.format("%d", avgTime);
            if (num3.length() < maxLength4.get()) {
                String prefix4 = " ".repeat(maxLength4.get() - num3.length());
                num3 = String.format("%s%d", prefix4, avgTime);
            }
            sb.append(String.format("\n    %s%s : Tot Count = %s, Tot Time = %s ms, Overall Avg Time = %s ms", methodName, prefix1, num1, num2, num3));
        });

        if (!sb.isEmpty()) {
            log.info(sb.toString());
        }
    }
}
