package com.frx.jetpro.ui.my;

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

@FragmentDestination(pageUrl = "main/tabs/my", needLogin = true)
public class MyFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my, container, false);
        final TextView textView = root.findViewById(R.id.text_my);
        textView.setText("my");
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        XLog.i("onViewCreated");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        XLog.i("onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
        XLog.i("onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        XLog.i("onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        XLog.i("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XLog.i("onDestroy");
    }
}