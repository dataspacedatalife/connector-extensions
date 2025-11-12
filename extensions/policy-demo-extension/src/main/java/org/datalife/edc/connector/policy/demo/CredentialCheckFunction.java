package org.datalife.edc.connector.policy.demo;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.participant.spi.ParticipantAgentPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.result.Result;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CredentialCheckFunction <C extends ParticipantAgentPolicyContext> implements AtomicConstraintRuleFunction<Permission, C> {

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

    private CredentialCheckFunction() {

    }

    public static <C extends ParticipantAgentPolicyContext> CredentialCheckFunction<C> create() {
        return new CredentialCheckFunction<>() {
        };
    }

    @Override
    public boolean evaluate(Operator operator, Object rightOperand, Permission permission, C policyContext) {
        if (operator != Operator.EQ) {// Comprobamos que el operador sea correcto
            return false;
        }
        var participant = policyContext.participantAgent();
        var credentialResult = getCredentialList(participant);
        if (credentialResult.failed()){// Comprobamos que el participante tenga alguna VC
            return false;
        }
        return credentialResult.getContent()
                .stream()
                .filter(vc -> vc.getType().stream().anyMatch(t -> t.endsWith("MembershipCredential")))
                .flatMap(vc -> vc.getCredentialSubject().stream())
                .anyMatch(credentialSubject -> {
                    var membershipMap = credentialSubject.getClaim("https://w3id.org/mvd/credentials/", "membership");

                    if (membershipMap instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) membershipMap;
                        Object type = map.get("membershipType");
                        return Objects.equals(type, rightOperand);
                    }

                    return false;
                });// Buscamos entre las credenciales si alguna tiene type con el valor del rightOperand ("FullMember")
    }
}
