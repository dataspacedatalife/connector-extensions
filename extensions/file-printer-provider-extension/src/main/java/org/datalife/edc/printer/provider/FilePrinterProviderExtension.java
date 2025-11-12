package org.datalife.edc.printer.provider;

import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.datalife.edc.spi.printer.Printer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@Extension(FilePrinterProviderExtension.NAME)
public class FilePrinterProviderExtension implements ServiceExtension {
    public static final String NAME = "File Printer Provider";

    @Setting(key = "edc.printer.file.path", description = "Path for printer file")
    private String printerFilePath;

    private Printer printer;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        Path path = Path.of(printerFilePath);
        printer = new FilePrinter(path);
    }

    @Provider
    public Printer providePrinter() {
        return printer;
    }

    class FilePrinter implements Printer {
        private final Path path;

        public FilePrinter(Path path) {
            this.path = path;
        }

        @Override
        public void print(String msg) {
            System.out.println(">>> Writing to: " + path.toAbsolutePath());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
                writer.write(msg);
                writer.newLine();
            } catch (IOException ignored) {
            }
        }
    }
}
