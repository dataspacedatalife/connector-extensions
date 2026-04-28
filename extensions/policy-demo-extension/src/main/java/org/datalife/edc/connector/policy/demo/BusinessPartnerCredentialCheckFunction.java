package org.datalife.edc.connector.policy.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.participant.spi.ParticipantAgentPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.TypeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BusinessPartnerCredentialCheckFunction <C extends ParticipantAgentPolicyContext> implements AtomicConstraintRuleFunction<Permission, C> {

    public static final String MVD_NAMESPACE = "https://w3id.org/mvd/credentials/";

    private final Monitor monitor;
    private final TypeManager typeManager;
    private Set<String> businessPartnerIds = Collections.emptySet();

    private Result<List<VerifiableCredential>> getCredentialList(ParticipantAgent agent) {
        var vcListClaim = agent.getClaims().get("vc");
        if (vcListClaim == null) {
            return Result.failure("No se ha encontrado la lista de VCs");
        }
        var vcList = (List<VerifiableCredential>) vcListClaim;
        if (vcList.isEmpty()) {
            return Result.failure("La lista de VCs esta vacia");
        }
        return Result.success(vcList);
    }

    private void loadBusinessPartners(String businessPartnersPath) {
        File businessPartnersFile = new File(businessPartnersPath).getAbsoluteFile();
        
        if (!businessPartnersFile.exists()) {
            monitor.warning("Path '%s' does not exist. It must be a resolvable path with read access. Business partners list will be empty.".formatted(businessPartnersPath));
            return;
        }

        var mapper = typeManager.getMapper();

        try (var is = new FileInputStream(businessPartnersFile)) {
            var listType = new TypeReference<List<String>>() {};
            businessPartnerIds = Set.copyOf(mapper.readValue(is, listType));
            monitor.info("Loaded %d business partners from '%s'.".formatted(businessPartnerIds.size(), businessPartnersFile.getPath()));
        } catch (IOException e) {
            monitor.severe("Failed to read business partners from file '%s': %s".formatted(businessPartnersFile.getPath(), e.getMessage()), e);
        }
    }

    private BusinessPartnerCredentialCheckFunction(TypeManager typeManager, Monitor monitor, String businessPartnersFilePath) {
        this.typeManager = typeManager;
        this.monitor = monitor;
        loadBusinessPartners(businessPartnersFilePath);
    }

    public static <C extends ParticipantAgentPolicyContext> BusinessPartnerCredentialCheckFunction<C> create(TypeManager typeManager, Monitor monitor, String businessPartnersFilePath) {
        return new BusinessPartnerCredentialCheckFunction<>(typeManager, monitor, businessPartnersFilePath){};
    }


    @Override
    public boolean evaluate(Operator operator, Object rightOperand, Permission permission, C policyContext) {

        if (!operator.equals(Operator.EQ)) {
            policyContext.reportProblem("Cannot evaluate operator " + operator);
            return false;
        }

        var pa = policyContext.participantAgent();
        if (pa == null) {
            policyContext.reportProblem("Participant agent is null");
            return false;
        }

        var credentialResult = getCredentialList(pa);
        if (credentialResult.failed()) {
            policyContext.reportProblem(credentialResult.getFailureDetail());
            return false;
        }

        return credentialResult.getContent()
                .stream()
                .filter(vc -> vc.getType().stream().anyMatch(t -> t.endsWith("XDataShareMembershipCredential")))
                .filter(vc -> vc.getExpirationDate().isAfter(Instant.now()) && vc.getIssuanceDate().isBefore(Instant.now()))
                .flatMap(credential -> credential.getCredentialSubject().stream())
                .anyMatch(credentialSubject -> {
                    // Check if the credential ID is in the business partners list
                    var credentialId = credentialSubject.getClaim(MVD_NAMESPACE, "id");
                    return "BusinessPartners".equals(rightOperand) &&
                            credentialId != null && businessPartnerIds.contains(credentialId);
                    
                });
    }
}
