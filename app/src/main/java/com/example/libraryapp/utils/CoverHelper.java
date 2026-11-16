package com.example.libraryapp.utils;

import com.example.libraryapp.R;

public class CoverHelper {
    public static int getCoverDrawable(int coverIndex) {
        switch (coverIndex) {
            case 2:
                return R.drawable.cover_2;
            case 3:
                return R.drawable.cover_3;
            case 4:
                return R.drawable.cover_4;
            case 5:
                return R.drawable.cover_5;
            case 6:
                return R.drawable.cover_6;
            case 1:
            default:
                return R.drawable.cover_1;
        }
    }
}