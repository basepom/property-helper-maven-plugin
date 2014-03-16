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

import static java.lang.String.format;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.basepom.mojo.propertyhelper.beans.UuidDefinition;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class UuidField implements PropertyElement
{
    private final UUID defaultValue;
    private final UuidDefinition uuidDefinition;
    private final ValueProvider valueProvider;

    public static List<UuidField> createUuids(final PropertyCache propertyCache, final UuidDefinition[] uuidDefinitions)
        throws IOException
    {
        final ImmutableList.Builder<UuidField> result = ImmutableList.builder();

        if (!ArrayUtils.isEmpty(uuidDefinitions)) {
            for (UuidDefinition uuidDefinition : uuidDefinitions) {
                uuidDefinition.check();
                final ValueProvider uuidValue = propertyCache.getPropertyValue(uuidDefinition);
                final UuidField uuidField = new UuidField(uuidDefinition, uuidValue);
                result.add(uuidField);
            }
        }
        return result.build();
    }

    public UuidField(final UuidDefinition uuidDefinition, final ValueProvider valueProvider)
    {
        this(uuidDefinition, valueProvider, UUID.randomUUID());
    }

    @VisibleForTesting
    UuidField(final UuidDefinition uuidDefinition, final ValueProvider valueProvider, final UUID defaultValue)
    {
        this.defaultValue = checkNotNull(defaultValue, "defaultValue is null");
        this.uuidDefinition = checkNotNull(uuidDefinition, "uuidDefinition is null");
        this.valueProvider = checkNotNull(valueProvider, "valueProvider is null");
    }

    @Override
    public String getPropertyName()
    {
        return uuidDefinition.getId();
    }

    @Override
    public Optional<String> getPropertyValue()
    {
        // Only add the value from the provider if it is not null.
        UUID result = defaultValue;
        final Optional<String> propValue = valueProvider.getValue();

        if (propValue.isPresent()) {
            result = UUID.fromString(propValue.get());
        }
        else {
            final Optional<UUID> definedValue = uuidDefinition.getValue();
            if (definedValue.isPresent()) {
                result = definedValue.get();
            }
        }

        final Optional<String> format = uuidDefinition.getFormat();
        return Optional.of(format.isPresent() ? format(format.get(), result) : result.toString());
    }

    @Override
    public boolean isExport()
    {
        return uuidDefinition.isExport();
    }

    @Override
    public String toString()
    {
        return getPropertyValue().or("");
    }
}
