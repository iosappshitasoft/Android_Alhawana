package com.app.helper.callback;

import java.util.ArrayList;

public class CompositeOnNetworkChangedListener implements OnNetworkChangedListener {

    private ArrayList<OnNetworkChangedListener> mListeners;

    public CompositeOnNetworkChangedListener() {
        this.mListeners = new ArrayList<>();
    }

    public void register(OnNetworkChangedListener listener) {

        if (!mListeners.contains(listener)) mListeners.add(listener);
    }

    public void unregister(OnNetworkChangedListener listener) {
        mListeners.remove(listener);
    }

    /*public void setNetworkChanged(Boolean connected) {
        for (OnNetworkChangedListener l :
                mListeners) {
            l.onNetworkChanged(connected);
        }

    }*/


    @Override
    public void onNetworkChanged(Boolean connect) {

        for (OnNetworkChangedListener l :
                mListeners) {
            l.onNetworkChanged(connect);
        }

    }
}
