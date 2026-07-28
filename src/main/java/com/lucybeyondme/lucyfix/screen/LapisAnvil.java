package com.lucybeyondme.lucyfix.screen;

public interface LapisAnvil {
    // These IDs and coordinates extend the vanilla anvil layout by one slot.
    int LAPIS_SLOT_ID = 39;
    int LAPIS_SLOT_X = 39;
    int LAPIS_SLOT_Y = 22;
    int PLAYER_INVENTORY_START = 3;

    int lucyfix$getLapisCost();

    int lucyfix$getLapisCount();
}
