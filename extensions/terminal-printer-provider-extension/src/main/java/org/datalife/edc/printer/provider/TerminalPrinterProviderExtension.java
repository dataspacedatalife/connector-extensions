package org.datalife.edc.printer.provider;

import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.datalife.edc.spi.printer.Printer;

@Extension(value = TerminalPrinterProviderExtension.NAME)
public class TerminalPrinterProviderExtension implements ServiceExtension {
    public static final String NAME = "Terminal Printer Provider";

    private Printer printer;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        printer = new TerminalPrinter();
    }

    @Provider
    public Printer providerPrinter() {
        return printer;
    }

    class TerminalPrinter implements Printer {

        public TerminalPrinter() {
        }

        @Override
        public void print(String msg) {
            System.out.println(msg);
        }
    }

}