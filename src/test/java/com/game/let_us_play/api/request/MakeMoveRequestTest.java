package com.game.let_us_play.api.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MakeMoveRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequest_shouldPassValidation() {
        MakeMoveRequest request = new MakeMoveRequest(1, 2);

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void negativeRow_shouldFailValidation() {
        MakeMoveRequest request = new MakeMoveRequest(-1, 2);

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Row must be 0 or greater.");
    }

    @Test
    void negativeCol_shouldFailValidation() {
        MakeMoveRequest request = new MakeMoveRequest(1, -2);

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Col must be 0 or greater.");
    }
}
