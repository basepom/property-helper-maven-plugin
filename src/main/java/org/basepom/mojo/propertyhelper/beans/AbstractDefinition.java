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
package org.basepom.mojo.propertyhelper.beans;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

public abstract class AbstractDefinition<T extends AbstractDefinition<T>>
{
    /** Name of the build property to define. Field injected by Maven. */
    private String id = null;

    /** True skips the parsing of this definition. Field injected by Maven. */
    private boolean skip = false;

    /** Whether to export this number directly. Field injected by Maven. */
    private boolean export = false;

    /** Name of the property from the properties file. Field injected by Maven. */
    private String propertyName = null;

    /** Name of the properties file to persist the count. Field injected by Maven. */
    private File propertyFile = null;

    /** What to do when the property is missing from the file. Field injected by Maven. */
    private IgnoreWarnFailCreate onMissingFile = IgnoreWarnFailCreate.FAIL;

    /** What to do when the property is missing from the file. Field injected by Maven. */
    private IgnoreWarnFailCreate onMissingProperty = IgnoreWarnFailCreate.FAIL;

    /** The initial value for this field. Field injected by Maven. */
    private String initialValue = null;

    /** Format for this element. Field injected by Maven. */
    private String format = null;

    protected AbstractDefinition()
    {
    }

    public String getId()
    {
        return id;
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setId(final String id)
    {
        this.id = checkNotNull(id, "id is null").trim();
        return (T) this;
    }

    public boolean isSkip()
    {
        return skip;
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setSkip(final boolean skip)
    {
        this.skip = skip;
        return (T) this;
    }

    public Optional<String> getInitialValue()
    {
        return Optional.fromNullable(initialValue);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setInitialValue(final String initialValue)
    {
        this.initialValue = checkNotNull(initialValue, "initialValue is null").trim();
        return (T) this;
    }

    public boolean isExport()
    {
        return export;
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setExport(final boolean export)
    {
        this.export = export;
        return (T) this;
    }

    public String getPropertyName()
    {
        return Objects.firstNonNull(propertyName, id);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setPropertyName(final String propertyName)
    {
        this.propertyName = checkNotNull(propertyName, "propertyName is null").trim();
        return (T) this;
    }

    public Optional<File> getPropertyFile()
    {
        return Optional.fromNullable(propertyFile);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setPropertyFile(final File propertyFile)
    {
        this.propertyFile = checkNotNull(propertyFile, "propertyFile is null");
        return (T) this;
    }

    public IgnoreWarnFailCreate getOnMissingFile()
    {
        return onMissingFile;
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setOnMissingFile(final String onMissingFile)
    {
        checkNotNull(onMissingFile, "onMissingFile is null");
        this.onMissingFile = IgnoreWarnFailCreate.forString(onMissingFile);
        return (T) this;
    }

    public IgnoreWarnFailCreate getOnMissingProperty()
    {
        return onMissingProperty;
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setOnMissingProperty(final String onMissingProperty)
    {
        checkNotNull(onMissingProperty, "onMissingProperty is null");
        this.onMissingProperty = IgnoreWarnFailCreate.forString(onMissingProperty);
        return (T) this;
    }

    public Optional<String> getFormat()
    {
        return Optional.fromNullable(format);
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public T setFormat(final String format)
    {
        this.format = checkNotNull(format, "format is null");
        return (T) this;
    }

    public void check()
    {
        checkState(id != null, "the id element must not be empty!");
    }
}
