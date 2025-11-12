package org.datalife.edc.printer.injector;

import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.datalife.edc.spi.printer.Printer;

@Extension(value = PrinterInjectorExtension.NAME)
public class PrinterInjectorExtension implements ServiceExtension {
    public static final String NAME = "Printer Injector";

    @Inject
    private Printer printer;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        printer.print("Hello, World!");
    }

}