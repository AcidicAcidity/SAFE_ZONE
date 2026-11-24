package com.safezone.backend.api;

import com.safezone.backend.api.config.BeanConfig;
import com.safezone.backend.infrastructure.data.DBHelper;
import com.safezone.backend.infrastructure.data.DatabaseInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseConfigurationTest {
    @Test void runtimeDatabaseConfigurationOverridesLegacyClasspathFile() {
        DBHelper database = new BeanConfig().dbHelper("jdbc:postgresql://db:5432/test", "docker-user", "docker-password");
        assertThat(ReflectionTestUtils.getField(database, "url")).isEqualTo("jdbc:postgresql://db:5432/test");
        assertThat(ReflectionTestUtils.getField(database, "user")).isEqualTo("docker-user");
    }

    @Test void initializerUsesRuntimeAdminAndPreservesExistingRecords() throws Exception {
        DBHelper database = mock(DBHelper.class);
        when(database.getSingleRow(anyString(), any())).thenReturn(Map.of("user_id", 1));
        when(database.getSingleRow(anyString())).thenReturn(Map.of("warehouse_id", 1));
        DatabaseInitializer initializer = new BeanConfig().databaseInitializer(database, "docker-admin", "docker-password");
        initializer.initialize();
        verify(database).getSingleRow(anyString(), eq("docker-admin"));
        verify(database, never()).executeUpdateData(anyString(), any(Object[].class));
    }
}
