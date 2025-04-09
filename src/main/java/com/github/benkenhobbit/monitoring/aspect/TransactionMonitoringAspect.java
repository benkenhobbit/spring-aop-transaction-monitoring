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
package com.github.benkenhobbit.monitoring.aspect;

import com.github.benkenhobbit.monitoring.config.TransactionMonitoringConfiguration;
import com.github.benkenhobbit.monitoring.model.TransactionEvent;
import com.github.benkenhobbit.monitoring.model.TransactionThreadStats;
import com.sun.management.ThreadMXBean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Transaction monitoring aspect.
 *
 * @author A. Aquila
 */
@Aspect
@Component
public class TransactionMonitoringAspect {

    /**
     * Enum for event types.
     */
    public enum TransactionEventType {
        START, COMPLETE, ERROR
    }

    private static final String TRANSACTION_ID_RESOURCE_KEY = "TRANSACTION_CORRELATION_ID";

    /**
     * Structure for storing transactions in chronological order.
     */
    private final List<TransactionEvent> transactionEventLog = Collections.synchronizedList(new ArrayList<>());

    /**
     * Map structure: ThreadId -> (MethodName -> Stats).
     */
    private final Map<String, Map<String, TransactionThreadStats>> threadTransactionStats = new ConcurrentHashMap<>();

    /**
     * Manages transaction monitor settings.
     */
    private final TransactionMonitoringConfiguration transactionMonitoringConfiguration;

    /**
     * Constructor injection.
     *
     * @param transactionMonitoringConfiguration
     */
    public TransactionMonitoringAspect(TransactionMonitoringConfiguration transactionMonitoringConfiguration) {
        this.transactionMonitoringConfiguration = transactionMonitoringConfiguration;
    }

