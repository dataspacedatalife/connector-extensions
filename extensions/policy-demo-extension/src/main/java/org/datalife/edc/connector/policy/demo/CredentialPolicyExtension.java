package org.datalife.edc.connector.policy.demo;


import org.eclipse.edc.connector.controlplane.catalog.spi.policy.CatalogPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.TransferProcessPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class CredentialPolicyExtension implements ServiceExtension {

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;

    @Override
    public void initialize(ServiceExtensionContext context) {
        ruleBindingRegistry.bind("MembershipCredential.type", "catalog");
        ruleBindingRegistry.bind("MembershipCredential.type", "contract.negotiation");
        ruleBindingRegistry.bind("MembershipCredential.type", "transfer.process");

        policyEngine.registerFunction(CatalogPolicyContext.class, Permission.class, "MembershipCredential.type", CredentialCheckFunction.create());
        policyEngine.registerFunction(ContractNegotiationPolicyContext.class, Permission.class, "MembershipCredential.type", CredentialCheckFunction.create());
        policyEngine.registerFunction(TransferProcessPolicyContext.class, Permission.class, "MembershipCredential.type", CredentialCheckFunction.create());
    }
}