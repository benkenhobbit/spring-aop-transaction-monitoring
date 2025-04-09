# Spring Boot Transaction Monitoring

A lightweight transaction monitoring system for Spring Boot applications that provides detailed performance metrics and
specific JVM statistics.

## Overview

This project provides an aspect-oriented approach to monitor Spring's `@Transactional` methods, collecting valuable
metrics like:

- Execution time
- CPU and user time consumption per thread
- Memory allocation per thread
- Transaction nesting information

The monitoring system makes it easy to track and analyze transaction performance in your Spring Boot applications with
minimal configuration.

## Features

- **Non-invasive monitoring**: Works with standard Spring `@Transactional` annotations
- **Detailed metrics**: Captures comprehensive performance data for each transaction
- **Transaction correlation**: Tracks nested transactions with correlation IDs
- **JVM metrics**:
    - Thread CPU time
    - Thread user time
    - Thread memory allocation
    - Class loading information
- **Configurable retention**: Control how much historical data to keep
- **Event logging**: Chronological log of transaction events (start, complete, error)
- **Statistical analysis**: Aggregate statistics by thread or method
- **Built-in test suite**: Includes sample controller and service classes to demonstrate functionality

## How to Use This Project

### Option 1: Clone and Integrate

1. Clone this repository:
   ```
   git clone https://github.com/benkenhobbit/spring-aop-transaction-monitoring.git
   ```

2. Copy the necessary files into your project:
    - `TransactionMonitoringAspect.java`
    - `TransactionMonitoringConfiguration.java`
    - `TransactionEvent.java`
    - `TransactionThreadStats.java`

3. Ensure these classes maintain their package structure or update the package declarations as needed.

### Option 2: Fork and Customize

Fork this repository to customize it for your specific needs while keeping the ability to pull future updates.

## Just Run (don't tell me more)

```shell
./gradlew bootRun
```

## Quick Start (but this time tell me more)

1. Generally you should add @EnableAspectJAutoProxy in "traditional" Spring projects (not in newer versions of Spring
   Boot) or in specific situations where you want to have explicit control over the AOP configuration or when using
   advanced features like CGLIB proxies.

2. Configure the monitoring in your `application.properties` or `application.yml`:

```properties  
# Enable transaction monitoring  
app.monitoring.transaction.enabled=true  
# Configure log pruning settings  
app.monitoring.transaction.log-pruning.enabled=true  
app.monitoring.transaction.log-pruning.max-event-log.size=1000  
app.monitoring.transaction.log-pruning.max-event-log.time=24  
```  

or with YAML:

```yaml  
app:
  monitoring:
    transaction:
      enabled:
        true log-pruning:
          enabled:
            true log-pruning:
              max-event-log:
                size:
                  1000 time: 24  
```  

## Configuration Properties

| Property                                                    | Description                                  | Default |  
|-------------------------------------------------------------|----------------------------------------------|---------|  
| `app.monitoring.transaction.enabled`                        | Enable/disable transaction monitoring        | `false` |  
| `app.monitoring.transaction.log-pruning.enabled`            | Enable/disable automatic log pruning         | `true`  |  
| `app.monitoring.transaction.log-pruning.max-event-log.size` | Maximum number of transaction events to keep | `1000`  |  
| `app.monitoring.transaction.log-pruning.max-event-log.time` | Maximum age of events to keep (in hours)     | `24`    |  

## Usage Examples

### Basic Usage

The monitoring system automatically monitors all methods annotated with Spring's `@Transactional`. No additional code is
required for monitoring to work.

## Test and Demo Classes

The project includes test classes (in `com.github.benkenhobbit.monitoring.demo` package) that demonstrate how to use the
transaction monitoring system and verify its functionality:

- `demo/rest/MonitorController.java`
- `demo/service/MonitorService.java`
- `demo/service/NestedMonitorService.java`
- `demo/model/Instrument.java`
- `demo/controller/DatabaseInterface.java`

### MonitorController

A REST controller that provides endpoints for testing and interacting with the monitoring system:

```java  

@RestController
@RequestMapping("private/transaction-monitor")
public class MonitorController {  
  ...
}  
```  

**Notice: For security reasons, it is recommended to use a protected path in a production environment.**

MonitorController could also expose all the methods to change TransactionMonitoringConfiguration parameters like for
example:

