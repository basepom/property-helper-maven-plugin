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

import org.basepom.mojo.propertyhelper.beans.IgnoreWarnFailCreate;
import org.junit.Assert;
import org.junit.Test;


public class TestIgnoreWarnFailCreate
{
    @Test
    public void testValid()
    {
        IgnoreWarnFailCreate value = IgnoreWarnFailCreate.forString("fail");
        Assert.assertSame(IgnoreWarnFailCreate.FAIL, value);
        value = IgnoreWarnFailCreate.forString("warn");
        Assert.assertSame(IgnoreWarnFailCreate.WARN, value);
        value = IgnoreWarnFailCreate.forString("ignore");
        Assert.assertSame(IgnoreWarnFailCreate.IGNORE, value);
        value = IgnoreWarnFailCreate.forString("create");
        Assert.assertSame(IgnoreWarnFailCreate.CREATE, value);
    }

    @Test
    public void testValidCases()
    {
        IgnoreWarnFailCreate value = IgnoreWarnFailCreate.forString("fail");
        Assert.assertSame(IgnoreWarnFailCreate.FAIL, value);
        value = IgnoreWarnFailCreate.forString("FAIL");
        Assert.assertSame(IgnoreWarnFailCreate.FAIL, value);
        value = IgnoreWarnFailCreate.forString("Fail");
        Assert.assertSame(IgnoreWarnFailCreate.FAIL, value);
        value = IgnoreWarnFailCreate.forString("FaIl");
        Assert.assertSame(IgnoreWarnFailCreate.FAIL, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadValue()
    {
        IgnoreWarnFailCreate.forString("foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullValue()
    {
        IgnoreWarnFailCreate.forString(null);
    }

    @Test
    public void testCheckState()
    {
        boolean value = IgnoreWarnFailCreate.checkState(IgnoreWarnFailCreate.FAIL, true, "");
        Assert.assertFalse(value);
        value = IgnoreWarnFailCreate.checkState(IgnoreWarnFailCreate.IGNORE, false, "");
        Assert.assertFalse(value);
        value = IgnoreWarnFailCreate.checkState(IgnoreWarnFailCreate.WARN, false, "");
        Assert.assertFalse(value);
        value = IgnoreWarnFailCreate.checkState(IgnoreWarnFailCreate.CREATE, false, "");
        Assert.assertTrue(value);
    }

    @Test(expected = IllegalStateException.class)
    public void testCheckStateFail()
    {
        IgnoreWarnFailCreate.checkState(IgnoreWarnFailCreate.FAIL, false, "");
    }
}
