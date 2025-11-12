package org.datalife.edc.connector.dataplane.demo;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public class FileDataSourceFactory implements DataSourceFactory {

    @Override
    public String supportedType() {
        return "File";
    }

    @Override
    public DataSource createSource(DataFlowStartMessage request) {
        return new FileDataSource(getFile(request).orElseThrow(RuntimeException::new));
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        return getFile(request)
                .map(it -> Result.success())
                .orElseGet(() -> Result.failure("sourceFile is not found or it does not exist"));
    }

    private Optional<File> getFile(DataFlowStartMessage request) {
        return Optional.ofNullable(request.getSourceDataAddress().getStringProperty("sourceFile"))
                .map(File::new)
                .filter(File::exists)
                .filter(File::isFile);
    }
}