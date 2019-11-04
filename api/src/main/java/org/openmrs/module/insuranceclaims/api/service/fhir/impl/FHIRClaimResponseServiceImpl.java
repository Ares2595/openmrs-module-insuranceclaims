package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.hl7.fhir.dstu3.model.ClaimResponse;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Money;
import org.openmrs.module.fhir.api.util.BaseOpenMRSDataUtil;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimResponseService;

import java.util.List;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.buildClaimReference;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.buildCommunicationRequestReference;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.createPaymentComponent;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimErrors;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseErrorCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseOutcome;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseStatus;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimUuid;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimIdentifier;

public class FHIRClaimResponseServiceImpl implements FHIRClaimResponseService {

    public ClaimResponse generateClaimResponse(InsuranceClaim omrsClaim) {
        ClaimResponse claim = new ClaimResponse();

        //id
        IdType claimId = new IdType();
        claimId.setValue(omrsClaim.getClaimCode());
        claim.setId(claimId);

        //identifier
        claim.setIdentifier(createClaimIdentifier(omrsClaim));

        //status
        claim.setOutcome(getClaimResponseOutcome(omrsClaim));

        //payment
        claim.setPayment(createPaymentComponent(omrsClaim));

        //adjustiment
        claim.setDisposition(omrsClaim.getAdjustment());
        //TODO: MRS says that it should be mapped to disposition, but IMIS don't map disposition and have adjustment in payment instead

        //totalBenefit
        Money benefit = new Money();
        benefit.setValue(omrsClaim.getApprovedTotal());
        claim.setTotalBenefit(benefit);

        //date created
        claim.setCreated(omrsClaim.getDateProcessed());

        //error
        claim.setError(getClaimErrors(omrsClaim));

        //processNote
        //TODO: add when item mappings updated
        //items
        //TODO: Add item mappings after database changes

        //request
        claim.setRequest(buildClaimReference(omrsClaim));

        //communicationRequest
        claim.setCommunicationRequest(buildCommunicationRequestReference(omrsClaim));

        return claim;
    }

    public InsuranceClaim generateOmrsClaim(ClaimResponse claim, List<String> errors) {
        InsuranceClaim omrsClaim = new InsuranceClaim();

        //id
        BaseOpenMRSDataUtil.readBaseExtensionFields(omrsClaim, claim);
        BaseOpenMRSDataUtil.setBaseExtensionFields(claim, omrsClaim);

        omrsClaim.setUuid(getClaimUuid(claim, errors));

        //identifier
        omrsClaim.setClaimCode(getClaimCode(claim, errors));

        //status
        //Use outcome or process note?
        InsuranceClaimStatus status = getClaimResponseStatus(claim, errors);
        omrsClaim.setClaimStatus(status);

        //adjustiment
        //Use disposition or payment?
        omrsClaim.setAdjustment(claim.getPayment().getAdjustmentReason().getText());
        //TODO: MRS says that it should be mapped to disposition, but IMIS don't map disposition and have adjustment in payment instead

        //approved total
        omrsClaim.setApprovedTotal(claim.getTotalBenefit().getValue());

        //date processed
        //Use date or payment?
        omrsClaim.setDateProcessed(claim.getPayment().getDate());

        //error
        omrsClaim.setRejectionReason(getClaimResponseErrorCode(claim));

        //items
        //TODO: Add item mappings after database changes

        return omrsClaim;
    }
}
