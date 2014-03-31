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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.basepom.mojo.propertyhelper.InterpolatorFactory;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class TestPropertyGroup
{
    private final InterpolatorFactory interpolatorFactory = new InterpolatorFactory(Optional.<Model>absent());

    @Test
    public void testConstant() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "world");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props);

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", Collections.<String, String> emptyMap());
        Assert.assertEquals("world", propValue);
    }

    @Test
    public void testRenderSingle() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "#{world}");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props);

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", ImmutableMap.of("world", "pizza"));
        Assert.assertEquals("pizza", propValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testRenderEmptyFail() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "#{world}");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props);

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", Collections.<String, String> emptyMap());
        Assert.assertEquals("", propValue);
    }

    @Test
    public void testRenderEmptyOk() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "nice-#{world}-hat");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props)
            .setOnMissingProperty("ignore");

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", Collections.<String, String> emptyMap());
        Assert.assertEquals("nice--hat", propValue);
    }

    @Test
    public void testRenderIsReluctant() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "nice-#{first}-#{world}-hat");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props)
            .setOnMissingProperty("ignore");

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", Collections.<String, String> emptyMap());
        Assert.assertEquals("nice---hat", propValue);
    }

    @Test
    public void testRenderFriendOfAFriend() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "nice-#{whatWorld}-#{world}-hat");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props)
            .setOnMissingProperty("ignore");

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", ImmutableMap.of("whatWorld", "#{first}", "first", "decadent", "world", "rome"));
        Assert.assertEquals("nice-decadent-rome-hat", propValue);
    }


    @Test
    public void testRenderDotsAreCool() throws Exception
    {
        final Map<String, String> props = ImmutableMap.of("hello", "nice-#{foo.bar.world}-hat");

        final PropertyGroup pg = new PropertyGroup()
            .setId("hello")
            .setProperties(props)
            .setOnMissingProperty("ignore");

        final List<String> propNames = Lists.newArrayList(pg.getPropertyNames());
        Assert.assertEquals(1, propNames.size());
        Assert.assertEquals("hello", propNames.get(0));

        final String propValue = pg.getPropertyValue(interpolatorFactory, "hello", ImmutableMap.of("foo.bar.world", "strange"));
        Assert.assertEquals("nice-strange-hat", propValue);
    }

}