```java

@GetMapping("/enable")
public ResponseEntity<Void> enable() {
    transactionMonitorService.enable();
    return ResponseEntity.ok().build(); // Returns 200 OK
}
```

that enables transaction monitoring (dafault value is: *false*).

These test classes make it easy to see the monitoring system in action and understand how to integrate it into your own
application.

## How It Works

The project uses Spring AOP to intercept calls to `@Transactional` methods. For each transaction:

1. Before execution: Creates a transaction ID, records start time with method name and captures current thread details (
   thread id, thread name)
2. During execution: The method proceeds normally
3. After execution: Records completion time, captures final JVM metrics, and calculates resource usage
4. On error: Records exception information for troubleshooting

### JVM Metrics Collection

The aspect collects the following JVM metrics for each transaction:

```java  
// Get class loading information  
...
// Get thread information using standard and extended ThreadMXBean  
        ...
// Collect CPU time metrics when supported  
        ...
// Collect memory allocation per thread (using Sun's extension)  
```  

## Project Structure

### Main Components

- **TransactionMonitoringAspect**: The core aspect that intercepts transaction calls and collects metrics
- **TransactionMonitoringConfiguration**: Configuration class for monitoring settings
- **TransactionEvent**: Model class representing a transaction event
- **TransactionThreadStats**: Model class for maintaining transaction statistics

### Data Structures

#### TransactionEvent

Represents a single transaction event with:

- Transaction ID
- Thread ID
- Thread name
- Method name
- Start time
- Execution time
- Event type (START, COMPLETE, ERROR)
- Stack trace
- Total loaded class count
- CPU time (in milliseconds)
- User time (in milliseconds)
- Allocated memory (formatted as human-readable string)
- Exception details (if applicable)

#### TransactionThreadStats

Maintains running statistics for transactions:

- Count of transactions
- Total execution times

## Advanced Usage

### Pruning Old Data

The monitoring system automatically prunes old data based on your configuration settings. You can also manually trigger
pruning:

```java  
monitoringAspect.pruneEventLog();  
```  

### Custom Time Range Analysis

```java  
// Get events between timestamps  
long startTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
long endTime = System.currentTimeMillis();
List<TransactionEvent> events = monitoringAspect.getEventsInTimeRange(startTime, endTime);
```  
## Transactions Logs Example

Here is an example of the logs generated by the demo classes:

```text
=== Transaction Timeline (Long) ===
    [2025-01-09 20:05:37.655] START    | Thread: task-1 (ID: 34) | Transaction: [7b076004-60b7-4da7-a157-81a66abd373a] | Method: c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported
    [2025-01-09 20:05:37.656]   └> START    | Thread: task-1 (ID: 34) | Transaction: [7b076004-60b7-4da7-a157-81a66abd373a] | Method: c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported
    [2025-01-09 20:05:43.027]   └> COMPLETE | Thread: task-1 (ID: 34) | Transaction: [7b076004-60b7-4da7-a157-81a66abd373a] | Method: c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported | Execution time: 5371 ms
                                            ------------------------------------------------------------------------------------------------------------
                                            | CPU Time: 2,092000 ms | User Time: 1,914000 ms | Allocated Memory: 38,24 KB | Total Loaded Classes: 7632 |
                                            ------------------------------------------------------------------------------------------------------------
    [2025-01-09 20:05:43.030] COMPLETE | Thread: task-1 (ID: 34) | Transaction: [7b076004-60b7-4da7-a157-81a66abd373a] | Method: c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported | Execution time: 5375 ms
                                       ------------------------------------------------------------------------------------------------------------
                                       | CPU Time: 4,249000 ms | User Time: 3,835000 ms | Allocated Memory: 55,94 KB | Total Loaded Classes: 7633 |
                                       ------------------------------------------------------------------------------------------------------------
```

