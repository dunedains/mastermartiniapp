package cl.duoc.mastermartini.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ValidatorsTest {

    @Test fun `nombre requiere nombre y apellido`() {
        assertFalse(Validators.name("").isValid)
        assertFalse(Validators.name("Rosa").isValid)
        assertFalse(Validators.name("Rosa P3rez").isValid)
        assertTrue(Validators.name("Rosa Pérez").isValid)
    }

    @Test fun `correo valida formato`() {
        assertFalse(Validators.email("rosa@").isValid)
        assertFalse(Validators.email("rosa.cl").isValid)
        assertTrue(Validators.email("rosa@ejemplo.cl").isValid)
    }

    @Test fun `telefono celular chileno`() {
        assertTrue(Validators.phone("+56 9 1234 5678").isValid)
        assertTrue(Validators.phone("912345678").isValid)
        assertFalse(Validators.phone("+56 2 1234 5678").isValid)
        assertFalse(Validators.phone("1234").isValid)
        assertFalse(Validators.phone("9abc").isValid)
    }

    @Test fun `contrasena segura y confirmacion`() {
        assertFalse(Validators.password("corta1").isValid)
        assertFalse(Validators.password("sinnumeros").isValid)
        assertTrue(Validators.password("martini2026").isValid)
        assertFalse(Validators.passwordConfirmation("martini2026", "martini2025").isValid)
        assertTrue(Validators.passwordConfirmation("martini2026", "martini2026").isValid)
    }

    @Test fun `cantidad entre 1 y maximo`() {
        assertFalse(Validators.quantity("0").isValid)
        assertFalse(Validators.quantity("abc").isValid)
        assertFalse(Validators.quantity("1000").isValid)
        assertTrue(Validators.quantity("5").isValid)
    }

    @Test fun `direccion requiere numero`() {
        assertFalse(Validators.address("Av. Siempre Viva").isValid)
        assertTrue(Validators.address("Av. Siempre Viva 742").isValid)
    }

    @Test fun `fecha futura habil`() {
        val today = LocalDate.of(2026, 10, 27) // martes
        fun millis(d: LocalDate) = d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        assertFalse(Validators.futureWorkingDate(null, today).isValid)
        assertFalse(Validators.futureWorkingDate(millis(today), today).isValid)
        assertFalse(Validators.futureWorkingDate(millis(LocalDate.of(2026, 11, 1)), today).isValid) // domingo
        assertTrue(Validators.futureWorkingDate(millis(LocalDate.of(2026, 10, 28)), today).isValid)
        assertEquals("Máximo 60 días de anticipación",
            Validators.futureWorkingDate(millis(today.plusDays(90)), today).error)
    }
}
