package com.frx.jetpro.ui.sofa;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elvishew.xlog.XLog;
import com.frx.jetpro.R;
import com.frx.libnavannotation.FragmentDestination;

@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sofa, container, false);
        final TextView textView = root.findViewById(R.id.text_sofa);
        textView.setText("sofa");
        return root;
    }
}