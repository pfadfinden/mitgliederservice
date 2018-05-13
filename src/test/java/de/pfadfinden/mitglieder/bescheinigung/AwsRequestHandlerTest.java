package de.pfadfinden.mitglieder.bescheinigung;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import de.pfadfinden.mitglieder.bescheinigung.model.ValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AwsRequestHandlerTest {

    private AwsRequestHandler awsRequestHandler;

    @BeforeEach
    void tearUp() {
        this.awsRequestHandler = new AwsRequestHandler();
    }

    @Test
    void handleRequestValid() {

        Gson gson = new Gson();

        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setFirstName("Jhilioo");
        validationRequest.setLastName("Uteiumetzgeö");
        validationRequest.setDateOfBirth("1987-08-13");
        validationRequest.setMembershipNumber(11111);
        validationRequest.setChallenge("XXXXXXXXXXXXXXX");
        validationRequest.setReportAusweis(true);

        APIGatewayProxyRequestEvent apiGatewayRequest = new APIGatewayProxyRequestEvent();
        apiGatewayRequest.setBody(gson.toJson(validationRequest));

        APIGatewayProxyResponseEvent response = this.awsRequestHandler.handleRequest(apiGatewayRequest, null);

        assertEquals(200, response.getStatusCode().intValue());
        assertEquals("application/pdf",response.getHeaders().get("Content-Type"));
        assertEquals("X-Amzn-Requestid",response.getHeaders().get("Access-Control-Expose-Headers"));
        assertNotNull(response.getHeaders().get("Access-Control-Allow-Origin"));
        assertNotNull(response.getBody());
    }

    @Test
    void handleRequestInvalid() {

        Gson gson = new Gson();

        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setFirstName("Max");                            // <-- invalid input
        validationRequest.setLastName("Uteiumetzgeö");
        validationRequest.setDateOfBirth("1987-08-13");
        validationRequest.setMembershipNumber(11111);
        validationRequest.setChallenge("XXXXXXXXXXXXXXX");

        APIGatewayProxyRequestEvent apiGatewayRequest = new APIGatewayProxyRequestEvent();
        apiGatewayRequest.setBody(gson.toJson(validationRequest));

        APIGatewayProxyResponseEvent response = this.awsRequestHandler.handleRequest(apiGatewayRequest, null);

        assertEquals(400, response.getStatusCode().intValue());
        assertNull(response.getBody());
    }

}