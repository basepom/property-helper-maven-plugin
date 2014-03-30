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

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class PropertyGroup
{
    /** Property group id. */
    private String id;

    /** Activate the group if the current project version does not contain SNAPSHOT. Field injected by Maven. */
    private boolean activeOnRelease = true;

    /** Activate the group if the current project version contains SNAPSHOT. Field injected by Maven. */
    private boolean activeOnSnapshot = true;

    /** Action if this property group defines a duplicate property. Field injected by Maven. */
    private String onDuplicateProperty = "fail";

    /** Action if any property from that group could not be defined. Field injected by Maven. */
    private String onMissingProperty = "fail";

    /** Property definitions in this group. Field injected by Maven. */
    private Properties properties = new Properties();

    public PropertyGroup()
    {
    }

    public String getId()
    {
        return id;
    }

    public PropertyGroup setId(String id)
    {
        this.id = id;
        return this;
    }

    public boolean isActiveOnRelease()
    {
        return activeOnRelease;
    }

    public PropertyGroup setActiveOnRelease(final boolean activeOnRelease)
    {
        this.activeOnRelease = activeOnRelease;
        return this;
    }

    public boolean isActiveOnSnapshot()
    {
        return activeOnSnapshot;
    }

    public PropertyGroup setActiveOnSnapshot(final boolean activeOnSnapshot)
    {
        this.activeOnSnapshot = activeOnSnapshot;
        return this;
    }

    public IgnoreWarnFail getOnDuplicateProperty()
    {
        return IgnoreWarnFail.forString(onDuplicateProperty);
    }

    public PropertyGroup setOnDuplicateProperty(final String onDuplicateProperty)
    {
        IgnoreWarnFail.forString(onDuplicateProperty);
        this.onDuplicateProperty = onDuplicateProperty;
        return this;
    }

    public IgnoreWarnFail getOnMissingProperty()
    {
        return IgnoreWarnFail.forString(onMissingProperty);
    }

    public PropertyGroup setOnMissingProperty(final String onMissingProperty)
    {
        IgnoreWarnFail.forString(onMissingProperty);
        this.onMissingProperty = onMissingProperty;
        return this;
    }

    public Map<String, String> getProperties()
    {
        return ImmutableMap.copyOf(Maps.fromProperties(properties));
    }

    public PropertyGroup setProperties(final Properties properties)
    {
        this.properties = checkNotNull(properties, "properties is null");
        return this;
    }

    public PropertyGroup setProperties(final Map<String, String> properties)
    {
        checkNotNull(properties, "properties is null");
        this.properties = new Properties();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            this.properties.setProperty(entry.getKey(), entry.getValue());
        }
        return this;
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

        String propertyValue = properties.getProperty(propertyName);

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
