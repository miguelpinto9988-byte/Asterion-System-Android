package pe.asterionsystem.app;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Pruebas unitarias del arranque técnico, no de lógica de negocio. */
public final class BootstrapConfigurationTest {
    @Test
    public void applicationIdIsProvisionalProjectIdentifier() {
        assertEquals("pe.asterionsystem.app", BuildConfig.APPLICATION_ID);
    }

    @Test
    public void initialVersionCodeIsOne() {
        assertEquals(1, BuildConfig.VERSION_CODE);
    }
}
