package com.barebonebatch.common.processor;

import com.barebonebatch.common.domain.ImportLine;
import com.barebonebatch.common.domain.MappingFields;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportLineProcessor<T> implements ItemProcessor<ImportLine, T>, InitializingBean {

    /**
     * A private static inner class to hold cached reflection metadata.
     * This avoids expensive lookups during the process() method.
     */
    private record CachedField(String propertyName, String fieldType, String pattern, Method setter) {
    }

    private FixedLengthTokenizer tokenizer;
    private final List<MappingFields> mappingFields;
    private final Class<T> targetType;
    private List<CachedField> cachedFields;

    public ImportLineProcessor(Class<T> targetType, List<MappingFields> mappingFields) {
        this.mappingFields = mappingFields;
        this.targetType = targetType;
    }

    @Override
    public T process(ImportLine item) throws Exception {
        if (item == null || item.getImlText() == null) {
            return null;
        }

        T targetInstance = targetType.getDeclaredConstructor().newInstance();
        FieldSet fieldSet = tokenizer.tokenize(item.getImlText());

        // Iterate through the cached metadata, which is much faster than reflecting on every item.
        for (CachedField field : this.cachedFields) {
            Object value = switch (field.fieldType.toUpperCase()) {
                case "STRING" -> fieldSet.readString(field.propertyName);
                case "BIGDECIMAL" -> fieldSet.readBigDecimal(field.propertyName);
                case "DATE" -> fieldSet.readDate(field.propertyName, field.pattern);
                case "INT" -> fieldSet.readInt(field.propertyName);
                case "LONG" -> fieldSet.readLong(field.propertyName);
                case "DOUBLE" -> fieldSet.readDouble(field.propertyName);
                default -> throw new IllegalArgumentException("Unsupported field type in mapping: " + field.fieldType);
            };

            // Read the value from the FieldSet using the correct type

            // Invoke the pre-fetched setter method.
            field.setter.invoke(targetInstance, value);
        }

        return targetInstance;
    }

    /**
     * Configure the tokenizer with the fixed-width layout.
     * This method also caches all reflection metadata for performance.
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
                .map(mf -> new Range(mf.getOffset(), mf.getOffset() + mf.getLength() - 1))
                .toArray(Range[]::new);
        tokenizer.setColumns(ranges);

        // --- OPTIMIZATION: Cache reflection metadata here ---
        this.cachedFields = new ArrayList<>();
        for (MappingFields field : this.mappingFields) {
            cacheField(field);
        }
    }

    /**
     * Helper method to convert a type string from the mapping to a Class object.
     */
    private Class<?> getClassForType(String type) {
        switch (type.toUpperCase()) {
            case "STRING":
                return String.class;
            case "BIGDECIMAL":
                return BigDecimal.class;
            case "DATE":
                return Date.class;
            case "INT":
                return int.class;
            case "LONG":
                return long.class;
            case "DOUBLE":
                return double.class;
            default:
                throw new IllegalArgumentException("Unsupported class type in mapping: " + type);
        }
    }

    private void cacheField(MappingFields field) throws NoSuchMethodException {
        String propertyName = field.getProperty();
        String fieldType = field.getType();
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Class<?> paramType = getClassForType(fieldType);
        Method setter = targetType.getMethod(setterName, paramType);

        this.cachedFields.add(new CachedField(propertyName, fieldType, field.getPattern(), setter));
    }
}
