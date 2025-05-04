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

 package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeDurationTest {

    @Test
    public void testSeconds() {
        TimeDuration duration = new TimeDuration("10s");
        assertEquals(10.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testMinutes() {
        TimeDuration duration = new TimeDuration("5min");
        assertEquals(300.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testHours() {
        TimeDuration duration = new TimeDuration("1.5h");
        assertEquals(5400.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testDays() {
        TimeDuration duration = new TimeDuration("2d");
        assertEquals(172800.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testWeeks() {
        TimeDuration duration = new TimeDuration("1w");
        assertEquals(604800.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testMonths() {
        TimeDuration duration = new TimeDuration("1m");
        assertEquals(2592000.0, duration.getSeconds(), 0.0001);
    }

    @Test
    public void testYears() {
        TimeDuration duration = new TimeDuration("1y");
        assertEquals(31536000.0, duration.getSeconds(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() {
        new TimeDuration("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumber() {
        new TimeDuration("xs");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedUnit() {
        TimeDuration duration = new TimeDuration("10q");
        duration.getSeconds();
    }
}