```text
=== Transaction Timeline (Short) ===
    [2025-01-09 20:05:37.655] S | Th: task-1 (ID: 34) | Tr: [7b076004-60b7-4da7-a157-81a66abd373a] | Me: c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported
    [2025-01-09 20:05:37.656]   └> S | Th: task-1 (ID: 34) | Tr: [7b076004-60b7-4da7-a157-81a66abd373a] | Me: c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported
    [2025-01-09 20:05:43.027]   └> C | Th: task-1 (ID: 34) | Tr: [7b076004-60b7-4da7-a157-81a66abd373a] | Me: c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported | Et: 5371 ms
                                     ----------------------------------------------------------------
                                     | CT: 2,092000 ms | UT: 1,914000 ms | AM: 38,24 KB | TLC: 7632 |
                                     ----------------------------------------------------------------
    [2025-01-09 20:05:43.030] C | Th: task-1 (ID: 34) | Tr: [7b076004-60b7-4da7-a157-81a66abd373a] | Me: c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported | Et: 5375 ms
                                ----------------------------------------------------------------
                                | CT: 4,249000 ms | UT: 3,835000 ms | AM: 55,94 KB | TLC: 7633 |
                                ----------------------------------------------------------------
```

```text
=== Transaction Statistics By Thread ===
Thread ID: 34 (Name: task-1)
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported     : Count = 1, Tot Time = 5375 ms, Avg Time = 5375 ms
    c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported              : Count = 1, Tot Time = 5371 ms, Avg Time = 5371 ms
Thread ID: 21 (Name: http-nio-8080-exec-1)
    c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported              : Count = 6, Tot Time = 1709 ms, Avg Time =  284 ms
    c.g.b.m.d.c.DatabaseInterface.getInstrumentRequired                  : Count = 5, Tot Time = 1363 ms, Avg Time =  272 ms
    c.g.b.m.d.s.NestedMonitorService.getInstrumentNestedRequiresNew      : Count = 1, Tot Time =  406 ms, Avg Time =  406 ms
Thread ID: 37 (Name: task-3)
    c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported              : Count = 1, Tot Time = 5342 ms, Avg Time = 5342 ms
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsRequired         : Count = 1, Tot Time = 5344 ms, Avg Time = 5344 ms
Thread ID: 36 (Name: task-2)
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentNestedRequiresNew : Count = 1, Tot Time = 5404 ms, Avg Time = 5404 ms
    c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported              : Count = 1, Tot Time = 5402 ms, Avg Time = 5402 ms
    ==============================
```

```text
=== Aggregated Transaction Statistics By Method ===
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentNestedRequiresNew : Tot Count = 1, Tot Time =  5404 ms, Overall Avg Time = 5404 ms
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsNotSupported     : Tot Count = 1, Tot Time =  5375 ms, Overall Avg Time = 5375 ms
    c.g.b.m.d.c.DatabaseInterface.getInstrumentNotSupported              : Tot Count = 9, Tot Time = 17824 ms, Overall Avg Time = 1980 ms
    c.g.b.m.d.c.DatabaseInterface.getInstrumentRequired                  : Tot Count = 5, Tot Time =  1363 ms, Overall Avg Time =  272 ms
    c.g.b.m.d.s.NestedMonitorService.getAsyncInstrumentsRequired         : Tot Count = 1, Tot Time =  5344 ms, Overall Avg Time = 5344 ms
    c.g.b.m.d.s.NestedMonitorService.getInstrumentNestedRequiresNew      : Tot Count = 1, Tot Time =   406 ms, Overall Avg Time =  406 ms
    ==============================
```
## Integration Ideas

This is a simple yet effective monitoring system that requires no configuration or integration with external systems.
It's particularly valuable in complex systems that integrate these technologies, providing a convenient way to:

- Study the relationships and dependencies between transactions that access data
- Analyze the performance of these transactions
- Quickly identify potential bugs or critical issues in data access patterns

While the system works great as a standalone solution, you could extend its capabilities by:

- Connecting to monitoring systems like Prometheus
- Visualizing transaction data with Grafana dashboards
- Setting up alerts for slow transactions or excessive resource usage
- Building a custom admin panel for transaction analysis

## Requirements

- Java 17 or higher
- Spring Boot 2.x or higher
- Spring AOP
- Access to com.sun.management classes for extended thread metrics
- Lombok (for the configuration class)

## Note on JVM Compatibility

This project uses `com.sun.management.ThreadMXBean` to access extended thread metrics like memory allocation. These are
Sun/Oracle JVM-specific extensions and may not be available on all JVM implementations. The code includes fallback
handling for environments where these extensions are not available.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the BSD 2-Clause License - see the LICENSE file for details.

## Author

Created by A. Aquila (GitHub: benkenhobbit)

## Acknowledgments

- Inspired by the need for detailed transaction monitoring in Spring Boot applications
- Thanks to Spring's AOP capabilities that make this monitoring possible