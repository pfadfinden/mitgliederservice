package de.pfadfinden.mitglieder.bescheinigung.captcha;

import com.google.gson.Gson;
import de.pfadfinden.mitglieder.bescheinigung.utils.PropertyFactory;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationException;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationInputException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CaptchaService {

    private final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    private Gson gson = new Gson();

    public void verify(String responseCode, String remoteIp) throws MembershipValidationException {

        if (!Boolean.parseBoolean(PropertyFactory.getPropertiesMap().getProperty("captcha.enabled"))) {
            logger.warn("reCaptcha verification is not enabled in application config.");
            return;
        }

        if (responseCode == null || "".equals(responseCode)) throw new MembershipValidationInputException("captcha " +
                "empty or null");
        try {
            CaptchaVerifyResponse captchaVerifyResponse = verifyResponse(responseCode, remoteIp);
            if (captchaVerifyResponse == null || !captchaVerifyResponse.isSuccess())
                throw new MembershipValidationInputException("Captcha challenge fehlerhaft.");
        } catch (IOException e) {
            logger.error("technical error validating captcha", e);
            throw new MembershipValidationException();
        } catch (URISyntaxException e) {
            logger.error("technical error validating captcha", e);
            throw new MembershipValidationException();
        }
    }

    private CaptchaVerifyResponse verifyResponse(String captchaResponse, String remoteip) throws IOException,
            URISyntaxException {

        String captchaSecret = PropertyFactory.getPropertiesMap().getProperty("captcha.secret");
        logger.debug("reCaptcha challenge: {}", captchaResponse);

        URIBuilder builder = new URIBuilder()
                .setScheme("https").setHost("www.google.com").setPath("/recaptcha/api/siteverify")
                .addParameter("secret", captchaSecret)
                .addParameter("response", captchaResponse)
                .addParameter("remoteip", remoteip);

        URI requestUri = builder.build();
        logger.debug("reCaptcha siteverfy request uri: {}", requestUri);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet httpget = new HttpGet(requestUri);
            HttpResponse result = httpClient.execute(httpget);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            CaptchaVerifyResponse verifyResponse = gson.fromJson(json, CaptchaVerifyResponse.class);
            logger.debug("reCaptcha siteverfy response object: {}", verifyResponse);
            return verifyResponse;
        }
    }
}
