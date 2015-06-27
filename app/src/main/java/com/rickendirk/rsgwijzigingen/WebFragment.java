package com.rickendirk.rsgwijzigingen;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

public class WebFragment extends Fragment {

    WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.web_layout, container, false);

        webView = (WebView) mainView.findViewById(R.id.webView);
        refresh();
        webView.getSettings().setBuiltInZoomControls(true);

        return mainView;

    }
    public void refresh(){
        webView.loadUrl("http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm");

        Toast.makeText(getActivity(), "Pagina is vernieuwd", Toast.LENGTH_LONG).show();
    }

}
