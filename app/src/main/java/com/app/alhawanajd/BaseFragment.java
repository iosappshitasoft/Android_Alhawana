package com.app.alhawanajd;

import androidx.fragment.app.Fragment;

import com.app.helper.callback.OnBackPressedListener;

public class BaseFragment extends Fragment implements OnBackPressedListener {

    @Override
    public boolean onBackPressed() {
        return false;
    }
}