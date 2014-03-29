/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.basepom.mojo.propertyhelper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.basepom.mojo.propertyhelper.beans.DateDefinition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateField implements PropertyElement
{
    private final DateDefinition dateDefinition;
    private final ValueProvider valueProvider;

    public static List<DateField> createDates(final PropertyCache propertyCache, final DateDefinition[] dateDefinitions)
        throws IOException
    {
        checkNotNull(propertyCache, "propertyCache is null");
        checkNotNull(dateDefinitions, "dateDefinitions is null");

        final ImmutableList.Builder<DateField> result = ImmutableList.builder();

        for (DateDefinition dateDefinition : dateDefinitions) {
            dateDefinition.check();
            final ValueProvider dateValue = propertyCache.getPropertyValue(dateDefinition);
            final DateField dateField = new DateField(dateDefinition, dateValue);
            result.add(dateField);
        }
        return result.build();
    }

    public DateField(final DateDefinition dateDefinition, final ValueProvider valueProvider)
    {
        this.dateDefinition = dateDefinition;
        this.valueProvider = valueProvider;
    }

    @Override
    public String getPropertyName()
    {
        return dateDefinition.getId();
    }

    @Override
    public Optional<String> getPropertyValue()
    {
        final DateTimeZone timeZone = dateDefinition.getTimezone().isPresent()
                        ? DateTimeZone.forID(dateDefinition.getTimezone().get())
                        : DateTimeZone.getDefault();

        DateTime date = getDateTime(valueProvider.getValue(), timeZone);

        if (date == null && dateDefinition.getValue().isPresent()) {
            date = new DateTime(dateDefinition.getValue().get(), timeZone);
        }

        if (date == null) {
            date = new DateTime(timeZone);
        }

        final Optional<String> format = dateDefinition.getFormat();

        final String result;
        if (format.isPresent()) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(format.get());
            result = formatter.print(date);
        }
        else {
            result = date.toString();
        }

        valueProvider.setValue(result);
        return Optional.of(result);
    }

    private DateTime getDateTime(final Optional<String> value, final DateTimeZone timeZone)
    {
        if (!value.isPresent()) {
            return null;
        }
        try {
            return new DateTime(Long.parseLong(value.get()), timeZone);
        }
        catch (NumberFormatException nfe) {
            return new DateTime(value.get(), timeZone);
        }
    }

    @Override
    public boolean isExport()
    {
        return dateDefinition.isExport();
    }

    @Override
    public String toString()
    {
        return getPropertyValue().or("");
    }
}
