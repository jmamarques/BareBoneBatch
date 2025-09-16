package com.barebonebatch.common.processor;

import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.domain.MappingFields;
import com.barebonebatch.common.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A generic Spring Batch {@link ItemProcessor} that converts an {@link ImportLine}
 * containing a fixed-length string into a target object of type {@code T}.
 * <p>
 * This processor is configured using a list of {@link MappingFields}, which define how to parse
 * the fixed-length string and map its parts to the properties of the target object.
 * <p>
 * For performance, it caches reflection metadata (like setter methods) during initialization
 * to avoid expensive reflection calls for each item processed.
 *
 * @param <T> The target type to which the import line will be converted.
 */
public class ImportLineProcessor<T> implements ItemProcessor<ImportLine, T>, InitializingBean {

    /**
     * A private static inner class to hold cached reflection metadata.
     * This avoids expensive lookups during the process() method.
     */
    private record CachedField(String propertyName, String fieldType, String pattern, Method setter, boolean mandatory, String transformer,
                               boolean enable) {
    }

    private FixedLengthTokenizer tokenizer;
    private final List<MappingFields> mappingFields;
    private final Class<T> targetType;
    private List<CachedField> cachedFields;

    /**
     * Constructs a new {@code ImportLineProcessor}.
     *
     * @param targetType    The class of the target object.
     * @param mappingFields A list of {@link MappingFields} that define the parsing and mapping rules.
     */
    public ImportLineProcessor(Class<T> targetType, List<MappingFields> mappingFields) {
        this.mappingFields = mappingFields;
        this.targetType = targetType;
    }

    /**
     * Processes an {@link ImportLine} by tokenizing its text content based on the provided mapping.
     * It converts each field to the specified type, applies any defined transformers, and populates
     * a new instance of the target object.
     *
     * @param input The {@link ImportLine} to process.
     * @return A new, populated instance of the target object, or {@code null} if the input is null.
     * @throws Exception if any error occurs during processing, such as instantiation, reflection, or data conversion.
     */
    @Override
    public T process(ImportLine input) throws Exception {
        if (input == null || input.getImlText() == null) {
            return null;
        }

        T targetInstance = targetType.getDeclaredConstructor().newInstance();
        FieldSet fieldSet = tokenizer.tokenize(input.getImlText());

        // Iterate through the cached metadata, which is much faster than reflecting on every input.
        for (CachedField field : this.cachedFields) {
            if (field.enable) {
                if (field.mandatory && StringUtils.isBlank(fieldSet.readString(field.propertyName))) {
                    throw new IllegalArgumentException("Mandatory field '" + field.propertyName + "' (" + field.fieldType + ") is blank.");
                }

                Object value = switch (field.fieldType.toUpperCase()) {
                    case "STRING" -> fieldSet.readString(field.propertyName);
                    case "BIGDECIMAL" -> fieldSet.readBigDecimal(field.propertyName);
                    case "DATE" -> fieldSet.readDate(field.propertyName(), field.pattern());
                    case "INT" -> fieldSet.readInt(field.propertyName);
                    case "LONG" -> fieldSet.readLong(field.propertyName);
                    case "DOUBLE" -> fieldSet.readDouble(field.propertyName);
                    default ->
                            throw new IllegalArgumentException("Unsupported field type in mapping: " + field.fieldType);
                };

                // Apply transformer if present
                if (StringUtils.isNotBlank(field.transformer())) {
                    value = applyTransformer(field.transformer(), value);
                }


                // Invoke the pre-fetched setter method.
                field.setter.invoke(targetInstance, value);
            }
        }

        return targetInstance;
    }

    /**
     * Applies a transformation to the given value using a Spring SpEL expression.
     *
     * @param transformer the SpEL expression representing the transformer class and method to invoke
     * @param value the value to be transformed and passed to the transformer
     * @return the transformed value as returned by the transformer
     */
    private Object applyTransformer(String transformer, Object value) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("value", value);
        // Example transformer: "com.example.TransformerClass.transform(#value)"
        return parser.parseExpression(transformer).getValue(context);
    }

    /**
     * Configures the {@link FixedLengthTokenizer} with the field names and column ranges.
     * This method also triggers the caching of reflection metadata for performance optimization.
     * It is called by Spring after all bean properties have been set.
     *
     * @throws IllegalStateException if mappingFields are not provided.
     * @throws NoSuchMethodException if a setter method defined in the mapping does not exist on the target object.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (mappingFields == null || mappingFields.isEmpty()) {
            throw new IllegalStateException("MappingFields cannot be null or empty.");
        }

        tokenizer = new FixedLengthTokenizer();

        // Define the names for the fields
        String[] names = mappingFields.stream().map(MappingFields::getProperty).toArray(String[]::new);
        tokenizer.setNames(names);

        // Define column ranges
        Range[] ranges = mappingFields.stream()
                .map(mf -> new Range(mf.getOffset() + 1, mf.getOffset() + mf.getLength()))
                .toArray(Range[]::new);
        tokenizer.setColumns(ranges);

        // --- OPTIMIZATION: Cache reflection metadata here ---
        this.cachedFields = new ArrayList<>();
        for (MappingFields field : this.mappingFields) {
            cacheField(field);
        }
    }

    /**
     * Helper method to convert a type string from the mapping to a {@link Class} object.
     *
     * @param type The type as a string (e.g., "STRING", "BIGDECIMAL").
     * @return The corresponding {@link Class} object.
     * @throws IllegalArgumentException if the type string is not supported.
     */
    private Class<?> getClassForType(String type) {
        return switch (type.toUpperCase()) {
            case "STRING" -> String.class;
            case "BIGDECIMAL" -> BigDecimal.class;
            case "DATE" -> Date.class;
            case "INT" -> int.class;
            case "LONG" -> long.class;
            case "DOUBLE" -> double.class;
            default -> throw new IllegalArgumentException("Unsupported class type in mapping: " + type);
        };
    }

    /**
     * Caches reflection metadata for a single {@link MappingFields} object.
     * This involves finding the appropriate setter method on the target class and storing it,
     * along with other relevant details, for later use in the {@link #process} method.
     *
     * @param field The {@link MappingFields} to cache metadata for.
     * @throws NoSuchMethodException if the corresponding setter method is not found on the target class.
     */
    private void cacheField(MappingFields field) throws NoSuchMethodException {
        String propertyName = field.getProperty();
        String fieldType = field.getType();
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Class<?> paramType = getClassForType(fieldType);
        Method setter = targetType.getMethod(setterName, paramType);

        this.cachedFields.add(new CachedField(propertyName, fieldType, field.getPattern(), setter, Constants.YES.equals(field.getMandatory()), field.getTransformer(), Constants.YES.equals(field.getEnable())));
    }
}
