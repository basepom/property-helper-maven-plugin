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
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;

import org.basepom.mojo.propertyhelper.beans.AbstractDefinition;
import org.basepom.mojo.propertyhelper.beans.IgnoreWarnFailCreate;
import org.basepom.mojo.propertyhelper.util.Log;

public class ValueCache
{
    private static final Log LOG = Log.findLog();

    /** Cache for values files loaded from disk */
    private Map<File, ValueCacheEntry> valueFiles = Maps.newHashMap();

    private final Map<String, String> ephemeralValues = Maps.newHashMap();

    public ValueProvider getValueProvider(final AbstractDefinition<?> definition)
        throws IOException
    {
        final Optional<Map<String, String>> values = getValues(definition);
        if (!values.isPresent()) {
            final String name = definition.getPropertyName();
            final Optional<String> value = definition.getInitialValue();
            if (value.isPresent()) {
                ephemeralValues.put(name, value.get());
            }
            return new ValueProvider.MapValueProvider(ephemeralValues, name);
        }
        else {
            return ValueCache.findCurrentValueProvider(values.get(), definition);
        }
    }

    @VisibleForTesting
    static ValueProvider findCurrentValueProvider(final Map<String, String> values, final AbstractDefinition<?> definition)
    {
        checkNotNull(values, "values is null");
        final String name = definition.getPropertyName();
        final boolean hasValue = values.containsKey(name);

        final boolean createProperty = IgnoreWarnFailCreate.checkState(definition.getOnMissingProperty(), hasValue, name);

        if (hasValue) {
            return new ValueProvider.MapValueProvider(values, name);
        }
        else if (createProperty) {
            if (definition.getInitialValue().isPresent()) {
                values.put(name, definition.getInitialValue().get());
            }
            return new ValueProvider.MapValueProvider(values, name);
        }
        else {
            return ValueProvider.NULL_PROVIDER;
        }
    }

    @VisibleForTesting
    Optional<Map<String, String>> getValues(final AbstractDefinition<?> definition)
        throws IOException
    {
        final Optional<File> definitionFile = definition.getPropertyFile();

        // Ephemeral, so return null.
        if (!definitionFile.isPresent()) {
            return Optional.absent();
        }

        ValueCacheEntry cacheEntry;
        final File canonicalFile = definitionFile.get().getCanonicalFile();

        // Throws an exception if the file must exist and does not.
        final boolean createFile = IgnoreWarnFailCreate.checkState(definition.getOnMissingFile(), canonicalFile.exists(), definitionFile.get().getCanonicalPath());

        cacheEntry = valueFiles.get(canonicalFile);

        if (cacheEntry != null) {
            // If there is a cache hit, something either has loaded the file
            // or another property has already put in a creation order.
            // Make sure that if this number has a creation order it is obeyed.
            if (createFile) {
                cacheEntry.doCreate();
            }
        }
        else {
            // Try loading or creating properties.
            final Properties props = new Properties();

            if (!canonicalFile.exists()) {
                cacheEntry = new ValueCacheEntry(props, false, createFile); // does not exist
                valueFiles.put(canonicalFile, cacheEntry);
            }
            else {
                if (canonicalFile.isFile() && canonicalFile.canRead()) {
                    final Closer closer = Closer.create();
                    try {
                        final InputStream stream = closer.register(new FileInputStream(canonicalFile));
                        props.load(stream);
                        cacheEntry = new ValueCacheEntry(props, true, createFile);
                        valueFiles.put(canonicalFile, cacheEntry);
                    }
                    finally {
                        closer.close();
                    }
                }
                else {
                    throw new IllegalStateException(format("Can not load %s, not a file!", definitionFile.get().getCanonicalPath()));
                }
            }
        }

        return Optional.of(cacheEntry.getValues());
    }

    public void persist() throws IOException
    {
        for (final Map.Entry<File, ValueCacheEntry> entries : valueFiles.entrySet())
        {
            final ValueCacheEntry entry = entries.getValue();
            if (!entry.isDirty()) {
                continue;
            }
            final File file = entries.getKey();
            if (entry.isExists() || entry.isCreate()) {
                checkNotNull(file, "no file defined, can not persist!");
                final File oldFile = new File(file.getCanonicalPath() + ".bak");

                if (entry.isExists()) {
                    checkState(file.exists(), "'%s' should exist!", file.getCanonicalPath());
                    // unlink an old file if necessary
                    if (oldFile.exists()) {
                        checkState(oldFile.delete(), "Could not delete '%s'", file.getCanonicalPath());
                    }
                }

                final File folder = file.getParentFile();
                if (!folder.exists()) {
                    checkState(folder.mkdirs(), "Could not create folder '%s'", folder.getCanonicalPath());
                }

                final Closer closer = Closer.create();

                final File newFile = new File(file.getCanonicalPath() + ".new");
                try {
                    final OutputStream stream = closer.register(new FileOutputStream(newFile));
                    entry.store(stream, "created by property-helper-maven-plugin");
                }
                finally {
                    closer.close();
                }

                if (file.exists()) {
                    if (!file.renameTo(oldFile)) {
                        LOG.warn("Could not rename '%s' to '%s'!", file, oldFile);
                    }
                }

                if (!file.exists()) {
                    if (!newFile.renameTo(file)) {
                        LOG.warn("Could not rename '%s' to '%s'!", newFile, file);
                    }
                }
            }
        }
    }

    public static class ValueCacheEntry
    {
        private final Map<String, String> values = Maps.newHashMap();

        private final boolean exists;

        private boolean create;

        private boolean dirty = false;

        ValueCacheEntry(@Nonnull final Properties props,
                               final boolean exists,
                               final boolean create)
        {
            checkNotNull(props, "props is null");

            values.putAll(Maps.fromProperties(props));

            this.exists = exists;
            this.create = create;
        }

        public void store(final OutputStream out, final String comment) throws IOException
        {
            final Properties p = new Properties();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                p.setProperty(entry.getKey(), entry.getValue());
            }
            p.store(out, comment);
        }

        public boolean isDirty()
        {
            return dirty;
        }

        public void dirty()
        {
            this.dirty = true;
        }

        public Map<String, String> getValues()
        {
            return new ForwardingMap<String, String>() {
                @Override
                protected Map<String, String> delegate()
                {
                    return values;
                }

                @Override
                public String remove(Object object) {
                    dirty();
                    return super.remove(object);
                }

                @Override
                public void clear() {
                    dirty();
                    super.clear();
                }

                @Override
                public String put(String key, String value) {
                    final String oldValue = super.put(key, value);
                    if (!Objects.equal(value, oldValue)) {
                        dirty();
                    }
                    return oldValue;
                }

                @Override
                public void putAll(Map<? extends String, ? extends String> map) {
                    dirty();
                    super.putAll(map);
                }


            };
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
            dirty();
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
            ValueCacheEntry that = (ValueCacheEntry) other;
            return Objects.equal(this.values, that.values)
                            && Objects.equal(this.dirty, that.dirty)
                            && Objects.equal(this.exists, that.exists)
                            && Objects.equal(this.create, that.create);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(values, dirty, exists, create);
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                            .add("values", values)
                            .add("exists", exists)
                            .add("create", create)
                            .add("dirty", dirty)
                            .toString();
        }
    }
}
