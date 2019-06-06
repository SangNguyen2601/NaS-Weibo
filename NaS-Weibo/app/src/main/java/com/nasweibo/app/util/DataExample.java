package com.nasweibo.app.util;

import com.nasweibo.app.R;

import java.util.Random;


public class DataExample {

    public static final String[] listUserName = new String[]{
            "Mr.Bean",
            "Hieuapp",
            "Nguyen Thuy",
            "Hung Tran",
            "Taylor",
            "Adam Thien",
            "Vuong Do",
            "Pham Hung",
            "Ronaldo",
            "Pep Guardiola",
            "Tran Hieu",
            "Justa Tee",
            "Tom",
    };

    public static final String[] groupEx = new String[]{
            "Friends",
            "Co-worker",
//            "Family",
//            "College",
//            "Others"
    };

    public static final int[] listAvatar = new int[]{
            R.drawable.f1,
            R.drawable.f2,
            R.drawable.f3,
            R.drawable.f4,
            R.drawable.f5,
            R.drawable.f6,
            R.drawable.f7,
            R.drawable.f8,
            R.drawable.f9,
    };

    public static final String[] onlineStamp = new String[]{
            "Active 2m ago",
            "Active 22m ago",
            "Active 8m ago",
            "Active 5m ago",
            "Active 4m ago",
            "Active 39m ago",
            "Active 1m ago",
            "Active 10m ago",
    };

    public static String getRandomStringValue(String[] list) {
        if (list == null || list.length == 0) {
            return "";
        }

        Random random = new Random();
        int rand = random.nextInt(list.length - 1);
        return list[rand];
    }

    public static int getRandomIntValue(int[] list) {
        if (list == null || list.length == 0) {
            return 0;
        }

        Random random = new Random();
        int rand = random.nextInt(list.length - 1);
        return list[rand];
    }
}
