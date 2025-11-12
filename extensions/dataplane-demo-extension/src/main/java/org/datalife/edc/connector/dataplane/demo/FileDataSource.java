package org.datalife.edc.connector.dataplane.demo;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.stream.Stream;

public class FileDataSource implements DataSource {

    private final File sourceFile;

    public FileDataSource(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public StreamResult<Stream<DataSource.Part>> openPartStream() {
        return StreamResult.success(Stream.of(new FileStreamPart(sourceFile)));
    }

    @Override
    public void close() {
    }

    private record FileStreamPart(File file) implements Part {

        @Override
        public String name() {
            return file.getName();
        }
        @Override
        public String mediaType() {
            try {
                String type = Files.probeContentType(file.toPath());
                return type != null ? type : "application/octet-stream";
            } catch (Exception e) {
                return "application/octet-stream";
            }
        }

        @Override
        public InputStream openStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}