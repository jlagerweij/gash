package net.lagerwey.gash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 */
public class GashStateManager {

    private final Collection<GashState> states = new ArrayList<GashState>();

    public void add(GashState state) {
        if (!states.contains(state)) {
            states.add(state);
        }
    }

    public void remove(GashState state) {
        if (states.contains(state)) {
            states.remove(state);
        }
    }

    public void clear() {
        states.clear();
    }

    public boolean contains(GashState state) {
        return this.states.contains(state);
    }

    public boolean containsAll(GashState... states) {
        if (states == null || states.length == 0) {
            return true;
        } else {
            return this.states.containsAll(Arrays.asList(states));
        }
    }
}
