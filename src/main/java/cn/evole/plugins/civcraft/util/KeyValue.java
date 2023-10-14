package cn.evole.plugins.civcraft.util;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;

import java.util.Collection;
import java.util.HashMap;
 
/*
 * Utility class that implements serialization/deserialization for key-value pairs into a single string.
 *              String outEncoded = new String(Base64Coder.encode(out.getBytes()));
                return outEncoded;
        }
 
        public static ItemStack deserializeEnhancements(ItemStack stack, String serial) {
                String in = StringUtils.toAsciiString(Base64Coder.decode(serial));
 */

public class KeyValue {

    private HashMap<String, Object> keyValues = new HashMap<String, Object>();

    public String serialize() {
        StringBuilder builder = new StringBuilder();

        for (String key : keyValues.keySet()) {
            Object value = keyValues.get(key);
            builder.append(key);
            builder.append(",");
            builder.append(value.getClass().getSimpleName());
            builder.append(",");
            String valueString = "" + value;
            builder.append(Base64Encoder.encode(valueString));
            builder.append(";");
        }

        return builder.toString();
    }

    public void deserialize(String input) {
        if (input == null || input.equals("")) {
            return;
        }

        String[] kvs = input.split(";");

        for (String kv : kvs) {
            String[] data = kv.split(",");


            String key = data[0];
            String className = data[1];
            String decodedValue;

            if (data.length < 3) {
                /* string key with no value? */
                decodedValue = "";
            } else {
                String encodedValue = data[2];
                decodedValue = Base64Decoder.decodeStr(encodedValue);
            }

            try {
                Object valueInstance;

                switch (className) {
                    case "Integer":
                        valueInstance = Integer.valueOf(decodedValue);
                        break;
                    case "Boolean":
                        valueInstance = Boolean.valueOf(decodedValue);
                        break;
                    case "Double":
                        valueInstance = Double.valueOf(decodedValue);
                        break;
                    default:
                        valueInstance = decodedValue;
                        break;
                }

                keyValues.put(key, valueInstance);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void setString(String key, String value) {
        keyValues.put(key, value);
    }

    public void setInt(String key, Integer value) {
        keyValues.put(key, value);
    }

    public void setDouble(String key, Double value) {
        keyValues.put(key, value);
    }

    public void setBoolean(String key, Boolean value) {
        keyValues.put(key, value);
    }

    public String getString(String key) {
        String value = (String) keyValues.get(key);
        return value;
    }

    public Integer getInt(String key) {
        Integer value = (Integer) keyValues.get(key);
        return value;
    }

    public Double getDouble(String key) {
        Double value = (Double) keyValues.get(key);
        return value;
    }

    public Boolean getBoolean(String key) {
        Boolean value = (Boolean) keyValues.get(key);
        return value;
    }

    public Collection<String> getKeySet() {
        return this.keyValues.keySet();
    }
}