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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.basepom.mojo.propertyhelper.beans.PropertyGroup;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class PropertyField implements PropertyElement
{
    private final String propertyName;
    private final String propertyValue;

    public static List<PropertyElement> createProperties(final Map<String, String> props, final PropertyGroup propertyGroup)
        throws IOException
    {
        final ImmutableList.Builder<PropertyElement> result = ImmutableList.builder();

        for (Iterator<String> it = propertyGroup.getPropertyNames(); it.hasNext(); ) {
            final String name = it.next();
            final String value = propertyGroup.getPropertyValue(name, props);
            result.add(new PropertyField(name, value));
        }
        return result.build();
    }

    PropertyField(final String propertyName, final String propertyValue)
    {
        this.propertyName = checkNotNull(propertyName, "propertyName is null");
        this.propertyValue = checkNotNull(propertyValue, "propertyValue is null");
    }

    @Override
    public String getPropertyName()
    {
        return propertyName;
    }

    @Override
    public Optional<String> getPropertyValue()
    {
        return Optional.of(propertyValue);
    }

    @Override
    public boolean isExport()
    {
        return true;
    }
}
