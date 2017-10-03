package de.pfadfinden.mitglieder.bescheinigung;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import de.pfadfinden.mitglieder.bescheinigung.captcha.CaptchaService;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationException;
import de.pfadfinden.mitglieder.bescheinigung.model.ValidationRequest;
import de.pfadfinden.mitglieder.bescheinigung.service.MembershipValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class AwsRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AwsRequestHandler.class);
    private Gson gson = new Gson();
    private MembershipValidationService membershipValidationService = new MembershipValidationService();
    private CaptchaService captchaService = new CaptchaService();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
        String fullRequestId = "";
        String shortRequestId = "";

        if(apiGatewayRequest.getRequestContext() != null) {

            fullRequestId = apiGatewayRequest.getRequestContext().getRequestId();
            shortRequestId = fullRequestId.substring(0,fullRequestId.indexOf("-"));

            logger.info("RequestContext requestId: {} ShortID: {}", apiGatewayRequest.getRequestContext()
                            .getRequestId(),shortRequestId);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Headers","Content-Type,X-Amz-Date,Authorization,X-Api-Key," +
                "X-Amz-Security-Token");
        headers.put("Access-Control-Expose-Headers","X-Amzn-Requestid");
        headers.put("Access-Control-Allow-Methods","POST,OPTIONS");
        headers.put("Access-Control-Allow-Origin","*");

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            byte[] encodedBytes = this.processRequest(apiGatewayRequest,shortRequestId);
            headers.put("Content-Type", "application/pdf");
            responseEvent.setStatusCode(200);
            responseEvent.setHeaders(headers);
            responseEvent.setBody(new String(encodedBytes));
        } catch (MembershipValidationException e) {
            logger.info("Validation failed",e);
            responseEvent.setStatusCode(400);
            responseEvent.setHeaders(headers);
            responseEvent.setBody(null);
        }
        return responseEvent;
    }

    private byte[] processRequest(APIGatewayProxyRequestEvent apiGatewayRequest, String requestId) throws
            MembershipValidationException {
        ValidationRequest validationRequest = this.gson.fromJson(apiGatewayRequest.getBody(), ValidationRequest.class);

        if(apiGatewayRequest.getRequestContext() != null) {
            // Captcha verifizieren
            captchaService.verify(validationRequest.getChallenge(),
                    apiGatewayRequest.getRequestContext().getIdentity().getSourceIp());
        }

        // Mitgliedsdaten pruefen
        membershipValidationService.validate(validationRequest);

        // Report generieren
        byte[] icaReport = membershipValidationService.getReport(validationRequest, requestId);

        // Report base64 codiert
        return Base64.getEncoder().encode(icaReport);
    }

}