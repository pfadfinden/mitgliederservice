package de.pfadfinden.mitglieder.bescheinigung.service;

import de.pfadfinden.ica.IcaConnection;
import de.pfadfinden.ica.IcaServer;
import de.pfadfinden.ica.model.IcaMitgliedListElement;
import de.pfadfinden.ica.model.IcaSearchedValues;
import de.pfadfinden.ica.model.IcaSearchedValuesStatus;
import de.pfadfinden.ica.service.MitgliedService;
import de.pfadfinden.ica.service.ReportService;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationException;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationInputException;
import de.pfadfinden.mitglieder.bescheinigung.model.ValidationRequest;
import de.pfadfinden.mitglieder.bescheinigung.utils.PropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MembershipValidationService {

    private final Logger logger = LoggerFactory.getLogger(MembershipValidationService.class);

    public void validate(ValidationRequest validationRequest) throws MembershipValidationException, IOException {
        if (validationRequest == null) throw new MembershipValidationException("Mitglied nicht gefunden");

        logger.info("validationRequest: {}", validationRequest);

        ArrayList<IcaMitgliedListElement> mitgliederResult;
        IcaSearchedValues icaSearchedValues = buildSearchedValues(validationRequest);

        String icaServer = PropertyFactory.getPropertiesMap().getProperty("ica.server");
        String icaUsername = PropertyFactory.getPropertiesMap().getProperty("ica.username");
        String icaPassword = PropertyFactory.getPropertiesMap().getProperty("ica.password");

        IcaServer ica = (icaServer.equals("BDP_QA")) ? IcaServer.BDP_QA : IcaServer.BDP_PROD;

        try {
            IcaConnection icaConnection = new IcaConnection(ica, icaUsername, icaPassword);
            MitgliedService mitgliedService = new MitgliedService(icaConnection);
            mitgliederResult = mitgliedService.getMitgliedBySearch(icaSearchedValues, 1, 0, 1);
        } catch (Exception e) {
            logger.error("Membership validation exception",e);
            throw new MembershipValidationException(e.getMessage());
        }

        logger.info("MitgliederResult {}",mitgliederResult);

        if(mitgliederResult == null) throw new IOException("MV returned null");

        if (mitgliederResult.size() > 0) {
            IcaMitgliedListElement icaMitglied = mitgliederResult.iterator().next();
            logger.info("Mitglied gefunden: {}", icaMitglied);
            if (icaMitglied == null) {
                throw new MembershipValidationException("Abfrage MV fehlerhaft");
            }

            if (!icaMitglied.getGeburtsDatum().toString().equals(validationRequest.getDateOfBirth())) {
                throw new MembershipValidationException("Geburtsdatum");
            }

            if (!icaMitglied.getVorname().equals(validationRequest.getFirstName().trim())) {
                throw new MembershipValidationException("FirstName");
            }

            if (!icaMitglied.getNachname().equals(validationRequest.getLastName().trim())) {
                throw new MembershipValidationException("LastName");
            }
        } else {
            throw new MembershipValidationException("Mitglied nicht gefunden");
        }
    }

    private IcaSearchedValues buildSearchedValues(ValidationRequest validationRequest) {

        IcaSearchedValues.Builder builder = new IcaSearchedValues.Builder();
        if (validationRequest == null) return builder.build();

        return builder.withNachname(validationRequest.getLastName())
                .withVorname(validationRequest.getFirstName())
                .withMitgliedsNummber(String.valueOf(validationRequest.getMembershipNumber()))
                .withMglStatusId(IcaSearchedValuesStatus.AKTIV)
                .withTaetigkeitId(Arrays.asList(1, 2)).build();
    }

    public byte[] getReport(ValidationRequest validationRequest, String requestId) throws
            MembershipValidationInputException {

        String icaServer = PropertyFactory.getPropertiesMap().getProperty("ica.server");
        String icaUsername = PropertyFactory.getPropertiesMap().getProperty("ica.username");
        String icaPassword = PropertyFactory.getPropertiesMap().getProperty("ica.password");

        String reportString = PropertyFactory.getPropertiesMap().getProperty("ica.report.bescheinigung.reportId");
        if(validationRequest.isReportAusweis()){
            reportString = PropertyFactory.getPropertiesMap().getProperty("ica.report.ausweis.reportId");
        }
        int reportId = Integer.parseInt(reportString);

        IcaServer ica = (icaServer.equals("BDP_QA")) ? IcaServer.BDP_QA : IcaServer.BDP_PROD;

        HashMap<String, Object> reportParams = new HashMap<>();
        reportParams.put("A_Mitgliedsnummer", validationRequest.getMembershipNumber());
        reportParams.put("X_RequestId", requestId);

        try {
            IcaConnection icaConnection = new IcaConnection(ica, icaUsername, icaPassword);
            ReportService reportService = new ReportService(icaConnection);
            return reportService.getReport(reportId, 1, reportParams);
        } catch (Exception e) {
            throw new MembershipValidationInputException("report generierung");
        }
    }
}
