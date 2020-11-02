package net.querybuilder4j;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class serves as an alternative entry point to the Spring application when packaged as a JAR.  If the `updateCache`
 * option is present and has a value of `true`, then the database metadata is refreshed and then the Spring application
 * is exited.  This logic is run at the `ApplicationReadyEvent`, which is after all application beans are instantiated and
 * injected by Spring's Dependency Injection (DI) logic.  This allows this repo's docker image to be run as both the API
 * and, optionally, as a scheduled task that refreshes the database metadata cache.
 */
@Component
public class UpdateDatabaseMetadataCacheRunner implements ApplicationListener<ApplicationReadyEvent> {

    Logger LOG = LoggerFactory.getLogger(UpdateDatabaseMetadataCacheRunner.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LOG.info("Inside UpdateDatabaseMetadataCacheRunner.onApplicationEvent method");

        try {
            Optional<String> updateCacheOption = Optional.ofNullable(System.getProperty("updateCache"));
            if (updateCacheOption.isPresent() && updateCacheOption.get().equals("true")) {
                LOG.info("updateCache property is present.  Refreshing cache.");
                applicationReadyEvent.getApplicationContext().getBean(DatabaseMetadataCache.class).refreshCache();

                LOG.info("Shutting down Spring application.");
                applicationReadyEvent.getApplicationContext().close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

}
