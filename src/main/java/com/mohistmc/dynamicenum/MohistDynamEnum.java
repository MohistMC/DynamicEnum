/*
 * Mohist - MohistMC
 * Copyright (C) 2018-2024.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mohistmc.dynamicenum;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import sun.misc.Unsafe;

public class MohistDynamEnum {

    public static final sun.misc.Unsafe unsafe;
    private static final MethodHandles.Lookup implLookup;
    private static final List<String> ENUM_CACHE = List.of("enumConstantDirectory", "enumConstants", "enumVars");

    static {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            MethodHandles.lookup().ensureInitialized(MethodHandles.Lookup.class);
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            implLookup = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(implLookupField), unsafe.staticFieldOffset(implLookupField));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static <T> T makeEnum(Class<T> enumClass, String value, int ordinal, List<Class<?>> additionalParameterTypes, List<Object> additionalValues) {
        try {
            implLookup.ensureInitialized(enumClass);
            List<Class<?>> ptypes = new ArrayList<>(additionalParameterTypes.size() + 2);
            ptypes.add(String.class);
            ptypes.add(int.class);
            ptypes.addAll(additionalParameterTypes);
            MethodHandle constructor = implLookup.findConstructor(enumClass, MethodType.methodType(void.class, ptypes));

            List<Object> arguments = new ArrayList<>(additionalValues.size() + 2);
            arguments.add(normalizeName(value));
            arguments.add(ordinal);
            arguments.addAll(additionalValues);
            return (T) constructor.invokeWithArguments(arguments);
        } catch (Throwable e) {
            e.fillInStackTrace();
            return null;
        }
    }

    private static void cleanEnumCache(Class<?> enumClass) {
        ENUM_CACHE.forEach(s -> Arrays.stream(Class.class.getDeclaredFields())
                .filter(field -> field.getName().equals(s))
                .forEachOrdered(field -> unsafe.putObjectVolatile(enumClass, unsafe.objectFieldOffset(field), null)));
    }

    public static <T> T addEnum(Class<T> cl, String name, List<Class<?>> additionalParameterTypes, List<Object> additionalValues) {
        try {
            implLookup.ensureInitialized(cl);
            for (Field field : cl.getDeclaredFields()) {
                if (field.getName().equals("$VALUES") || field.getName().equals("ENUM$VALUES")) {
                    Object base = unsafe.staticFieldBase(field);
                    long offset = unsafe.staticFieldOffset(field);
                    T[] arr = (T[]) unsafe.getObject(base, offset);
                    T[] newArr = (T[]) Array.newInstance(cl, arr.length + 1);
                    System.arraycopy(arr, 0, newArr, 0, arr.length);
                    T newInstance = makeEnum(cl, name, arr.length, additionalParameterTypes, additionalValues);

                    newArr[arr.length] = newInstance;
                    unsafe.putObject(base, offset, newArr);
                    cleanEnumCache(cl);
                    return newInstance;
                }
            }
        } catch (Throwable e) {
            e.fillInStackTrace();
            return null;
        }
        return null;
    }

    public static <T> T addEnum(Class<T> cl, String name) {
        return addEnum(cl, name, List.of(), List.of());
    }

    public static String normalizeName(String name) {
        return name.replace(':', '_')
                .replaceAll("\\s+", "_")
                .replaceAll("\\W", "")
                .toUpperCase(Locale.ENGLISH);
    }
}