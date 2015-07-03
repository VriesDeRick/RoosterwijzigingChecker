package com.rickendirk.rsgwijzigingen;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebFragment extends Fragment {

    WebView webView;
    private boolean isLoading = false;
    private boolean isFinished = false;
    boolean is1eKeerGenegeerd = false;
    ProgressDialog progressDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_web, container, false);
        if (savedInstanceState != null){
            is1eKeerGenegeerd = true;
        }
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setTitle("Pagina wordt geladen");
        progressDialog.setMessage("Even geduld alstublieft, de pagina wordt geladen");
        progressDialog.setProgressNumberFormat(null);



        webView = (WebView) mainView.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isLoading = true;
                isFinished = false;
                progressDialog.setProgress(0);
                progressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                isFinished = true;
                isLoading = false;
                //Ondestaande if voorkomt weergeven "pagina is vernieuwd" bij oriëntatie-verandering
                if (is1eKeerGenegeerd) {
                    is1eKeerGenegeerd = false;
                } else
                    Toast.makeText(getActivity(), "Pagina is vernieuwd", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressDialog.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });

        setRetainInstance(true);
        return mainView;

    }
    public void refresh(){
        webView.loadUrl("http://www.googledrive.com/host/0Bwyvbj_hCVmNOHhnUGZFRnZpMUk");


    }
    public boolean isFinished(){
        return isFinished;
    }
    public boolean isLoading(){
        return isLoading;
    }
    //Onderstaand voor veranderen oriëntatie apparaat

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
