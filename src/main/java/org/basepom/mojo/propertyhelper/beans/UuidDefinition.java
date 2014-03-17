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

import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class UuidDefinition extends AbstractDefinition<UuidDefinition>
{
    /** Value for this uuid. Field injected by Maven. */
    private String value = null;

    public UuidDefinition()
    {
    }

    public Optional<UUID> getValue()
    {
        return value == null ? Optional.<UUID>absent() : Optional.of(UUID.fromString(value));
    }

    @VisibleForTesting
    public UuidDefinition setValue(final String value)
    {
        this.value = checkNotNull(value, "value is null");
        return this;
    }

    @Override
    public Optional<String> getInitialValue()
    {
        final Optional<String> initialValue = super.getInitialValue();
        return initialValue.isPresent() ? initialValue : Optional.of(UUID.randomUUID().toString());
    }
}
