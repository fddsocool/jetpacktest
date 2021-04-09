package com.frx.jetpro.ui.sofa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.frx.jetpro.databinding.FragmentSofaBinding;
import com.frx.libnavannotation.FragmentDestination;

@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {

    private FragmentSofaBinding fragmentSofaBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentSofaBinding = FragmentSofaBinding.inflate(inflater, container, false);
        return fragmentSofaBinding.getRoot();
    }
}