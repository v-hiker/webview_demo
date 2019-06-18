package com.example.broswer2;

import android.util.Patterns;

public class ToUrl {


    String input_head1 = "http://";
    String input_head2 = "https://";

    public String tourl(String input, String search_engine) {
        if (Patterns.WEB_URL.matcher(input).matches()) {
            if (input.contains(input_head1) || input.contains(input_head2)) {
                return input;
            } else {
                return input_head1.concat(input);
            }
        } else {
            return search_engine.concat(input);
        }
    }

}