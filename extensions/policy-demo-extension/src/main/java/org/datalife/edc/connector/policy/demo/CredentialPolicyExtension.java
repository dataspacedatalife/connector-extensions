package org.datalife.edc.connector.policy.demo;


import org.eclipse.edc.connector.controlplane.catalog.spi.policy.CatalogPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.connector.controlplane.contract.spi.policy.TransferProcessPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import org.eclipse.edc.spi.types.TypeManager;

import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;

@Extension(value = CredentialPolicyExtension.NAME)
public class CredentialPolicyExtension implements ServiceExtension {
    public static final String NAME = "Credential Policy Evaluation Extension Demo";

    public static final String BUSINESS_PARTNERS_FILE_PATH = "edc.business.partners.file";

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private TypeManager typeManager;

    @Inject
    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var businessPartnersFilePath = context.getConfig().getString(BUSINESS_PARTNERS_FILE_PATH);

        registerBusinessPartnerCredentialCheckFunction(businessPartnersFilePath);
    }

    private void registerBusinessPartnerCredentialCheckFunction(String businessPartnersFilePath) {

        bindPermissionFunction(BusinessPartnerCredentialCheckFunction.create(typeManager, monitor, businessPartnersFilePath), TransferProcessPolicyContext.class, TransferProcessPolicyContext.TRANSFER_SCOPE, "XDataShareMembershipCredential.partner");
        bindPermissionFunction(BusinessPartnerCredentialCheckFunction.create(typeManager, monitor, businessPartnersFilePath), ContractNegotiationPolicyContext.class, ContractNegotiationPolicyContext.NEGOTIATION_SCOPE, "XDataShareMembershipCredential.partner");
        bindPermissionFunction(BusinessPartnerCredentialCheckFunction.create(typeManager, monitor, businessPartnersFilePath), CatalogPolicyContext.class, CatalogPolicyContext.CATALOG_SCOPE, "XDataShareMembershipCredential.partner");
    }

    private <C extends PolicyContext> void bindPermissionFunction(AtomicConstraintRuleFunction<Permission, C> function, Class<C> contextClass, String scope, String constraintType) {
        ruleBindingRegistry.bind("use", scope);
        ruleBindingRegistry.bind(ODRL_SCHEMA + "use", scope);
        ruleBindingRegistry.bind(constraintType, scope);

        policyEngine.registerFunction(contextClass, Permission.class, constraintType, function);
    }

    private <C extends PolicyContext> void bindDutyFunction(AtomicConstraintRuleFunction<Duty, C> function, Class<C> contextClass, String scope, String constraintType) {
        ruleBindingRegistry.bind("use", scope);
        ruleBindingRegistry.bind(ODRL_SCHEMA + "use", scope);
        ruleBindingRegistry.bind(constraintType, scope);

        policyEngine.registerFunction(contextClass, Duty.class, constraintType, function);
    }
}