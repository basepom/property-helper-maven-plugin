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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.basepom.mojo.propertyhelper.beans.AbstractDefinition;
import org.basepom.mojo.propertyhelper.beans.IgnoreWarnFailCreate;
import org.basepom.mojo.propertyhelper.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;

public class PropertyCache
{
    private static final Log LOG = Log.findLog();

    /** Cache for properties files loaded from disk */
    private Map<File, PropertyCacheEntry> propFiles = Maps.newHashMap();

    private final Properties ephemeralProperties = new Properties();

    public ValueProvider getPropertyValue(final AbstractDefinition<?> definition)
        throws IOException
    {
        final Properties props = getProperties(definition);
        if (props == null) {
            final String propName = definition.getPropertyName();
            final Optional<String> value = definition.getInitialValue();
            if (value.isPresent()) {
                ephemeralProperties.setProperty(propName, value.get());
            }
            return new ValueProvider.PropertyProvider(ephemeralProperties, propName);
        }
        else {
            return findCurrentValue(props, definition);
        }
    }

    @VisibleForTesting
    ValueProvider findCurrentValue(final Properties props, final AbstractDefinition<?> definition)
    {
        final String propName = definition.getPropertyName();
        final boolean hasProperty = props.containsKey(propName);

        final boolean createProperty = IgnoreWarnFailCreate.checkState(definition.getOnMissingProperty(), hasProperty, propName);

        if (hasProperty) {
            return new ValueProvider.PropertyProvider(props, propName);
        }
        else if (createProperty) {
            props.setProperty(propName, definition.getInitialValue().orNull());
            return new ValueProvider.PropertyProvider(props, propName);
        }
        else {
            return ValueProvider.NULL_PROVIDER;
        }
    }

    @VisibleForTesting
    Properties getProperties(final AbstractDefinition<?> definition)
        throws IOException
    {
        final Optional<File> definitionPropertyFile = definition.getPropertyFile();

        // Ephemeral, so return null.
        if (!definitionPropertyFile.isPresent()) {
            return null;
        }

        PropertyCacheEntry propertyCacheEntry;
        final File propertyFile = definitionPropertyFile.get().getCanonicalFile();

        // Throws an exception if the file must exist and does not.
        final boolean createFile = IgnoreWarnFailCreate.checkState(definition.getOnMissingFile(), propertyFile.exists(), definitionPropertyFile.get().getCanonicalPath());

        propertyCacheEntry = propFiles.get(propertyFile);

        if (propertyCacheEntry != null) {
            // If there is a cache hit, something either has loaded the file
            // or another property has already put in a creation order.
            // Make sure that if this number has a creation order it is obeyed.
            if (createFile) {
                propertyCacheEntry.doCreate();
            }
        }
        else {
            // Try loading or creating properties.
            final Properties props = new Properties();

            if (!propertyFile.exists()) {
                propertyCacheEntry = new PropertyCacheEntry(props, false, createFile); // does not exist
            }
            else {
                if (propertyFile.isFile() && propertyFile.canRead()) {
                    final Closer closer = Closer.create();
                    InputStream stream = null;
                    try {
                        stream = closer.register(new FileInputStream(propertyFile));
                        props.load(stream);
                        propertyCacheEntry = new PropertyCacheEntry(props, true, createFile);
                        propFiles.put(propertyFile, propertyCacheEntry);
                    }
                    finally {
                        closer.close();
                    }
                }
                else {
                    throw new IllegalStateException(format("Can not load %s, not a file!", definitionPropertyFile.get().getCanonicalPath()));
                }
            }
        }

        return propertyCacheEntry.getProps();
    }

    public void persist() throws IOException
    {
        for (final Map.Entry<File, PropertyCacheEntry> propFile : propFiles.entrySet())
        {
            final PropertyCacheEntry entry = propFile.getValue();
            final File file = propFile.getKey();
            if (entry.isExists() || entry.isCreate()) {
                Preconditions.checkNotNull(file, "no file defined, can not persist!");
                final File oldFile = new File(file.getCanonicalPath() + ".bak");

                if (entry.isExists()) {
                    Preconditions.checkState(file.exists(), "File %s should exist!", file.getCanonicalPath());
                    // unlink an old file if necessary
                    if (oldFile.exists()) {
                        Preconditions.checkState(oldFile.delete(), "Could not delete '%s'", file.getCanonicalPath());
                    }
                }
                final File newFile = new File(file.getCanonicalPath() + ".new");
                final Closer closer = Closer.create();
                OutputStream stream = null;
                try {
                    stream = closer.register(new FileOutputStream(newFile));
                    entry.getProps().store(stream, "created by property-helper-maven-plugin");
                }
                finally {
                    closer.close();
                }

                if (file.exists()) {
                    if (file.renameTo(oldFile)) {
                        if (!newFile.renameTo(file)) {
                            LOG.warn("Could not rename '%s' to '%s'!", newFile, file);
                        }
                    }
                    else {
                        LOG.warn("Could not rename '%s' to '%s'!", file, oldFile);
                    }
                }
            }
        }
    }

    public static class PropertyCacheEntry
    {
        private final Properties props;

        private final boolean exists;

        private boolean create;

        PropertyCacheEntry(@Nonnull final Properties props,
                               final boolean exists,
                               final boolean create)
        {
            Preconditions.checkNotNull(props, "Properties element can not be null!");
            this.props = props;
            this.exists = exists;
            this.create = create;
        }

        public Properties getProps()
        {
            return props;
        }

        public boolean isExists()
        {
            return exists;
        }

        public boolean isCreate()
        {
            return create;
        }

        public void doCreate()
        {
            this.create = true;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other == this) {
                return true;
            }
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            PropertyCacheEntry that = (PropertyCacheEntry) other;
            return Objects.equal(this.props, that.props)
                            && Objects.equal(this.exists, that.exists)
                            && Objects.equal(this.create, that.create);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(props, exists, create);
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                            .add("props", props)
                            .add("exists", exists)
                            .add("create", create)
                            .toString();
        }
    }
}
