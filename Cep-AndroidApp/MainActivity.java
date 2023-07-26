package br.com.local.cep;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private EditText editTextCep;
    private TextView textViewResultado;
    private Button buttonBuscar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCep = findViewById(R.id.editTextCep);
        textViewResultado = findViewById(R.id.textViewResultado);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        progressBar = findViewById(R.id.progressBar);

        buttonBuscar.setOnClickListener(v -> {
            String cep = editTextCep.getText().toString();
            if (!cep.isEmpty()) {
                new ConsultsCepTask().execute(cep);
            }
        });
    }

    private class ConsultsCepTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            textViewResultado.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = "https://viacep.com.br/ws/" + params[0] + "/json/";

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                bufferedReader.close();
                inputStream.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String endereco = jsonObject.getString("logradouro") + ", "
                            + jsonObject.getString("bairro") + ", "
                            + jsonObject.getString("localidade") + " - "
                            + jsonObject.getString("uf");
                    textViewResultado.setText(endereco);
                } catch (JSONException e) {
                    e.printStackTrace();
                    textViewResultado.setText("Erro ao processar resposta.");
                }
            } else {
                textViewResultado.setText("CEP não encontrado.");
            }
        }
    }
}
