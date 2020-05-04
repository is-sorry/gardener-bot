package com.syylacodes.gardener.bot;

import lombok.Data;

@Data
public class Room {
    private long id;
    private boolean occupied = false;

    public Room(long id) {
        this.id = id;
    }
}
