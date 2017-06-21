package com.musala.atmosphere.server;

import java.util.Map;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel;
import com.musala.atmosphere.commons.cs.deviceselection.CameraAvailable;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceModel;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceParameter;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.cs.deviceselection.RamCapacity;
import com.musala.atmosphere.commons.cs.deviceselection.ScreenParameter;
import com.musala.atmosphere.commons.cs.deviceselection.SerialNumber;

/**
 * Checks whether a certain {@link DeviceSelector device selector} is applicable to a given {@link IDevie device}.
 *
 * @author dimcho.nedev
 *
 */
public final class DeviceSelectorApplicabilityChecker {
    private static final int MIN_API = 18;

    private static final int MAX_API = Integer.MAX_VALUE;

    /**
     * Checks whether a {@link DeviceSelector selector} is applicable to a given {@link IDevie device}
     *
     * @param dSelector
     *        - a {@link DeviceSelector selector} for a device
     * @param dInfo
     *        - an {@link DeviceInformation information} about a device
     * @return whether the selector is applicable for the specific device
     *
     */
    public boolean isApplicable(DeviceSelector dSelector, DeviceInformation dInfo) {
        Map<Class<? extends DeviceParameter>, DeviceParameter> dParams = dSelector.getParameters();
        boolean isApplicable = true;

        isApplicable &= checkApiLevel(dParams, dInfo.getApiLevel());

        if (!isApplicable) {
            return false;
        }

        for (DeviceParameterType dParamType : DeviceParameterType.values()) {
            DeviceParameter dParam = dParams.get(dParamType.targetClass);

            if (dParam != null) {
                switch (DeviceParameterType.fromClass(dParamType.targetClass)) {
                    case DeviceModel:
                        isApplicable &= checkStringDeviceParam(dParam, dInfo.getModel());
                        break;
                    case DeviceType:
                        isApplicable &= checkIsEmulator(dParam, dInfo.isEmulator());
                        break;
                    case DeviceOs:
                        isApplicable &= checkStringDeviceParam(dParam, dInfo.getOS());
                        break;
                    case CameraAvailable:
                        isApplicable &= checkBooleanDeviceParam(dParam, dInfo.hasCamera());
                        break;
                    case SerialNumber:
                        isApplicable &= checkStringDeviceParam(dParam, dInfo.getSerialNumber());
                        break;
                    case ScreenDpi:
                        isApplicable &= checkIntegerDeviceParam(dParam, dInfo.getDpi());
                        break;
                    case ScreenHeight:
                        isApplicable &= checkIntegerDeviceParam(dParam, dInfo.getResolution().getValue());
                        break;
                    case ScreenWidth:
                        isApplicable &= checkIntegerDeviceParam(dParam, dInfo.getResolution().getKey());
                        break;
                    case RamCapacity:
                        isApplicable &= checkIntegerDeviceParam(dParam, dInfo.getRam());
                        break;
                    default:
                        break;
                }

                if (!isApplicable) {
                    return false;
                }
            }
        }

        return isApplicable;
    }

    private boolean checkApiLevel(Map<Class<? extends DeviceParameter>, DeviceParameter> dParams, int apilevel) {
        DeviceParameter pTargetApi = dParams.get(ApiLevel.Target.class);
        if (pTargetApi == null || toInt(pTargetApi) != apilevel) {
            DeviceParameter pMinApi = dParams.get(ApiLevel.Minimum.class);
            DeviceParameter pMaxApi = dParams.get(ApiLevel.Maximum.class);

            int minApi = pMinApi != null ? toInt(pMinApi) : MIN_API;
            int maxApi = pMaxApi != null ? toInt(pMaxApi) : MAX_API;

            if (apilevel < minApi || apilevel > maxApi) {
                return false;
            }
        }

        return true;
    }

    private boolean checkStringDeviceParam(DeviceParameter dParam, String value) {
        return !dParam.toString().equals(value) ? false : true;
    }

    private boolean checkIntegerDeviceParam(DeviceParameter dParam, int value) {
        return toInt(dParam) != value ? false : true;
    }

    private boolean checkBooleanDeviceParam(DeviceParameter dParam, boolean value) {
        return Boolean.parseBoolean(dParam.toString()) == value;
    }

    private int toInt(DeviceParameter parameter) {
        return Integer.parseInt(parameter.toString());
    }

    private boolean checkIsEmulator(DeviceParameter dType, boolean isEmulator) {
        DeviceType deviceType = (DeviceType) dType;
        if (deviceType == DeviceType.DEVICE_ONLY && isEmulator
                || deviceType == DeviceType.EMULATOR_ONLY && !isEmulator) {
            return false;
        }

        return true;
    }

    /**
     * An enumeration for the {@link DeviceParameter device parameters}. Helps to resolve the type of certain device
     * parameter.
     *
     */
    private enum DeviceParameterType {
        DeviceModel(DeviceModel.class),
        DeviceType(DeviceType.class),
        DeviceOs(DeviceOs.class),
        CameraAvailable(CameraAvailable.class),
        SerialNumber(SerialNumber.class),
        ScreenDpi(ScreenParameter.DPI.class),
        ScreenHeight(ScreenParameter.Height.class),
        ScreenWidth(ScreenParameter.Width.class),
        RamCapacity(RamCapacity.class);

        private Class<? extends DeviceParameter> targetClass;

        private DeviceParameterType(Class<? extends DeviceParameter> targetClass) {
            this.targetClass = targetClass;
        }

        private static DeviceParameterType fromClass(Class<? extends DeviceParameter> clazz) {
            for (DeviceParameterType dParamType : values()) {
                if (dParamType.targetClass == clazz) {
                    return dParamType;
                }
            }
            return null;
        }
    }
}
