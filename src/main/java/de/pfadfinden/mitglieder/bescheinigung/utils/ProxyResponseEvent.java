package de.pfadfinden.mitglieder.bescheinigung.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class ProxyResponseEvent extends APIGatewayProxyResponseEvent {

    private Boolean isBase64Encoded;

    public Boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    public void setIsBase64Encoded(Boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    public ProxyResponseEvent withIsBase64Encoded(Boolean isBase64Encoded) {
        this.setIsBase64Encoded(isBase64Encoded);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (this.getStatusCode() != null) {
            sb.append("statusCode: ").append(this.getStatusCode()).append(",");
        }

        if (this.getHeaders() != null) {
            sb.append("headers: ").append(this.getHeaders().toString()).append(",");
        }

        if (this.getBody() != null) {
            sb.append("body: ").append(this.getBody());
        }

        if (this.getIsBase64Encoded() != null) {
            sb.append("isBase64Encoded: ").append(getIsBase64Encoded());
        }

        sb.append("}");
        return sb.toString();
    }
}