    /**
     * This is executed whenever a method with the indicated annotations is called.
     *
     * @param joinPoint gives us the power to control the flow of the code and decide how to proceed with further invocations.
     * @return Object
     * @throws Throwable
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitorTransactionalMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!transactionMonitoringConfiguration.isEnabled()) {
            return joinPoint.proceed();
        }

        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        Signature signature = joinPoint.getSignature();

        // Generate a unique ID only if one does not already exist for this transaction
        if (!TransactionSynchronizationManager.hasResource(TRANSACTION_ID_RESOURCE_KEY)) {
            String transactionId = generateTransactionId();
            TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_KEY, transactionId);
        }

        String currentTransactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_KEY);
        String methodName = getMethodName(signature);

        // Increment the counter for this thread and method
        String threadKey = getThreadName(threadId) + ";" + threadId;
        threadTransactionStats
                .computeIfAbsent(threadKey, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(methodName, k -> new TransactionThreadStats())
                .getCounter()
                .incrementAndGet();

        // Event start and creation timestamp
        long startTime = System.currentTimeMillis();
        TransactionEvent event = new TransactionEvent();
        event.setCurrentTransactionId(currentTransactionId);
        event.setThreadId(threadId);
        event.setThreadName(threadName);
        event.setMethodName(methodName);
        event.setStartTime(startTime);

//        // Capture also stack trace if enabled (it gives us unnecessary secondary information).
//        if (transactionMonitoringConfiguration.isStackTraceEnabled()) {
//            StackTraceElement[] fullStack = Thread.currentThread().getStackTrace();
//            // Skip first elements which concern this aspect and the AOP mechanism
//            int startIndex = 3;
//            int endIndex = Math.min(fullStack.length, startIndex + transactionMonitoringConfiguration.getStackTraceDepth());
//            event.setStackTrace(Arrays.copyOfRange(fullStack, startIndex, endIndex));
//        }

        try {
            // Add event to log before execution
            event.setEventType(TransactionEventType.START);
            transactionEventLog.add(event);

            return joinPoint.proceed();
        } catch (Exception e) {
            // Error event
            TransactionEvent errorEvent = new TransactionEvent();
            errorEvent.setCurrentTransactionId(currentTransactionId);
            errorEvent.setThreadId(threadId);
            errorEvent.setThreadName(threadName);
            errorEvent.setMethodName(methodName);
            errorEvent.setStartTime(System.currentTimeMillis());
            errorEvent.setEventType(TransactionEventType.ERROR);
            errorEvent.setException(e);
            transactionEventLog.add(errorEvent);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Update time statistics
            threadTransactionStats
                    .computeIfAbsent(threadKey, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(methodName, k -> new TransactionThreadStats())
                    .getTimes()
                    .addAndGet(executionTime);

            // Completion event
            TransactionEvent completeEvent = new TransactionEvent();

            // Add JVM info
            ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
            java.lang.management.ThreadMXBean standardThreadBean = ManagementFactory.getThreadMXBean();
            ThreadMXBean extendedThreadBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
            if (standardThreadBean.isThreadCpuTimeSupported()) {
                long cpuTime = standardThreadBean.getThreadCpuTime(threadId);
                long userTime = standardThreadBean.getThreadUserTime(threadId);
                completeEvent.setCpuTime(((float) cpuTime / 1_000_000));
                completeEvent.setUserTime(((float) userTime / 1_000_000));
            }

            // CPU Usage per Thread (com.sun.management extension)
            try {
                long[] threadAllocatedBytes = extendedThreadBean.getThreadAllocatedBytes(new long[]{ threadId });
                if (threadAllocatedBytes != null && threadAllocatedBytes.length > 0) {
                    completeEvent.setAllocatedMemory(formatBytes(threadAllocatedBytes[0]));
                }
            } catch (Exception e) {
            }

            completeEvent.setCurrentTransactionId(currentTransactionId);
            completeEvent.setThreadId(threadId);
            completeEvent.setThreadName(threadName);
            completeEvent.setMethodName(methodName);
            completeEvent.setStartTime(endTime);
            completeEvent.setEventType(TransactionEventType.COMPLETE);
            completeEvent.setExecutionTime(executionTime);
            completeEvent.setTotalLoadedClassCount(classLoadingBean.getTotalLoadedClassCount());
            transactionEventLog.add(completeEvent);

            if (!isNestedTransaction()) {
                // Register a listener for transaction completion (old version)
                //registerTransactionCompletionCallback(currentTransactionId);

                // Remove transaction ID at the end
                // For the monitoring operation it is sufficient to do it directly here
                TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_ID_RESOURCE_KEY);
            }

            if (transactionMonitoringConfiguration.isLogPruningEnabled()) {
                pruneEventLog();
            }
        }
    }

    /**
     * Shrinks the package name (this makes logs more readable).
     *
     * @param signature of the method.
     * @return shrunk signature of the method.
     */
    private String getMethodName(Signature signature) {
        String declaringTypeName = signature.getDeclaringTypeName();
        StringBuilder sb = new StringBuilder();
        if (declaringTypeName.contains(".")) {
            String[] tokens = declaringTypeName.split("\\.");
            for (int i = 0; i < tokens.length; i++) {
                if (i < tokens.length - 1) {
                    sb.append(tokens[i].charAt(0));
                    sb.append(".");
                } else {
                    sb.append(tokens[i]);
                }
            }
        }
        return sb + "." + signature.getName();
    }

    /**
     * Create a random id.
     *
     * @return a randomly generated <i>UUID</i> as <i>String</i>.
     */
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Implement logic to determine if it is a nested transaction.
     *
     * @return true if the transaction is nested, false otherwise.
     */
    private boolean isNestedTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive() &&
                TransactionSynchronizationManager.getCurrentTransactionName() != null;
    }

    /**
     * Old version, used to register transaction completion callback (afterCompletion).
     */
//    private void registerTransactionCompletionCallback(String transactionId) {
//        TransactionSynchronizationManager.registerSynchronization(new TransactionMonitoringAspect.TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCompletion(int status) {
//                log.info(":::::: afterCompletion ::::::");
//                // Remove transaction ID at the end
//                TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_ID_RESOURCE_KEY);
//            }
//        });
//    }

    /**
     * @Deprecated
     * Class for compatibility with older versions of Spring
     */
