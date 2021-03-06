// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.server.util;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

/**
 * This is an helper class to create mocks of singleton classes.
 */
public class SingletonMocker {
    private static String singletonHolderClassName;

    private static String instanceMemberName;

    /**
     * This method find the simple names of the singleton class and the inner instance variable. Without them we cannot
     * mock the singleton. If the passed class is not a singleton, a RuntimeException is thrown.
     * 
     * @param singletonClass
     *        - the singleton class we want to mock
     */
    private static void instantiateNames(Class<?> singletonClass) {
        singletonHolderClassName = singletonClass.getSimpleName();
        Field[] declaredFields = singletonClass.getDeclaredFields();
        boolean foundSingletonInstance = false;
        for (Field singletonInstance : declaredFields) {
            String instanceClassName = singletonInstance.getType().getSimpleName();
            if (instanceClassName.equals(singletonHolderClassName)) {
                instanceMemberName = singletonInstance.getName();
                foundSingletonInstance = true;
                break;
            }
        }

        if (!foundSingletonInstance) {
            throw new RuntimeException("Could not locate singleton instance variable. Your class is probably not singleton.");
        }
    }

    /**
     * Returns mocked object of the passed type.
     * 
     * @param singletonClass
     *        - singleton class for mocking
     * @return - mock instance of the same type as the passed singleton class.
     */
    public static <T> T mockSingleton(Class<T> singletonClass) {
        T mockedSingleton = mock(singletonClass);
        instantiateNames(singletonClass);
        setSingleton(singletonClass, mockedSingleton);
        return mockedSingleton;
    }

    /**
     * Sets null value to all the fields of the passed mocked object from given type.
     * 
     * @param singletonClass
     *        - class we want to mock
     * @param mockedSingleton
     *        - the mock object for this class
     */
    private static <T> void setSingleton(Class<T> singletonClass, T mockedSingleton) {
        Class<?>[] singletonDeclaredClasses = singletonClass.getDeclaredClasses();
        for (Class<?> declaredClass : singletonDeclaredClasses) {
            if (singletonHolderClassName.equals(declaredClass.getSimpleName())) {
                try {
                    Field field = declaredClass.getDeclaredField(instanceMemberName);
                    boolean oldAccessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(null, mockedSingleton);
                    field.setAccessible(oldAccessible);
                } catch (Exception e) {
                    throw new RuntimeException("Creating mock for singleton failed.", e);
                }
            }
        }
    }
}
