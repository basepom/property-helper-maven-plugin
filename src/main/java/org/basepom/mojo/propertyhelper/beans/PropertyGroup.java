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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class PropertyGroup
{
    /** Property group id. */
    private String id;

    /** Activate the group if the current project version does not contain SNAPSHOT- */
    private boolean activeOnRelease = true;

    /** Activate the group if the current project version contains SNAPSHOT- */
    private boolean activeOnSnapshot = true;

    /** Action if this property group defines a duplicate property. */
    private IgnoreWarnFail onDuplicateProperty = IgnoreWarnFail.FAIL;

    /** Action if any property from that group could not be defined. */
    private IgnoreWarnFail onMissingProperty = IgnoreWarnFail.FAIL;

    /** Property definitions in this group. */
    private Map<String, String> properties = ImmutableMap.of();

    @VisibleForTesting
    PropertyGroup(final String id,
                  final boolean activeOnRelease,
                  final boolean activeOnSnapshot,
                  final IgnoreWarnFail onDuplicateProperty,
                  final IgnoreWarnFail onMissingProperty,
                  final Map<String, String> properties)
    {
        this();

        this.id = id;
        this.activeOnRelease = activeOnRelease;
        this.activeOnSnapshot = activeOnSnapshot;
        this.onDuplicateProperty = checkNotNull(onDuplicateProperty, "onDuplicateProperty is null");
        this.onMissingProperty = checkNotNull(onMissingProperty, "onMissingProperty is null");
        this.properties = ImmutableMap.copyOf(checkNotNull(properties, "properties is null"));
    }

    public PropertyGroup()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public boolean isActiveOnRelease()
    {
        return activeOnRelease;
    }

    public void setActiveOnRelease(final boolean activeOnRelease)
    {
        this.activeOnRelease = activeOnRelease;
    }

    public boolean isActiveOnSnapshot()
    {
        return activeOnSnapshot;
    }

    public void setActiveOnSnapshot(final boolean activeOnSnapshot)
    {
        this.activeOnSnapshot = activeOnSnapshot;
    }

    public IgnoreWarnFail getOnDuplicateProperty()
    {
        return onDuplicateProperty;
    }

    public void setOnDuplicateProperty(final String onDuplicateProperty)
    {
        checkNotNull(onDuplicateProperty, "onDuplicateProperty is null");
        this.onDuplicateProperty = IgnoreWarnFail.forString(onDuplicateProperty);
    }

    public IgnoreWarnFail getOnMissingProperty()
    {
        return onMissingProperty;
    }

    public void setOnMissingProperty(final String onMissingProperty)
    {
        checkNotNull(onMissingProperty, "onMissingProperty is null");
        this.onMissingProperty = IgnoreWarnFail.forString(onMissingProperty);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(final Properties properties)
    {
        this.properties = ImmutableMap.copyOf(Maps.fromProperties(checkNotNull(properties, "properties is null")));
    }

    public void check()
    {
    }

    public Iterator<String> getPropertyNames()
    {
        return Iterators.transform(properties.keySet().iterator(), Functions.toStringFunction());
    }

    public String getPropertyValue(final String propertyName, final Map<String, String> propElements)
    {
        if (!properties.containsKey(propertyName)) {
            return "";
        }

        String propertyValue = properties.get(propertyName);

        for (Map.Entry<String, String> entry : propElements.entrySet()) {
            final String key = "#{" + entry.getKey() + "}";
            propertyValue = propertyValue.replace(key, entry.getValue());
        }
        // Replace all remaining groups.
        final String result = propertyValue.replaceAll("\\#\\{.*\\}", "");
        IgnoreWarnFail.checkState(getOnMissingProperty(), propertyValue.equals(result), "property");
        return result;
    }
}
