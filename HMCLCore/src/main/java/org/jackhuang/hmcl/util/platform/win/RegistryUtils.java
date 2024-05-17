package org.jackhuang.hmcl.util.platform.win;

import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Only usable on Windows
 */
public final class RegistryUtils {
    private RegistryUtils() {
    }

    public enum Type {
        LOCAL_MACHINE("HKLM"), CURRENT_USER("HKCU");

        private final String value;

        Type(String value) {
            this.value = value;
        }
    }

    public static final class QueryResult {
        private final String type;

        private final String value;

        private QueryResult(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    public static Task<QueryResult> query(Type type, String path, String key) {
        return Task.supplyAsync(() -> {
            String queryString = type.value + '\\' + path;
            Process process = new ProcessBuilder(
                    "REG", "QUERY", queryString, "/v", key
            ).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), OperatingSystem.NATIVE_CHARSET))) {
                if ("".equals(reader.readLine())) {
                    return null;
                }

                if (!queryString.equals(reader.readLine())) {
                    return null;
                }

                String data = reader.readLine();
                int keyLength = key.length();
                if (data == null || !data.startsWith("    ") || !data.regionMatches(true, 4, key, 0, keyLength) || !data.regionMatches(4 + keyLength, "    ", 0, 4)) {
                    return null;
                }

                int valueTypeStart = 8 + keyLength, valueTypeEnd = data.indexOf(' ', valueTypeStart);
                String valueType = data.substring(valueTypeStart, valueTypeEnd);

                if (!data.regionMatches(valueTypeEnd, "    ", 0, 4)) {
                    return null;
                }
                String valueValue = data.substring(valueTypeEnd + 4);

                if ("".equals(reader.readLine())) {
                    return null;
                }
                if ("".equals(reader.readLine())) {
                    return null;
                }

                return new QueryResult(valueType, valueValue);
            } finally {
                process.destroy();
            }
        });
    }
}
