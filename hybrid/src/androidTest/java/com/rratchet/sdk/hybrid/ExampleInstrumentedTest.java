/*
 * Copyright (c) 2019. RRatChet.
 * Created by ASLai(laijianhua@ruixiude.com).
 *
 * 项目名称：rratchet-sdk-hybrid-trunk
 * 模块名称：rratchet-sdk-hybrid-trunk
 *
 * 文件名称：ExampleInstrumentedTest.java
 * 文件描述：
 *
 * 创 建 人：ASLai(laijianhua@ruixiude.com)
 *
 * 上次修改时间：2019-04-28 10:34:45
 *
 * 修 改 人：ASLai(laijianhua@ruixiude.com)
 * 修改时间：2019-04-28 10:34:45
 * 修改备注：
 */

package com.rratchet.sdk.hybrid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.rratchet.sdk.hybrid.test", appContext.getPackageName());
    }
}
