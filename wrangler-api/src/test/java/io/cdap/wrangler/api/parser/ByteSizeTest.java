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

import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {

    @Test
    public void testValidByteSizes() {
        ByteSize size1 = new ByteSize("10KB");
        Assert.assertEquals(10 * 1024, size1.getBytes(), 0.001);
        Assert.assertEquals(10, size1.getKilobytes(), 0.001);
        Assert.assertEquals(10 / 1024.0, size1.getMegabytes(), 0.001);

        ByteSize size2 = new ByteSize("1.5MB");
        Assert.assertEquals(1.5 * 1024 * 1024, size2.getBytes(), 0.001);
        Assert.assertEquals(1.5 * 1024, size2.getKilobytes(), 0.001);
        Assert.assertEquals(1.5, size2.getMegabytes(), 0.001);

        ByteSize size3 = new ByteSize("1GB");
        Assert.assertEquals(1L * 1024 * 1024 * 1024, size3.getBytes(), 0.001);
        Assert.assertEquals(1L * 1024 * 1024, size3.getKilobytes(), 0.001);
        Assert.assertEquals(1024, size3.getMegabytes(), 0.001);
        Assert.assertEquals(1, size3.getGigabytes(), 0.001);

        ByteSize size4 = new ByteSize("1TB");
        Assert.assertEquals(1L * 1024 * 1024 * 1024 * 1024, size4.getBytes(), 0.001);
    }

    @Test
    public void testCaseInsensitiveParsing() {
        ByteSize sizeLower = new ByteSize("5mb");
        ByteSize sizeUpper = new ByteSize("5MB");
        Assert.assertEquals(sizeLower.getBytes(), sizeUpper.getBytes(), 0.001);
    }

    @Test
    public void testDecimalValues() {
        ByteSize size = new ByteSize("2.5GB");
        Assert.assertEquals(2.5 * 1024 * 1024 * 1024, size.getBytes(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnit() {
        new ByteSize("10XY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingNumber() {
        new ByteSize("MB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingUnit() {
        new ByteSize("1000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonNumeric() {
        new ByteSize("abcMB");
    }
}
