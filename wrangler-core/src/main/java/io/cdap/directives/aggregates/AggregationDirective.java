/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package io.cdap.directives.aggregates;

 import io.cdap.cdap.api.annotation.*;
import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.*;

 import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Directive to aggregate byte size and time duration fields.
 */

 @Plugin(type = Directive.TYPE)
 @Name("aggregate-directive")
 @Categories(categories = {"aggregates"})
 @Description("Aggregates total byte size and total or average time duration across rows.")
 public class AggregationDirective implements Directive {

     private String sizeInputCol;
     private String timeInputCol;
     private String sizeOutputCol;
     private String timeOutputCol;
     private String sizeUnit;
     private String timeUnit;
     private String aggregationType;

     @Override
     public UsageDefinition define() {
         UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-directive");

         builder.define("sizeInputCol", TokenType.BYTE_SIZE);
         builder.define("timeInputCol", TokenType.TIME_DURATION);
         builder.define("sizeOutputCol", TokenType.BYTE_SIZE);
         builder.define("timeOutputCol", TokenType.TIME_DURATION);
         builder.define("sizeUnit", TokenType.TEXT);
         builder.define("timeUnit", TokenType.TEXT);
         builder.define("aggregationType", TokenType.TEXT);

         return builder.build();
     }

     @Override
     public void initialize(Arguments arguments) throws DirectiveParseException {
         this.sizeInputCol = ((ColumnName) arguments.value("sizeInputCol")).value();
         this.timeInputCol = ((ColumnName) arguments.value("timeInputCol")).value();
         this.sizeOutputCol = ((ColumnName) arguments.value("sizeOutputCol")).value();
         this.timeOutputCol = ((ColumnName) arguments.value("timeOutputCol")).value();
         this.sizeUnit = ((Text) arguments.value("sizeUnit")).value();
         final Set<String> VALIDSIZEUNITS = Set.of("mb", "gb", "tb","b","kb");
         if (!VALIDSIZEUNITS.contains(sizeUnit.toLowerCase())) {
            throw new DirectiveParseException("Invalid size unit: " + sizeUnit);
        }
         this.timeUnit = ((Text) arguments.value("timeUnit")).value();
         final Set<String> VALIDTIMEUNITS = Set.of("s", "min", "h","d","m","y","w");
         if (!VALIDTIMEUNITS.contains(timeUnit.toLowerCase())) {
            throw new DirectiveParseException("Invalid time unit: " + timeUnit);
        }
         this.aggregationType = ((Text) arguments.value("aggregationType")).value();
     }

     @Override
     public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
         // Retrieve TransientStore instance from context
         TransientStore store = context.getTransientStore();

         // Retrieve stored values with default handling
         Double storedTotalBytes = store.get("totalBytes");
         Double storedTotalSeconds = store.get("totalSeconds");
         Long storedRowCount = store.get("rowCount");

         // Initialize the values if they are not present
         if (storedTotalBytes == null) storedTotalBytes = 0.0;
         if (storedTotalSeconds == null) storedTotalSeconds = 0.0;
         if (storedRowCount == null) storedRowCount = 0L;

         // Aggregate over the current batch of rows
         for (Row row : rows) {
             Object sizeVal = row.getValue(sizeInputCol);
             Object timeVal = row.getValue(timeInputCol);

             if (sizeVal == null || timeVal == null) {
                continue;  // Skip rows with null values
            }


             if (sizeVal instanceof String && timeVal instanceof String) {
                 try {
                     double bytes = new ByteSize((String) sizeVal).getBytes();
                     double seconds = new TimeDuration((String) timeVal).getSeconds();
                     storedTotalBytes += bytes;
                     storedTotalSeconds += seconds;
                     storedRowCount++;
                 } catch (Exception e) {
                     throw new DirectiveExecutionException(
                        "Error parsing size/time values", e);
                 }
             }
         }

         // Update the transient store with aggregated values
         store.set(TransientVariableScope.LOCAL, "totalBytes", storedTotalBytes);
         store.set(TransientVariableScope.LOCAL, "totalSeconds", storedTotalSeconds);
         store.set(TransientVariableScope.LOCAL, "rowCount", storedRowCount);

         // If this is not the last call, return an empty list (no output yet)
         if (rows.isEmpty()) {
             return new ArrayList<>();
         }

         // Compute final values (e.g., average)
         if ("average".equalsIgnoreCase(aggregationType) && storedRowCount > 0) {
             storedTotalBytes /= storedRowCount;
             storedTotalSeconds /= storedRowCount;
         }

         // Convert byte size and time duration to the desired units
         double finalSize = convertByteSize(storedTotalBytes);
         double finalTime = convertTimeDuration(storedTotalSeconds);

         // Build the result row
         Row resultRow = new Row();
         resultRow.add(sizeOutputCol, finalSize);
         resultRow.add(timeOutputCol, finalTime);

         List<Row> result = new ArrayList<>();
         result.add(resultRow);
         return result;
     }

     @Override
     public void destroy() {
         // No cleanup necessary in this case
     }

     // Method to convert byte size to desired units
     private double convertByteSize(double byteSize) {
         switch (sizeUnit.toLowerCase()) {
             case "mb":
                 return byteSize / (1024.0 * 1024.0);  // Convert to MB
             case "gb":
                 return byteSize / (1024.0 * 1024.0 * 1024.0);  // Convert to GB
             case "tb":
                 return byteSize / (1024.0 * 1024.0 * 1024.0 * 1024.0);  // Convert to TB
             default:
                 return byteSize;  // Default: Return in bytes
         }
     }

     // Method to convert time duration to desired units
     private double convertTimeDuration(double time) {
         switch (timeUnit.toLowerCase()) {
             case "d":
                 return time / (3600.0 * 24.0);  // Convert to days
             case "min":
                 return time / 60.0;  // Convert to minutes
             case "h":
                 return time / 3600.0;  // Convert to hours
             default:
                 return time;  // Default: Return in seconds
         }
     }
 }
