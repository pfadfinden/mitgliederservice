package de.pfadfinden.mitglieder.bescheinigung.captcha;

public class CaptchaVerifyResponse {

    private boolean success;
    private String challenge_ts;
    private String hostname;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getChallenge_ts() {
        return challenge_ts;
    }

    public void setChallenge_ts(String challenge_ts) {
        this.challenge_ts = challenge_ts;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String toString(){
        return String.format("CaptchaVerifyResponse{success=%b, challenge_ts=%s, hostname=%s}",
                success, challenge_ts, hostname);
    }

}