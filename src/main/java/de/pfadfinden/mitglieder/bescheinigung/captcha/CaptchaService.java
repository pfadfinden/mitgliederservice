package de.pfadfinden.mitglieder.bescheinigung.captcha;

import com.google.gson.Gson;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationException;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationInputException;
import de.pfadfinden.mitglieder.bescheinigung.utils.PropertyFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class CaptchaService {

    private final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    private Gson gson = new Gson();
    private OkHttpClient client = new OkHttpClient();

    public void verify(String responseCode, String remoteIp) throws MembershipValidationException, IOException {

        if (!Boolean.parseBoolean(PropertyFactory.getPropertiesMap().getProperty("captcha.enabled"))) {
            logger.warn("reCaptcha verification is not enabled in application config.");
            return;
        }

        if (responseCode == null || "".equals(responseCode))
            throw new MembershipValidationInputException("captcha empty or null");

        CaptchaVerifyResponse captchaVerifyResponse = verifyResponse(responseCode, remoteIp);
        if (captchaVerifyResponse == null || !captchaVerifyResponse.isSuccess())
            throw new MembershipValidationInputException("Captcha challenge fehlerhaft.");
    }

    private CaptchaVerifyResponse verifyResponse(String captchaResponse, String remoteip) throws IOException {

        String captchaSecret = PropertyFactory.getPropertiesMap().getProperty("captcha.secret");
        logger.debug("reCaptcha challenge: {}", captchaResponse);


        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("www.google.com")
                .addPathSegments("recaptcha/api/siteverify")
                .addQueryParameter("secret", captchaSecret)
                .addQueryParameter("response", captchaResponse)
                .addQueryParameter("remoteip", remoteip)
                .build();

        logger.debug("reCaptcha siteverfy request uri: {}", httpUrl);

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Objects.requireNonNull(response.body());
            String responseBody = response.body().string();
            CaptchaVerifyResponse verifyResponse = gson.fromJson(responseBody, CaptchaVerifyResponse.class);
            logger.debug("reCaptcha siteverfy response object: {}", verifyResponse);
            return verifyResponse;
        }
    }
}
