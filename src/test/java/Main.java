/*
 * Mohist - MohistMC
 * Copyright (C) 2018-2022.
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

import com.mohistmc.enumhelper.MohistEnumHelper;
import java.util.Arrays;

public class Main {

    public static void main(String[] a) {
        System.out.println(Arrays.toString(Test.values()));
        MohistEnumHelper.addEnum0(Test.class, "d", new Class[0]);
        System.out.println(Arrays.toString(Test.values()));
        System.out.println(Arrays.toString(Test.values()));
        MohistEnumHelper.addEnum0(Test.class, "aaa", new Class[0]);
        System.out.println(Arrays.toString(Test.values()));
    }
}