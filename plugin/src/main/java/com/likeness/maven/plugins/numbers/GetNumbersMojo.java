package com.likeness.maven.plugins.numbers;

import java.io.IOException;
import java.util.List;

import com.likeness.maven.plugins.numbers.beans.DateDefinition;
import com.likeness.maven.plugins.numbers.beans.StringDefinition;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.likeness.maven.plugins.numbers.beans.NumberDefinition;
import com.likeness.maven.plugins.numbers.util.Log;


/**
 * Fetches the defined numbers and add properties.
 *
 * @goal get
 */
public class GetNumbersMojo extends AbstractNumbersMojo
{
    private final Log LOG = Log.findLog();

    private final PropertyCache propertyCache = new PropertyCache();

    protected void doExecute() throws Exception
    {
        LOG.debug("Running GetNumbers");
        final List<PropertyElement> propertyElements = Lists.newArrayList();
        propertyElements.addAll(createNumbers(numbers));
        propertyElements.addAll(createStrings(strings));
        propertyElements.addAll(createDates(dates));

        for (PropertyElement pe : propertyElements) {
            if (pe.isExport()) {
                final String value = pe.getPropertyValue();
                if (value != null) {
                    project.getProperties().setProperty(pe.getPropertyName(), value);
                    LOG.info("Exporting Property name: %s, value: %s", pe.getPropertyName(), value);
                }
            }
            else {
                LOG.info("Property name: %s, value: %s", pe.getPropertyName(), pe.getPropertyValue());
            }
        }
    }

    private List<NumberField> createNumbers(final NumberDefinition [] numberDefinitions)
        throws IOException
    {
        final List<NumberField> result = Lists.newArrayList();

        if (!ArrayUtils.isEmpty(numberDefinitions)) {
            for (NumberDefinition numberDefinition : numberDefinitions) {
                numberDefinition.check();
                final ValueProvider numberValue = propertyCache.getPropertyValue(numberDefinition);
                final NumberField numberField = new NumberField(numberDefinition, numberValue);
                result.add(numberField);
            }
        }
        return result;
    }

    private List<StringField> createStrings(final StringDefinition[] stringDefinitions)
        throws IOException
    {
        final List<StringField> result = Lists.newArrayList();

        if (!ArrayUtils.isEmpty(stringDefinitions)) {
            for (StringDefinition stringDefinition : stringDefinitions) {
                stringDefinition.check();
                final ValueProvider stringValue = propertyCache.getPropertyValue(stringDefinition);
                final StringField stringField = new StringField(stringDefinition, stringValue);
                result.add(stringField);
            }
        }
        return result;
    }

    private List<DateField> createDates(final DateDefinition[] dateDefinitions)
        throws IOException
    {
        final List<DateField> result = Lists.newArrayList();

        if (!ArrayUtils.isEmpty(dateDefinitions)) {
            for (DateDefinition dateDefinition : dateDefinitions) {
                dateDefinition.check();
                final ValueProvider dateValue = propertyCache.getPropertyValue(dateDefinition);
                final DateField dateField = new DateField(dateDefinition, dateValue);
                result.add(dateField);
            }
        }
        return result;
    }
}
