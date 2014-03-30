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

import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Fetches the defined numbers, adds properties and increments all numbers.
 */
@Mojo(name="inc", threadSafe=true)
public final class IncrementNumbersMojo extends AbstractPropertyHelperMojo
{
    /**
     * If set to true, all generated properties are persisted to disk using a properties file.
     */
    @Parameter(defaultValue="true")
    private boolean persist = true;

    @Override
    protected void doExecute() throws Exception
    {
        LOG.debug("Running IncrementNumbers");

        loadPropertyElements();

        final List<NumberField> numberFields = getNumbers();

        if (numberFields != null) {
            for (NumberField nf : numberFields) {
                nf.increment();
            }
        }

        if (persist) {
            valueCache.persist();
        }
    }
}
