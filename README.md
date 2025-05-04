# CDAP Wrangler: Byte Size and Time Duration Support

## Overview

Added native support for parsing and utilizing byte size and time duration units within CDAP Wrangler recipes.

## Features

### 1. ByteSize Parser
- **Units**: B (Bytes), KB (Kilobytes), MB (Megabytes), GB (Gigabytes), TB (Terabytes)
- **Examples**: `"10KB"`, `"1.5MB"`, `"2GB"`

### 2. TimeDuration Parser
- **Units**: s (seconds), min (minutes), h (hours), d (days)
- **Examples**: `"30s"`, `"5min"`, `"2.5h"`

### 3. Aggregate-Stats Directive

#### Syntax
```text
aggregate-stats :source_size_col :source_time_col target_size_col target_time_col 'sizeUnit' 'timeUnit' 'aggregationType'
```

#### Parameters

| Parameter | Description |
|-----------|-------------|
| source_size_col | Column with byte sizes |
| source_time_col | Column with time durations |
| target_size_col | Output column for aggregated size |
| target_time_col | Output column for aggregated time |
| sizeUnit | Output unit (B, KB, MB, GB, TB) |
| timeUnit | Output unit (s, min, h, d) |
| aggregationType | Aggregation type (total, average) |

## Usage Examples

### Total Aggregation
```text
aggregate-stats :data_size :response_time total_size_mb total_time_sec 'MB' 's' 'total'
```

### Average Calculation
```text
aggregate-stats :transfer_size :process_time avg_size_gb avg_time_min 'GB' 'min' 'average'
```

## Implementation Details

### Core Components

1. **ByteSize Parser**
```java
@PublicEvolving
public class ByteSize implements Token {
    // Handles byte size parsing and unit conversion
}
```

2. **TimeDuration Parser**
```java
@PublicEvolving
public class TimeDuration implements Token {
    // Manages time duration parsing and conversion
}
```

3. **Aggregation Directive**
```java
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
public class AggregationDirective implements Directive {
    // Implements aggregation logic
}
```

## Testing

### Run Tests
```bash
# Build and test all modules
mvn clean install

# Run specific test class
mvn test -Dtest=AggregationDirectiveTest
```

### Test Coverage
- Unit tests for parsers
- Integration tests
- Grammar validation tests
- Edge case handling

## Building

### Prerequisites
- Java 8+
- Maven 3.6+
- Git



