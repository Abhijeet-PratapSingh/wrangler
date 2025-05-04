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
  * TimeDuration represents a duration like '10s', '5min', '1.5h' and converts it into seconds.
  *
  * Supports units: s, min, h, d, w, m, y
  */
 @PublicEvolving
 public class TimeDuration implements Token {
     private final double value;
     private final String unit;
 
     /**
      * Constructs a TimeDuration from the given token.
      *
      * @param token e.g., "10s", "5min", "1.5h"
      */
     public TimeDuration(String token) {
         token = token.trim().toLowerCase(Locale.ENGLISH);
         this.unit = token.replaceAll("[0-9.]", "");
         String numberPart = token.replaceAll("[^0-9.]", "");
         if (numberPart.isEmpty() || unit.isEmpty()) {
             throw new IllegalArgumentException("Invalid time duration format: " + token);
         }
         try {
             this.value = Double.parseDouble(numberPart);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Invalid number in time duration: " + token, e);
         }
     }
 
     /**
      * Returns the time duration in seconds.
      *
      * @return time in seconds as double
      */
     public Double getSeconds() {
         switch (unit) {
             case "s":
                 return value;
             case "min":
                 return value * 60;
             case "h":
                 return value * 60 * 60;
             case "d":
                 return value * 24 * 60 * 60;
             case "w":
                 return value * 7 * 24 * 60 * 60;
             case "m":
                 return value * 30 * 24 * 60 * 60; // Approximate: 30 days
             case "y":
                 return value * 365 * 24 * 60 * 60; // Approximate: 365 days
             default:
                 throw new IllegalArgumentException("Unsupported time unit: " + unit);
         }
     }
 

     @Override
    public Double value() {
        return getSeconds();
    }

     @Override
     public TokenType type() {
         return TokenType.TIME_DURATION;
     }
 
     @Override
     public JsonElement toJson() {
         JsonObject object = new JsonObject();
         object.addProperty("type", TokenType.TIME_DURATION.name());
         object.addProperty("value", value + unit);
         return object;
     }
 }
 