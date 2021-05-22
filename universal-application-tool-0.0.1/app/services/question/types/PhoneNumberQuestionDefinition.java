package services.question.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import services.LocalizedStrings;

public class PhoneNumberQuestionDefinition extends QuestionDefinition {

    public PhoneNumberQuestionDefinition(
            OptionalLong id,
            String name,
            Optional<Long> enumeratorId,
            String description,
            LocalizedStrings questionText,
            LocalizedStrings questionHelpText,
            TextValidationPredicates validationPredicates) {
        super(
                id, name, enumeratorId, description, questionText, questionHelpText, validationPredicates);
    }

    public PhoneNumberQuestionDefinition(
            String name,
            Optional<Long> enumeratorId,
            String description,
            LocalizedStrings questionText,
            LocalizedStrings questionHelpText,
            TextValidationPredicates validationPredicates) {
        super(name, enumeratorId, description, questionText, questionHelpText, validationPredicates);
    }

    public PhoneNumberQuestionDefinition(
            String name,
            Optional<Long> enumeratorId,
            String description,
            LocalizedStrings questionText,
            LocalizedStrings questionHelpText) {
        super(
                name,
                enumeratorId,
                description,
                questionText,
                questionHelpText,
                TextValidationPredicates.create());
    }

    @JsonDeserialize(
            builder = AutoValue_PhoneNumberQuestionDefinition_PhoneNumberValidationPredicates.Builder.class)
    @AutoValue
    public abstract static class PhoneNumberValidationPredicates extends ValidationPredicates {

        public static PhoneNumberValidationPredicates parse(String jsonString) {
            try {
                return mapper.readValue(
                        jsonString, AutoValue_PhoneNumberQuestionDefinition_PhoneNumberValidationPredicates.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public static PhoneNumberValidationPredicates create() {
            return builder().build();
        }
        // Only validation predicate would be that entry contains all numbers/digits
        // But if you use <input type="tel"> you could do client side validation so is it redundant to
        // have a validation check here? No because you cannot rely on client-side validation
        // since users can potentially put malformed inputs for malicious reasons
        // Every form value is a string in HTML so you would have to do a type conversion when
        // saving phone number to database
        public static PhoneNumberValidationPredicates create(int minLength, int maxLength) {
            return builder().setMinLength(minLength).setMaxLength(maxLength).build();
        }

        public static Builder builder() {
            return new AutoValue_PhoneNumberQuestionDefinition_PhoneNumberValidationPredicates.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract PhoneNumberValidationPredicates build();
        }
    }

    public PhoneNumberValidationPredicates getPhoneNumberValidationPredicates() {
        return (PhoneNumberValidationPredicates) getValidationPredicates();
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.TEXT;
    }
}
