

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

import io.cdap.wrangler.TestingPipelineContext;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class AggregationDirectiveTest {

    @Test
    public void testUsageDefinition() {
        AggregationDirective directive = new AggregationDirective();
        UsageDefinition definition = directive.define();
        Assert.assertNotNull(definition);
        Assert.assertEquals(7, definition.getTokens().size());
        Assert.assertEquals(TokenType.BYTE_SIZE, definition.getTokens().get(0).type());
        Assert.assertEquals(TokenType.TIME_DURATION, definition.getTokens().get(1).type());
        Assert.assertEquals(TokenType.BYTE_SIZE, definition.getTokens().get(2).type());
        Assert.assertEquals(TokenType.TIME_DURATION, definition.getTokens().get(3).type());
        Assert.assertEquals(TokenType.TEXT, definition.getTokens().get(4).type());
        Assert.assertEquals(TokenType.TEXT, definition.getTokens().get(5).type());
        Assert.assertEquals(TokenType.TEXT, definition.getTokens().get(6).type());
    }

    @Test
    public void testTotalAggregation() throws Exception {
        AggregationDirective directive = new AggregationDirective();
        Map<String, Object> args = new HashMap<>();
        args.put("sizeInputCol", new ColumnName("size"));
        args.put("timeInputCol", new ColumnName("time"));
        args.put("sizeOutputCol", new ColumnName("total_size"));
        args.put("timeOutputCol", new ColumnName("total_time"));
        args.put("sizeUnit", new Text("B"));
        args.put("timeUnit", new Text("s"));
        args.put("aggregationType", new Text("total"));

        directive.initialize(new DirectiveArgumentsTest(args));

        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", "5s"),
            new Row("size", "500B").add("time", "10s"),
            new Row("size", "2MB").add("time", "15s")
        );

        ExecutorContext context = new TestingPipelineContext();
        List<Row> resultRows = directive.execute(rows, context);

        Assert.assertEquals(1, resultRows.size());
        Row result = resultRows.get(0);

        double totalB = (1024 + 500 + 2 * 1024 * 1024);
        Assert.assertEquals(totalB, ((Number) result.getValue("total_size")).doubleValue(), 0.0001);
        Assert.assertEquals(30.0, ((Number) result.getValue("total_time")).doubleValue(), 0.0001);
    }

    @Test
    public void testAverageAggregation() throws Exception {
        AggregationDirective directive = new AggregationDirective();
        Map<String, Object> args = new HashMap<>();
        args.put("sizeInputCol", new ColumnName("size"));
        args.put("timeInputCol", new ColumnName("time"));
        args.put("sizeOutputCol", new ColumnName("avg_size"));
        args.put("timeOutputCol", new ColumnName("avg_time"));
        args.put("sizeUnit", new Text("KB"));
        args.put("timeUnit", new Text("s"));
        args.put("aggregationType", new Text("average"));

        directive.initialize(new DirectiveArgumentsTest(args));

        List<Row> rows = Arrays.asList(
            new Row("size", "1KB").add("time", "5s"),
            new Row("size", "500B").add("time", "10s"),
            new Row("size", "2MB").add("time", "15s")
        );

        ExecutorContext context = new TestingPipelineContext();
        List<Row> ResultRows = directive.execute(rows, context);

        Assert.assertEquals(1, ResultRows.size());
        Row result = ResultRows.get(0);

        double totalB = (1024 + 500 + 2 * 1024 * 1024);
        double avgB = totalB / 3;
        Assert.assertEquals(avgB, ((Number) result.getValue("avg_size")).doubleValue(), 0.01);
        Assert.assertEquals(10.0, ((Number) result.getValue("avg_time")).doubleValue(), 0.0001);
    }

    @Test(expected = DirectiveParseException.class)
    public void testInvalidSizeUnit() throws Exception {
        AggregationDirective directive = new AggregationDirective();
        Map<String, Object> args = new HashMap<>();
        args.put("sizeInputCol", new ColumnName("size"));
        args.put("timeInputCol", new ColumnName("time"));
        args.put("sizeOutputCol", new ColumnName("total_size"));
        args.put("timeOutputCol", new ColumnName("total_time"));
        args.put("sizeUnit", new Text("XB")); // Invalid unit
        args.put("timeUnit", new Text("s"));
        args.put("aggregationType", new Text("total"));

        directive.initialize(new DirectiveArgumentsTest(args));
    }

    @Test(expected = DirectiveParseException.class)
    public void testInvalidTimeUnit() throws Exception {
        AggregationDirective directive = new AggregationDirective();
        Map<String, Object> args = new HashMap<>();
        args.put("sizeInputCol", new ColumnName("size"));
        args.put("timeInputCol", new ColumnName("time"));
        args.put("sizeOutputCol", new ColumnName("total_size"));
        args.put("timeOutputCol", new ColumnName("total_time"));
        args.put("sizeUnit", new Text("MB"));
        args.put("timeUnit", new Text("x")); // Invalid unit
        args.put("aggregationType", new Text("total"));

        directive.initialize(new DirectiveArgumentsTest(args));
    }

    /**
     * Simple Arguments implementation for tests.
     */
    private static class DirectiveArgumentsTest implements io.cdap.wrangler.api.Arguments {
        private final Map<String, Object> tokens;

        DirectiveArgumentsTest(Map<String, Object> tokens) {
            this.tokens = tokens;
        }

        @Override
        public <T extends io.cdap.wrangler.api.parser.Token> T value(String name) {
            return (T) tokens.get(name);
        }

        @Override
        public int size() {
            return tokens.size();
        }

        @Override
        public boolean contains(String name) {
            return tokens.containsKey(name);
        }

        @Override
        public io.cdap.wrangler.api.parser.TokenType type(String name) {
            if (tokens.get(name) instanceof io.cdap.wrangler.api.parser.Token) {
                return ((io.cdap.wrangler.api.parser.Token) tokens.get(name)).type();
            }
            return null;
        }

        @Override
        public int line() {
            return 0;
        }

        @Override
        public int column() {
            return 0;
        }

        @Override
        public String source() {
            return "Test source";
        }

        @Override
        public com.google.gson.JsonElement toJson() {
            return new com.google.gson.JsonObject();
        }
    }
}
