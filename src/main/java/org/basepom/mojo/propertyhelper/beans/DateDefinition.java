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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class DateDefinition extends AbstractDefinition<DateDefinition>
{
    /** Timezone for this date. Field injected by Maven. */
    private String timezone = null;

    /** Value for this date. Field injected by Maven. */
    private Long value = null;

    public DateDefinition()
    {
    }

    public Optional<String> getTimezone()
    {
        return Optional.fromNullable(timezone);
    }

    @VisibleForTesting
    public DateDefinition setTimezone(final String timezone)
    {
        this.timezone = checkNotNull(timezone, "timezone is null");
        return this;
    }

    public Optional<Long> getValue()
    {
        return Optional.fromNullable(value);
    }

    @VisibleForTesting
    public DateDefinition setValue(final Long value)
    {
        this.value = checkNotNull(value, "value is null");
        return this;
    }
}
