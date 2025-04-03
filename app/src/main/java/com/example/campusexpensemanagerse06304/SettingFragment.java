package com.example.campusexpensemanagerse06304;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

public class SettingFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private TextView tvTitle;
    private Button btnManageCategories;
    private Button btnManageRecurring;
    private Button btnLogout;
    private int userId;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        btnManageCategories = view.findViewById(R.id.btnManageCategories);
        btnManageRecurring = view.findViewById(R.id.btnManageRecurring);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Display username and get user ID
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String account = bundle.getString("USER_ACCOUNT", "");
                userId = bundle.getInt("ID_USER", -1);
                tvTitle.setText("Hello, " + account);
            }
        }

        // Setup Manage Categories button
        btnManageCategories.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), CategoryManagementActivity.class);
            startActivity(intent);
        });

        // Setup Manage Recurring Expenses button
        btnManageRecurring.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), RecurringExpenseActivity.class);
            intent.putExtra("ID_USER", userId);
            startActivity(intent);
        });

        // Setup Logout button
        btnLogout.setOnClickListener(view12 -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return view;
    }
}