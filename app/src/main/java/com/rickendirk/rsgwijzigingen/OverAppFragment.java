package com.rickendirk.rsgwijzigingen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OverAppFragment extends Fragment {

    public OverAppFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_over_app, container, false);
        Button toGithub = (Button) layout.findViewById(R.id.toGithubButton);
        toGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String gitURL = "https://github.com/Richyrick/RoosterwijzigingChecker";
                Intent gitInt = new Intent(Intent.ACTION_VIEW);
                gitInt.setData(Uri.parse(gitURL));
                startActivity(gitInt);
            }
        });

        return layout;
    }
}
