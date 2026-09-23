package ai.interviewhq.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ih.security")
public class SecurityProperties {

    /**
     * When false, every request is permitted. Use this for local/dev only.
     * stg/prod profiles default this to true.
     */
    private boolean enabled = true;

    private String header = "X-API-Key";

    /** Convenience env bindings — {@code IH_API_KEY_CRAWLER}, etc. */
    private String crawlerKey = "";
    private String uiKey = "";
    private String adminKey = "";

    private List<ApiKey> apiKeys = new ArrayList<>();

    public List<ApiKey> resolvedKeys() {
        List<ApiKey> keys = new ArrayList<>();
        if (apiKeys != null) {
            for (ApiKey key : apiKeys) {
                if (key != null && key.getKey() != null && !key.getKey().isBlank()) {
                    keys.add(key);
                }
            }
        }
        addIfPresent(keys, "crawler", crawlerKey, List.of("CRAWLER", "READ"));
        addIfPresent(keys, "ui", uiKey, List.of("READ"));
        addIfPresent(keys, "admin", adminKey, List.of("ADMIN", "CRAWLER", "READ"));
        return keys;
    }

    private static void addIfPresent(List<ApiKey> keys, String name, String value, List<String> roles) {
        if (value == null || value.isBlank()) {
            return;
        }
        boolean exists = keys.stream().anyMatch(k -> name.equals(k.getName()) || value.equals(k.getKey()));
        if (exists) {
            return;
        }
        ApiKey key = new ApiKey();
        key.setName(name);
        key.setKey(value);
        key.setRoles(roles);
        keys.add(key);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getCrawlerKey() {
        return crawlerKey;
    }

    public void setCrawlerKey(String crawlerKey) {
        this.crawlerKey = crawlerKey;
    }

    public String getUiKey() {
        return uiKey;
    }

    public void setUiKey(String uiKey) {
        this.uiKey = uiKey;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }

    public List<ApiKey> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<ApiKey> apiKeys) {
        this.apiKeys = apiKeys == null ? new ArrayList<>() : apiKeys;
    }

    public static class ApiKey {
        private String name;
        private String key;
        private List<String> roles = List.of("READ");

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles == null ? List.of("READ") : roles;
        }
    }
}
