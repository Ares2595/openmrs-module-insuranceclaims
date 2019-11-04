package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.ClaimResponse;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Reference;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;

import java.util.Collections;
import java.util.List;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getIdentifierValueByCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ACCESSION_ID;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.MEDICAL_RECORD_NUMBER;

public class ClaimResponseUtil {

    //ClaimResponse utils
    public static CodeableConcept getClaimResponseOutcome(InsuranceClaim insuranceClaim) {
        CodeableConcept outcome = new CodeableConcept();
        outcome.setText(insuranceClaim.getClaimStatus().toString());

        Coding outcomeCoding = new Coding();
        String code = String.valueOf(insuranceClaim.getClaimStatus().getValue());
        outcomeCoding.setCode(code);

        outcome.setCoding(Collections.singletonList(outcomeCoding));

        return outcome;
    }

    public static InsuranceClaimStatus getClaimResponseStatus(ClaimResponse response, List<String> errors) {
        String codeString = response
                .getOutcome()
                .getText();
        try {
            return InsuranceClaimStatus.valueOf(codeString);
        } catch (ArrayIndexOutOfBoundsException e) {
            errors.add("Status code " + codeString + " is invalid");
            return null;
        }
    }

    public static String getClaimCode(ClaimResponse claim, List<String> errors) {
        return getIdentifierValueByCode(claim, MEDICAL_RECORD_NUMBER, errors);
    }

    public static String getClaimUuid(ClaimResponse claim, List<String> errors) {
        return getIdentifierValueByCode(claim, ACCESSION_ID, errors);
    }


    public static Reference buildClaimReference(InsuranceClaim omrsClaim) {
        Reference reference = new Reference();
        String stringReference = "Claim/" + omrsClaim.getClaimCode();
        reference.setReference(stringReference);
        return reference;
    }

    public static List<Reference> buildCommunicationRequestReference(InsuranceClaim omrsClaim) {
        Reference reference = new Reference();
        String stringReference = "CommunicationRequest/" + omrsClaim.getUuid();
        reference.setReference(stringReference);
        return Collections.singletonList(reference);
    }

    public static List<ClaimResponse.ErrorComponent> getClaimErrors(InsuranceClaim omrsClaim) {
        ClaimResponse.ErrorComponent error = new ClaimResponse.ErrorComponent();

        CodeableConcept codeableConcept = new CodeableConcept();
        String rejectionReason = omrsClaim.getRejectionReason();
        Coding reasonCoding = new Coding();

        if (StringUtils.isNotBlank(rejectionReason)) {
            codeableConcept.setText(rejectionReason);
            reasonCoding.setCode(rejectionReason);
        }
        else {
            reasonCoding.setCode("0");
        }
        codeableConcept.setCoding(Collections.singletonList(reasonCoding));
        error.setCode(codeableConcept);
        return Collections.singletonList(error);
    }

    public static String getClaimResponseErrorCode(ClaimResponse claimResponse) {
        ClaimResponse.ErrorComponent ec = claimResponse.getErrorFirstRep();
        return ec.getCode().getCoding().get(0).getCode();
    }

    public static ClaimResponse.PaymentComponent createPaymentComponent(InsuranceClaim omrsClaim) {
        ClaimResponse.PaymentComponent payment = new ClaimResponse.PaymentComponent();
        CodeableConcept adjustmentReason = new CodeableConcept();
        adjustmentReason.setText(omrsClaim.getAdjustment());
        payment.setAdjustmentReason(adjustmentReason);
        payment.setDate(omrsClaim.getDateProcessed());

        return payment;
    }
}
