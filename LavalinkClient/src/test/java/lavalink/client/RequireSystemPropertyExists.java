package lavalink.client;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

/**
 * Created by napster on 06.03.18.
 * <p>
 * Checks whether the required system properties have been set
 */
public class RequireSystemPropertyExists implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<RequireSystemProperty> annotation = AnnotationSupport.findAnnotation(context.getElement(), RequireSystemProperty.class);
        if (annotation.isPresent()) {
            for (String propertyKey : annotation.get().value()) {
                String propertyValue = System.getProperty(propertyKey);
                if (propertyValue == null || propertyValue.isEmpty()) {
                    return ConditionEvaluationResult.disabled(String.format("System property '%s' not set. Skipping test.", propertyKey));
                }
            }
            return ConditionEvaluationResult.enabled("All required system properties present. Continuing test.");
        }
        return ConditionEvaluationResult.enabled("No RequireSystemProperty annotation found. Continuing test.");
    }
}
