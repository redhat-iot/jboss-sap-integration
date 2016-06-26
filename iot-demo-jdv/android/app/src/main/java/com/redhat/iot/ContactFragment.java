package com.redhat.iot;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redhat.iot.R.layout;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup container,
                              final Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        return inflater.inflate( layout.contact, container, false );
    }

}
