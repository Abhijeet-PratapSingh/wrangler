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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.Locale;

/**
* Token class representing byte size values with units
* (e.g., "10KB", "5MB", "10.2MB").
* Parses and stores byte sizes,
* providing methods to retrieve the value in bytes.
*/
@PublicEvolving
public class ByteSize implements Token {

    private static final double KB_MULTIPLIER = 1024.0;
    private static final double MB_MULTIPLIER = KB_MULTIPLIER * KB_MULTIPLIER;
    private static final double GB_MULTIPLIER = MB_MULTIPLIER * KB_MULTIPLIER;
    private static final double TB_MULTIPLIER = GB_MULTIPLIER * KB_MULTIPLIER;

    private final double bytes;
    private final String value;

    /**
    * Constructs a ByteSize token from a string representation.
    * Accepts formats like "10KB", "5MB", "10.2MB".
    *
    * @param value String representation of a byte size with unit.
    * @throws IllegalArgumentException if the string
    * cannot be parsed as a byte size.
    */
    public ByteSize(String value) {
        this.value = value;
        this.bytes = parseBytes(value);
    }

    private Double parseBytes(final String sizeStr) {
        String trimmed = sizeStr.trim().toLowerCase(Locale.ENGLISH);
        String numberPart = trimmed.replaceAll("[^0-9.]", "");
        String unitPart = trimmed.replaceAll("[0-9.]", "");

        if (numberPart.isEmpty() || unitPart.isEmpty()) {
    throw new IllegalArgumentException(
        "Invalid byte size format:" +sizeStr);
        }

        double number;
        try {
            number = Double.parseDouble(numberPart);
        } catch (NumberFormatException e) {
throw new IllegalArgumentException(
    "Invalid number in byte size:" + sizeStr, e);
        }

         double multiplier;
         switch (unitPart) {
             case "b":
                 multiplier = 1;
                 break;
             case "kb":
                 multiplier = KB_MULTIPLIER;
                 break;
             case "mb":
                 multiplier = MB_MULTIPLIER;
                 break;
             case "gb":
                 multiplier = GB_MULTIPLIER;
                 break;
             case "tb":
                 multiplier = TB_MULTIPLIER;
                 break;
             default:
                 throw new IllegalArgumentException(
                    "Unsupported bytesize unit:" + unitPart);
         }

         return number * multiplier;
     }

     /**
      * Returns the size in bytes.
      */
     public Double getBytes() {
         return bytes;
     }

     /**
      * Returns the size in kilobytes.
      */
     public Double getKilobytes() {
         return bytes / KB_MULTIPLIER;
     }

     /**
      * Returns the size in megabytes.
      */
     public Double getMegabytes() {
         return bytes / MB_MULTIPLIER;
     }

     /**
      * Returns the size in gigabytes.
      */
     public Double getGigabytes() {
         return bytes / GB_MULTIPLIER;
     }

     /**
 * Returns the value in bytes.
 */
     @Override
     public Double value() {
         return getBytes();
     }

     /**
 * Returns the type.
 */
     @Override
     public TokenType type() {
        return TokenType.BYTE_SIZE;
     }
/**
 * Returns the json element.
 */
     @Override
     public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", value);
        return object;
     }
 }



