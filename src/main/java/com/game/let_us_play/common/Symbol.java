package com.game.let_us_play.common;

import lombok.Getter;

/**
 * Represents a game piece/symbol.
 */
@Getter
public enum Symbol {
    CROSS("X"),
    ZERO("O");

    private final String display;

    Symbol(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
