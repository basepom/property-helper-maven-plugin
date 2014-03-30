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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;



/**
 * Fetches the defined numbers and add properties.
 */
@Mojo(name="get", threadSafe=true)
public final class GetPropertiesMojo extends AbstractPropertyHelperMojo
{
    /**
     * If set to true, all generated properties are persisted to disk using a properties file.
     */
    @Parameter(defaultValue="false")
    private boolean persist = false;

    @Override
    protected void doExecute() throws Exception
    {
        LOG.debug("Running GetProperties");

        loadPropertyElements();

        if (persist) {
            LOG.debug("Persisting value cache");
            // Now dump the value cache back to the files if necessary.
            valueCache.persist();
        }
    }
}