//    private abstract static class TransactionSynchronizationAdapter
//            implements org.springframework.transaction.support.TransactionSynchronization {
//        // Implement only the necessary methods
//        public void beforeCommit(boolean readOnly) {}
//        public void beforeCompletion() {}
//        public void afterCommit() {}
//        public void suspend() {}
//        public void resume() {}
//        public void flush() {}
//    }

    /**
     * Get thread name from ID.
     *
     * @param threadId key to get thread name from all stack traces.
     * @return thread name.
     */
    private String getThreadName(long threadId) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getId() == threadId) {
                return thread.getName();
            }
        }
        return "Unknown";
    }

    /**
     * Format bytes with unit of measurement.
     *
     * @param bytes to be measured.
     * @return bytes with the unit of measure.
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Keep only the last <i>transactionMonitoringConfiguration.getMaxEventLogSize()</i> events or
     * events from the last <i>transactionMonitoringConfiguration.getMaxEventLogTime()</i> hours.
     */
    public void pruneEventLog() {
        synchronized(transactionEventLog) {
            long timeExceed = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(transactionMonitoringConfiguration.getMaxEventLogTime());

            int size = transactionMonitoringConfiguration.getMaxEventLogSize();
            if (transactionEventLog.size() > size) {
                int limit = transactionEventLog.size() - size;
                transactionEventLog.subList(0, limit).clear();
            } else {
                transactionEventLog.removeIf(event -> event.getStartTime() < timeExceed);
            }
        }
    }

    /**
     * Get all events.
     *
     * @return all events without filters.
     */
    public List<TransactionEvent> getAllEvents() {
        synchronized(transactionEventLog) {
            return new ArrayList<>(transactionEventLog);
        }
    }

    /**
     * Get at most counter events.
     *
     * @param counter maximum number of events to retrieve.
     * @return counter events.
     */
    public List<TransactionEvent> getEvents(int counter) {
        synchronized(transactionEventLog) {
            return transactionEventLog.stream()
                    .limit(counter)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get events for a specific period.
     *
     * @param startTime start time of range.
     * @param endTime end time of range.
     * @return the list of events of the selected period.
     */
    public List<TransactionEvent> getEventsInTimeRange(long startTime, long endTime) {
        synchronized(transactionEventLog) {
            return transactionEventLog.stream()
                    .filter(event -> event.getStartTime() >= startTime && event.getStartTime() <= endTime)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Returns aggregate statistics by thread.
     * NB: if there is only one thread the method statistics and thread statistics are the same.
     *
     * @return aggregatedThreadStats.
     */
    public Map<String, Map<String, long[]>> getStatsByThread() {
        // Map to hold thread -> [count, totalTime]
        Map<String, Map<String, long[]>> aggregatedThreadStats = new HashMap<>();

        // Aggregate counts from all threads
        threadTransactionStats.forEach((threadKey, mapStats) -> {
            mapStats.forEach((methodName, stats) -> {
                Map<String, long[]> threadStats = aggregatedThreadStats.computeIfAbsent(threadKey, k -> new HashMap<>());
                threadStats.computeIfAbsent(methodName, k -> new long[3])[0] += stats.getCounter().get();
                threadStats.computeIfAbsent(methodName, k -> new long[3])[1] += stats.getTimes().get();
                long totalCount = threadStats.computeIfAbsent(methodName, k -> new long[3])[0];
                long totalTime = threadStats.computeIfAbsent(methodName, k -> new long[3])[1];
                long avgTime = totalCount > 0 ? totalTime / totalCount : 0;
                threadStats.computeIfAbsent(methodName, k -> new long[3])[2] = avgTime;
            });
        });

        return aggregatedThreadStats;
    }

    /**
     * Returns aggregate statistics by method.
     * NB: if there is only one thread the method statistics and thread statistics are the same.
     *
     * @return aggregatedMethodStats.
     */
    public Map<String, long[]> getStatsByMethod() {
        // Map to hold method -> [count, totalTime]
        Map<String, long[]> aggregatedMethodStats = new HashMap<>();

        // Aggregate counts from all threads
        threadTransactionStats.forEach((threadId, mapStats) -> {
            mapStats.forEach((methodName, stats) -> {
                aggregatedMethodStats.computeIfAbsent(methodName, k -> new long[3])[0] += stats.getCounter().get();
                aggregatedMethodStats.computeIfAbsent(methodName, k -> new long[3])[1] += stats.getTimes().get();
                long totalCount = aggregatedMethodStats.computeIfAbsent(methodName, k -> new long[3])[0];
                long totalTime = aggregatedMethodStats.computeIfAbsent(methodName, k -> new long[3])[1];
                long avgTime = totalCount > 0 ? totalTime / totalCount : 0;
                aggregatedMethodStats.computeIfAbsent(methodName, k -> new long[3])[2] = avgTime;
            });
        });

        return aggregatedMethodStats;
    }


    /**
     * Reset statistics if needed.
     */
    public void resetStats() {
        transactionEventLog.clear();
        threadTransactionStats.clear();
    }
}
