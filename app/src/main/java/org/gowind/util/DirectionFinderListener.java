package org.gowind.util;

import org.gowind.model.Route;

import java.util.List;

/**
 * Created by shiv.loka on 10/8/16.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
