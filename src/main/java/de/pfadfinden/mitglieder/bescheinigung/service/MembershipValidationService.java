package de.pfadfinden.mitglieder.bescheinigung.service;

import de.pfadfinden.mitglieder.bescheinigung.utils.PropertyFactory;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationException;
import de.pfadfinden.mitglieder.bescheinigung.exception.MembershipValidationInputException;
import de.pfadfinden.mitglieder.bescheinigung.model.ValidationRequest;
import de.pfadfinden.ica.IcaConnector;
import de.pfadfinden.ica.IcaServer;
import de.pfadfinden.ica.model.IcaMitgliedListElement;
import de.pfadfinden.ica.model.IcaMitgliedStatus;
import de.pfadfinden.ica.model.IcaSearchedValues;
import de.pfadfinden.ica.service.MitgliedService;
import de.pfadfinden.ica.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MembershipValidationService {

    private final Logger logger = LoggerFactory.getLogger(MembershipValidationService.class);

    public void validate(ValidationRequest validationRequest) throws MembershipValidationException {
        if (validationRequest == null) throw new MembershipValidationException("Mitglied nicht gefunden");

        logger.info("validationRequest: {}", validationRequest);

        ArrayList<IcaMitgliedListElement> mitgliederResult;
        IcaSearchedValues icaSearchedValues = buildSearchedValues(validationRequest);

        String icaServer = PropertyFactory.getPropertiesMap().getProperty("ica.server");
        String icaUsername = PropertyFactory.getPropertiesMap().getProperty("ica.username");
        String icaPassword = PropertyFactory.getPropertiesMap().getProperty("ica.password");

        IcaServer ica = (icaServer.equals("BDP_QA")) ? IcaServer.BDP_QA : IcaServer.BDP_PROD;

        try (
                IcaConnector icaConnector = new IcaConnector(ica, icaUsername, icaPassword);
        ) {
            MitgliedService mitgliedService = new MitgliedService(icaConnector);
            mitgliederResult = mitgliedService.getMitgliedBySearch(icaSearchedValues, 1, 0, 1);
        } catch (Exception e) {
            logger.error("Membership validation exception",e);
            throw new MembershipValidationException(e.getMessage());
        }

        logger.info("MitgliederResult {}",mitgliederResult);

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
        IcaSearchedValues icaSearchedValues = new IcaSearchedValues();
        if (validationRequest == null) return icaSearchedValues;
        icaSearchedValues.setNachname(validationRequest.getLastName());
        icaSearchedValues.setVorname(validationRequest.getFirstName());
        icaSearchedValues.setMitgliedsNummber(String.valueOf(validationRequest.getMembershipNumber()));
        icaSearchedValues.setMglStatusId(IcaMitgliedStatus.AKTIV);
        icaSearchedValues.setTaetigkeitId(Arrays.asList(1, 2));
        return icaSearchedValues;
    }

    public byte[] getReport(ValidationRequest validationRequest, String requestId) throws
            MembershipValidationInputException {

        String icaServer = PropertyFactory.getPropertiesMap().getProperty("ica.server");
        String icaUsername = PropertyFactory.getPropertiesMap().getProperty("ica.username");
        String icaPassword = PropertyFactory.getPropertiesMap().getProperty("ica.password");

        IcaServer ica = (icaServer.equals("BDP_QA")) ? IcaServer.BDP_QA : IcaServer.BDP_PROD;

        HashMap<String, Object> reportParams = new HashMap<>();
        reportParams.put("A_Mitgliedsnummer", validationRequest.getMembershipNumber());
        reportParams.put("X_RequestId", requestId);

        try (
                IcaConnector icaConnector = new IcaConnector(ica, icaUsername, icaPassword);
        ) {
            ReportService reportService = new ReportService(icaConnector);
            return reportService.getReport(105, 1, reportParams);

        } catch (Exception e) {
            throw new MembershipValidationInputException("report generierung");
        }
    }
}