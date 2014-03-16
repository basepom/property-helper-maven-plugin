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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;


public class TestNumberDefinition
{
    @Test
    public void testValid()
    {
        final NumberDefinition nd = new NumberDefinition()
        .setId("hello")
        .setExport(true)
        .setInitialValue("1");
        nd.check();
    }

    @Test
    public void testValid2()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setId("hello");
        nd.check();
    }

    @Test
    public void testDefaults()
    {
        final String id = UUID.randomUUID().toString();
        final NumberDefinition nd = new NumberDefinition();
        nd.setId(id);
        nd.check();
        Assert.assertEquals(id, nd.getId());
        Assert.assertEquals("0", nd.getInitialValue().get());
        Assert.assertEquals(0, nd.getFieldNumber());
        Assert.assertEquals(1, nd.getIncrement());
        Assert.assertEquals(id, nd.getPropertyName());
        Assert.assertFalse(nd.getPropertyFile().isPresent());
        Assert.assertEquals(IgnoreWarnFailCreate.FAIL, nd.getOnMissingFile());
        Assert.assertEquals(IgnoreWarnFailCreate.FAIL, nd.getOnMissingProperty());
        Assert.assertFalse(nd.isExport());
    }

    @Test
    public void testPropNameOverridesId()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setId("hello");
        nd.setPropertyName("world");
        Assert.assertEquals("hello", nd.getId());
        Assert.assertEquals("world", nd.getPropertyName());
    }

    @Test
    public void testIdSuppliesPropName()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setId("hello");
        Assert.assertEquals("hello", nd.getId());
        Assert.assertEquals("hello", nd.getPropertyName());
    }

    @Test(expected = NullPointerException.class)
    public void testNullInitialValue()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setInitialValue(null);
        nd.check();
    }

    @Test(expected = IllegalStateException.class)
    public void testBlankInitialValue()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setInitialValue("");
        nd.check();
    }

    @Test(expected = IllegalStateException.class)
    public void testBadFieldNumber()
    {
        final NumberDefinition nd = new NumberDefinition();
        nd.setFieldNumber(-1);
        nd.check();
    }
}
