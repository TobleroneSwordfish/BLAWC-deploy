package ac.uk.bristol.law.clinic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

//provides the file storage service with the base path to use
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties
{
    @NotEmpty
    @Getter
    @Setter
    private String basePath;
}
