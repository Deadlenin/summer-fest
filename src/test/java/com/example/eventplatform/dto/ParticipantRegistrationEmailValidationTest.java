package com.example.eventplatform.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParticipantRegistrationEmailValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest(name = "принимает публичный домен: {0}")
    @ValueSource(strings = {
            "user@mail.ru",
            "user@inbox.ru",
            "user@list.ru",
            "user@bk.ru",
            "user@yandex.ru",
            "user@ya.ru",
            "user@yandex.com",
            "user@rambler.ru",
            "имя.фамилия@mail.ru",
            "user+fest@yandex.ru"
    })
    @DisplayName("Основные российские почтовые домены")
    void acceptsMajorRussianPublicDomains(String email) {
        assertThat(emailViolations(email)).isEmpty();
    }

    @ParameterizedTest(name = "принимает корпоративный домен: {0}")
    @ValueSource(strings = {
            "employee@gazprom.ru",
            "employee@gazprombank.ru",
            "employee@sberbank.ru",
            "employee@sber.ru",
            "employee@vtb.ru",
            "employee@mts.ru",
            "employee@megafon.ru",
            "employee@rt.ru",
            "employee@rostelecom.ru",
            "employee@yandex-team.ru",
            "qqqq@it-one.ru",
            "dev@company.gazprom.ru"
    })
    @DisplayName("Корпоративные домены крупных российских компаний")
    void acceptsLargeRussianCorporateDomains(String email) {
        assertThat(emailViolations(email)).isEmpty();
    }

    @ParameterizedTest(name = "отклоняет невалидный email: {0}")
    @MethodSource("invalidEmails")
    @DisplayName("Невалидные адреса отклоняются")
    void rejectsInvalidEmails(String email) {
        assertThat(emailViolations(email))
                .as("ожидалась ошибка валидации поля email для: %s", email)
                .isNotEmpty();
    }

    static Stream<String> invalidEmails() {
        return Stream.of(
                // нет @ / пустые
                "",
                "   ",
                "not-an-email",
                "user.mail.ru",
                "user mail.ru",

                // битая структура вокруг @
                "user@",
                "@mail.ru",
                "@",
                "user@@mail.ru",
                "user@mail@ru",

                // битый домен / локальная часть
                "user@.ru",
                "user@mail.",
                "user@-mail.ru",
                ".user@mail.ru",
                "user.@mail.ru",
                "user@mail..ru",

                // пробелы и спецсимволы
                " user@mail.ru",
                "user@mail.ru ",
                "user@mai l.ru",
                "user(name)@mail.ru",
                "user@mail.ru,",
                "user@mail.ru;"
        );
    }

    private Set<ConstraintViolation<ParticipantRegistrationRequest>> emailViolations(String email) {
        return validator.validate(validRequestWithEmail(email)).stream()
                .filter(violation -> "email".equals(violation.getPropertyPath().toString()))
                .collect(java.util.stream.Collectors.toSet());
    }

    private ParticipantRegistrationRequest validRequestWithEmail(String email) {
        return new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                email,
                "Компания",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                true,
                true,
                false
        );
    }
}
