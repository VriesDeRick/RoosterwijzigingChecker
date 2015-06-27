package com.rickendirk.rsgwijzigingen;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebFragment extends Fragment {

    WebView webView;
    private boolean isLoading = false;
    private boolean isFinished = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.web_layout, container, false);

        webView = (WebView) mainView.findViewById(R.id.webView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isLoading = true;
                isFinished = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                isFinished = true;
                isLoading = false;
                Toast.makeText(getActivity(), "Pagina is vernieuwd", Toast.LENGTH_LONG).show();
            }
        });


        return mainView;

    }
    public void refresh(){
        webView.loadUrl("http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm");


    }
    public boolean isFinished(){
        return isFinished;
    }
    public boolean isLoading(){
        return isLoading;
    }

}
