package com.zion.lottonumbergenerator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    private Spinner spinnerPastRound;
    private String round;
    private Button buttonNumberGeneration;
    private Button buttonCheckWin;
    private Button buttonTrigger;
    private LinearLayout linearLayoutWebView;
    private WebView webView;
    private EditText editText;
    private IntentIntegrator integrator;
    private TextView textViewPastRoundNumber1;
    private TextView textViewPastRoundNumber2;
    private TextView textViewPastRoundNumber3;
    private TextView textViewPastRoundNumber4;
    private TextView textViewPastRoundNumber5;
    private TextView textViewPastRoundNumber6;
    private TextView textViewPastRoundNumberBonus;
    private TextView textViewGeneratedNumber1;
    private TextView textViewGeneratedNumber2;
    private TextView textViewGeneratedNumber3;
    private TextView textViewGeneratedNumber4;
    private TextView textViewGeneratedNumber5;
    private TextView textViewGeneratedNumber6;
    private enum RequestType {
        INIT, UPDATE;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewPastRoundNumber1 = findViewById(R.id.text_view_past_round_number_1);
        textViewPastRoundNumber2 = findViewById(R.id.text_view_past_round_number_2);
        textViewPastRoundNumber3 = findViewById(R.id.text_view_past_round_number_3);
        textViewPastRoundNumber4 = findViewById(R.id.text_view_past_round_number_4);
        textViewPastRoundNumber5 = findViewById(R.id.text_view_past_round_number_5);
        textViewPastRoundNumber6 = findViewById(R.id.text_view_past_round_number_6);
        textViewGeneratedNumber1 = findViewById(R.id.text_view_generated_number_1);
        textViewGeneratedNumber2 = findViewById(R.id.text_view_generated_number_2);
        textViewGeneratedNumber3 = findViewById(R.id.text_view_generated_number_3);
        textViewGeneratedNumber4 = findViewById(R.id.text_view_generated_number_4);
        textViewGeneratedNumber5 = findViewById(R.id.text_view_generated_number_5);
        textViewGeneratedNumber6 = findViewById(R.id.text_view_generated_number_6);
        textViewPastRoundNumberBonus = findViewById(R.id.text_view_past_round_number_bonus);

        spinnerPastRound = findViewById(R.id.spinner_past_round);

        linearLayoutWebView = findViewById(R.id.linear_layout_web_view);
        buttonCheckWin = findViewById(R.id.button_check_win);
        buttonTrigger = findViewById(R.id.button_trigger);
        editText = findViewById(R.id.edit_text);
        webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                Log.d("WebView", "Loading complete.");
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    buttonCheckWin.callOnClick();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });

        integrator = new IntentIntegrator(this);
        integrator.setPrompt("QR 코드를 사각형 안에 비춰주세요.");
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(CaptureActivity.class);


        buttonCheckWin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                integrator.initiateScan();
            }
        });

        buttonTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editText.getText().toString();
                if(!url.startsWith("http://")) {
                    url = "http://" + url;
                }
                linearLayoutWebView.setVisibility(View.VISIBLE);
                buttonNumberGeneration.setVisibility(View.INVISIBLE);
                buttonCheckWin.setVisibility(View.INVISIBLE);
                linearLayoutWebView.bringToFront();
                webView.loadUrl(url);
            }
        });

        buttonNumberGeneration = findViewById(R.id.button_number_generation);
        buttonNumberGeneration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAndSetLottoNumber();
            }
        });

        getJsonObjectLotto(getString(R.string.lotto_round_url), RequestType.INIT);
    }

    public void getJsonObjectLotto(String url, final RequestType requestType) {
       // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(requestType == RequestType.INIT)
                            setSpinnerPastRound(response);
                        else if(requestType == RequestType.UPDATE)
                            setPastRoundNumbers((JsonObject) JsonParser.parseString(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
    public void setPastRoundNumbers(JsonObject jsonObject) {
        textViewPastRoundNumber1.setText("" + jsonObject.get("drwtNo1"));
        textViewPastRoundNumber2.setText(jsonObject.get("drwtNo2") + "");
        textViewPastRoundNumber3.setText(jsonObject.get("drwtNo3") + "");
        textViewPastRoundNumber4.setText(jsonObject.get("drwtNo4") + "");
        textViewPastRoundNumber5.setText(jsonObject.get("drwtNo5") + "");
        textViewPastRoundNumber6.setText(jsonObject.get("drwtNo6") + "");
        textViewPastRoundNumberBonus.setText(jsonObject.get("bnusNo") + "");
    }

    public void setSpinnerPastRound(String response)
    {
        String subResponse = response.substring(184, 204);
        int recentRound = Integer.parseInt(subResponse.replaceAll("[^0-9]",""));
        ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<CharSequence>(MainActivity.this,
                R.layout.support_simple_spinner_dropdown_item);
        for (int i = recentRound; i > 0; --i)
            arrayAdapter.add(String.valueOf(i));

        spinnerPastRound.setAdapter(arrayAdapter);
        getJsonObjectLotto(getString(R.string.lotto_number_url) + recentRound, RequestType.UPDATE);
        spinnerPastRound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                round = (String) spinnerPastRound.getSelectedItem();
                getJsonObjectLotto(getString(R.string.lotto_number_url) + round, RequestType.UPDATE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void generateAndSetLottoNumber()
    {
        List<Integer> lottoNumberList = new Vector<Integer>(45);
        for(int i = 1; i <= 45; ++i)
            lottoNumberList.add(i);

        Collections.shuffle(lottoNumberList);
        List<Integer> numberSelectedList = lottoNumberList.subList(0, 6);

        textViewGeneratedNumber1.setText(String.valueOf(numberSelectedList.get(0)));
        textViewGeneratedNumber2.setText(String.valueOf(numberSelectedList.get(1)));
        textViewGeneratedNumber3.setText(String.valueOf(numberSelectedList.get(2)));
        textViewGeneratedNumber4.setText(String.valueOf(numberSelectedList.get(3)));
        textViewGeneratedNumber5.setText(String.valueOf(numberSelectedList.get(4)));
        textViewGeneratedNumber6.setText(String.valueOf(numberSelectedList.get(5)));
    }

    @Override
    public void onBackPressed() {
        if(webView.isActivated()) {
            if(webView.canGoBack()) {
                linearLayoutWebView.setVisibility(View.INVISIBLE);
                buttonNumberGeneration.setVisibility(View.VISIBLE);
                buttonCheckWin.setVisibility(View.VISIBLE);
                webView.goBack();
            } else {
                linearLayoutWebView.setVisibility(View.INVISIBLE);
                buttonNumberGeneration.setVisibility(View.VISIBLE);
                buttonCheckWin.setVisibility(View.VISIBLE);
                // integrator.initiateScan();
            }
        } else {
            if(linearLayoutWebView.getVisibility() == View.VISIBLE) {
                linearLayoutWebView.setVisibility(View.INVISIBLE);
                buttonNumberGeneration.setVisibility(View.VISIBLE);
                buttonCheckWin.setVisibility(View.VISIBLE);
            }
            else
                super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null) {

            } else {
                editText.setText(result.getContents());
                buttonTrigger.callOnClick();
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
