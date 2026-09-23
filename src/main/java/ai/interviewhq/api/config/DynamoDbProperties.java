package ai.interviewhq.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ih.dynamodb")
public class DynamoDbProperties {

    /**
     * Single-table name. Schema default is {@code interview-hq}.
     */
    private String table = "interview-hq";

    private String region = "ap-south-1";

    /**
     * When set (local/dev), the client talks to DynamoDB Local / a custom endpoint
     * instead of AWS. Leave blank for stg/prod so the SDK uses the regional AWS endpoint.
     */
    private String endpoint = "";

    /**
     * Create the table (and GSIs) on startup when missing. Intended for local/dev only.
     */
    private boolean autoCreateTable = false;

    /**
     * Optional static credentials. Local DynamoDB accepts dummy values. stg/prod should
     * leave these blank and rely on the default credential chain (IAM role, env, profile).
     */
    private String accessKey = "";

    private String secretKey = "";

    public boolean hasEndpointOverride() {
        return endpoint != null && !endpoint.isBlank();
    }

    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
