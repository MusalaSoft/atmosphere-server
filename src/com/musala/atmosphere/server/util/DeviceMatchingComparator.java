package com.musala.atmosphere.server.util;

import java.util.Comparator;

import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.commons.sa.DeviceInformation;

public class DeviceMatchingComparator implements Comparator<DeviceInformation>
{
	private DeviceParameters requiredDeviceParameters;

	public DeviceMatchingComparator(DeviceParameters matchingBase)
	{
		this.requiredDeviceParameters = matchingBase;
	}

	@Override
	public int compare(DeviceInformation availDeviceFirst, DeviceInformation availDeviceSecond)
	{
		int matchingScoreFirst = matchingScore(requiredDeviceParameters, availDeviceFirst);
		int matchingScoreSecond = matchingScore(requiredDeviceParameters, availDeviceSecond);

		return matchingScoreFirst - matchingScoreSecond;
	}

	/**
	 * Calculates a required/present device matching score. Each device parameter should be taken into consideration
	 * with different weight. This method will be changed when more complex selection mechanism is discussed. At the
	 * moment it is a simple match check.
	 * 
	 * @param requiredDevice
	 *        information for a device that is needed.
	 * @param matchDevice
	 *        information for a present device.
	 * @return
	 */
	private static int matchingScore(DeviceParameters neededDeviceParameters, DeviceInformation matchDeviceInformation)
	{
		int result = 1;
		DeviceType requiredDeviceType = neededDeviceParameters.getDeviceType();
		int requiredDeviceDpi = neededDeviceParameters.getDpi();
		DeviceOs requiredDeviceOs = neededDeviceParameters.getOs();
		int requiredDeviceRam = neededDeviceParameters.getRam();
		int requiredDeviceResH = neededDeviceParameters.getResolutionHeight();
		int requiredDeviceResW = neededDeviceParameters.getResolutionWidth();

		if (requiredDeviceType != DeviceParameters.DEVICE_TYPE_NO_PREFERENCE)
		{
			if (requiredDeviceType == DeviceType.DEVICE_ONLY)
			{
				if (matchDeviceInformation.isEmulator())
				{
					result = 0;
				}
			}
			else if (requiredDeviceType == DeviceType.EMULATOR_ONLY)
			{
				if (matchDeviceInformation.isEmulator() == false)
				{
					result = 0;
				}
			}
		}

		if (requiredDeviceDpi != DeviceParameters.DPI_NO_PREFERENCE)
		{
			if (requiredDeviceDpi != matchDeviceInformation.getDpi())
			{
				result = 0;
			}
		}

		if (requiredDeviceRam != DeviceParameters.RAM_NO_PREFERENCE)
		{
			if (requiredDeviceRam != matchDeviceInformation.getRam())
			{
				result = 0;
			}
		}

		if (requiredDeviceResH != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE)
		{
			if (requiredDeviceResH != matchDeviceInformation.getResolution().getKey())
			{
				result = 0;
			}
		}

		if (requiredDeviceResW != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE)
		{
			if (requiredDeviceResW != matchDeviceInformation.getResolution().getValue())
			{
				result = 0;
			}
		}

		if (requiredDeviceOs != DeviceParameters.DEVICE_OS_NO_PREFERENCE)
		{
			if (requiredDeviceOs.toString().equals(matchDeviceInformation.getOS()) == false)
			{
				result = 0;
			}
		}

		return result;
	}

	public static boolean isValidMatch(	DeviceParameters neededDeviceParameters,
										DeviceInformation availableDeviceInformation)
	{
		int matchScore = matchingScore(neededDeviceParameters, availableDeviceInformation);
		boolean isMatch = matchScore > 0;
		return isMatch;
	}

}
