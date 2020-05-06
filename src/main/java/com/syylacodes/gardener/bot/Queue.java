package com.syylacodes.gardener.bot;

import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class Queue {
    public static final LinkedList<User> queue = new LinkedList<>();
}
