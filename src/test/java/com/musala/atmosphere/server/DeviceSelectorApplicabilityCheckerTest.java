package com.musala.atmosphere.server;

import org.junit.Assert;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.util.Pair;

public class DeviceSelectorApplicabilityCheckerTest {
    private DeviceSelectorApplicabilityChecker dsChecker = new DeviceSelectorApplicabilityChecker();

    public static final int MIN_API = 22;

    public static final int TARGET_API = 24;

    public static final int MAX_API = 26;

    public static final int MIN_TO_TARGET_INTERVAL_API = 23;

    public static final int TARGET_TO_MAX_INTERVAL_APi = 25;

    public static final int UNDER_MIN_API = 19;

    public static final int OVER_MAX_API = 27;

    @Test
    public void apiLevelApplicabilityTest() {
        // test min api level
        DeviceSelectorBuilder builder = new DeviceSelectorBuilder();
        DeviceSelector dSelector = builder.minApi(MIN_API).build();
        DeviceInformation dInfo = new DeviceInformation();

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(UNDER_MIN_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        // test max api level
        builder = new DeviceSelectorBuilder();
        dSelector = builder.maxApi(MAX_API).build();

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(OVER_MAX_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        // test min and max api level
        builder = new DeviceSelectorBuilder();
        dSelector = builder.minApi(MIN_API).maxApi(MAX_API).build();

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(UNDER_MIN_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(OVER_MAX_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        // test min, target api
        builder = new DeviceSelectorBuilder();
        dSelector = builder.minApi(MIN_API).targetApi(TARGET_API).build();

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MIN_TO_TARGET_INTERVAL_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(UNDER_MIN_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        // test max, target api
        builder = new DeviceSelectorBuilder();
        dSelector = builder.maxApi(MAX_API).targetApi(TARGET_API).build();

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_TO_MAX_INTERVAL_APi);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(OVER_MAX_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        // test min, max, target api
        builder = new DeviceSelectorBuilder();
        dSelector = builder.minApi(MIN_API).maxApi(MAX_API).targetApi(TARGET_API).build();

        dInfo.setApiLevel(MIN_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MAX_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(MIN_TO_TARGET_INTERVAL_API);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(TARGET_TO_MAX_INTERVAL_APi);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(UNDER_MIN_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setApiLevel(OVER_MAX_API);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));
    }

    @Test
    public void cameraApplicabilityTest() {
        DeviceSelectorBuilder builder = new DeviceSelectorBuilder();
        DeviceSelector dSelector = builder.isCameraAvailable(true).build();
        DeviceInformation dInfo = new DeviceInformation();
        dInfo.setApiLevel(MIN_API);

        dInfo.setCamera(true);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setCamera(false);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        builder = new DeviceSelectorBuilder();
        dSelector = builder.isCameraAvailable(false).build();

        dInfo.setCamera(true);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setCamera(false);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));
    }

    @Test
    public void deviceTypeApplicabilityTest() {
        DeviceSelectorBuilder builder = new DeviceSelectorBuilder();
        DeviceSelector dSelector = builder.deviceType(DeviceType.DEVICE_ONLY).build();
        DeviceInformation dInfo = new DeviceInformation();
        dInfo.setApiLevel(MIN_API);

        dInfo.setEmulator(true);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setEmulator(false);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        builder = new DeviceSelectorBuilder();
        dSelector = builder.deviceType(DeviceType.EMULATOR_ONLY).build();

        dInfo.setEmulator(true);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setEmulator(false);
        Assert.assertFalse(dsChecker.isApplicable(dSelector, dInfo));

        builder = new DeviceSelectorBuilder();
        dSelector = builder.deviceType(DeviceType.DEVICE_PREFERRED).build();

        dInfo.setEmulator(true);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setEmulator(false);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        builder = new DeviceSelectorBuilder();
        dSelector = builder.deviceType(DeviceType.EMULATOR_PREFERRED).build();

        dInfo.setEmulator(true);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));

        dInfo.setEmulator(false);
        Assert.assertTrue(dsChecker.isApplicable(dSelector, dInfo));
    }

    @Test
    public void variousParamTest() {
        DeviceSelectorBuilder builder = new DeviceSelectorBuilder();
        DeviceSelector deviceSelector = builder.minApi(19)
                                               //.targetApi(25)
                                               .maxApi(31)
                                               .deviceModel("Nexus")
                                               .deviceOs(DeviceOs.KITKAT_4_4_4)
                                               .isCameraAvailable(true)
                                               .deviceType(DeviceType.DEVICE_ONLY)
                                               .serialNumber("123")
                                               .ramCapacity(2048)
                                               .screenDpi(300)
                                               .screenHeight(800)
                                               .screenWidth(600)
                                               .build();

        DeviceInformation dInfo = new DeviceInformation();

        dInfo.setApiLevel(31);
        dInfo.setModel("Nexus");
        dInfo.setEmulator(false);
        dInfo.setSerialNumber("123");
        dInfo.setOs(DeviceOs.KITKAT_4_4_4.toString());
        dInfo.setCamera(true);
        dInfo.setRam(2048);
        dInfo.setDpi(300);
        dInfo.setResolution(new Pair<Integer, Integer>(600, 800));

        Assert.assertTrue(dsChecker.isApplicable(deviceSelector, dInfo));
    }
}
