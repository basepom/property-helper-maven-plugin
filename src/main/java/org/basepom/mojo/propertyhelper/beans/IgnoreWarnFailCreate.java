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

import static java.lang.String.format;

import java.util.Locale;

import javax.annotation.Nonnull;

import org.basepom.mojo.propertyhelper.util.Log;

import com.google.common.base.Preconditions;

public enum IgnoreWarnFailCreate
{
    IGNORE, WARN, FAIL, CREATE;

    private static final Log LOG = Log.findLog();

    public static IgnoreWarnFailCreate forString(final String value)
    {
        Preconditions.checkArgument(value != null, "the value can not be null");
        return Enum.valueOf(IgnoreWarnFailCreate.class, value.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Reacts on a given thing existing or not existing.
     *
     * IGNORE: Do nothing.
     * WARN: Display a warning message if the thing does not exist, otherwise do nothing.
     * FAIL: Throws an exception if the thing does not exist.
     * CREATE: Suggest creation of the thing.
     *
     * Returns true if the thing should be create, false otherwise.
     */
    public static boolean checkState(@Nonnull final IgnoreWarnFailCreate iwfc, final boolean exists, final String thing)
    {
        if (exists) {
            return false;
        }

        switch (iwfc) {
            case IGNORE:
                return false;
            case WARN:
                LOG.warn("'%s' does not exist!", thing);
                return false;
            case FAIL:
                throw new IllegalStateException(format("'%s' does not exist!", thing));
            case CREATE:
                LOG.debug("'%s' does not exist, suggesting creation.", thing);
                return true;
            default:
                throw new IllegalStateException("Unknown state: " + iwfc);
        }
    }
}
