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

import java.util.Map;
import java.util.Properties;

import com.google.common.base.Optional;

public interface ValueProvider
{
    Optional<String> getValue();

    void setValue(String value);

    ValueProvider NULL_PROVIDER = new NullProvider();

    static class NullProvider implements ValueProvider
    {
        private NullProvider()
        {
        }

        @Override
        public void setValue(final String value)
        {
        }

        @Override
        public Optional<String> getValue()
        {
            return Optional.absent();
        }
    }

    static class StaticValueProvider implements ValueProvider
    {
        private String value;

        StaticValueProvider()
        {
        }

        @Override
        public void setValue(final String value)
        {
            this.value = value;
        }

        @Override
        public Optional<String> getValue()
        {
            return Optional.fromNullable(value);
        }
    }

    static class MapValueProvider implements ValueProvider
    {
        private final Map<String, String> values;
        private final String valueName;

        MapValueProvider(final Map<String, String> values, final String valueName)
        {
            this.valueName = checkNotNull(valueName, "valueName is null");
            this.values = values;
        }

        @Override
        public void setValue(final String value)
        {
            checkNotNull(value, "value is null");
            values.put(valueName, value);
        }

        @Override
        public Optional<String> getValue()
        {
            return Optional.fromNullable(values.get(valueName));
        }
    }

    static class PropertyProvider implements ValueProvider
    {
        private final Properties props;
        private final String propertyName;

        PropertyProvider(final Properties props, final String propertyName)
        {
            this.props = props;
            this.propertyName = checkNotNull(propertyName, "propertyName is null");
        }

        @Override
        public void setValue(final String value)
        {
            checkNotNull(value, "value is null");
            props.setProperty(propertyName, value);
        }

        @Override
        public Optional<String> getValue()
        {
            return Optional.fromNullable(props.getProperty(propertyName));
        }
    }
}

