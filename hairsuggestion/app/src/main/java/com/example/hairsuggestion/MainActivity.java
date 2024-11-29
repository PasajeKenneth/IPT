package com.example.hairsuggestion;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFaceShape, editTextAgeGroup;
    private RadioGroup radioGroupGender;
    private Button buttonSubmit;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextFaceShape = findViewById(R.id.editTextFaceShape);
        editTextAgeGroup = findViewById(R.id.editTextAgeGroup);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewResult = findViewById(R.id.textViewResult);

        // Submit button onClick listener
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitHairstyleData();
            }
        });
    }

    // Function to get user inputs and send to Flask API
    private void submitHairstyleData() {
        // Get the input values
        String faceShape = editTextFaceShape.getText().toString().trim();
        String ageGroup = editTextAgeGroup.getText().toString().trim();

        // Validate inputs
        if (faceShape.isEmpty() || ageGroup.isEmpty()) {
            Toast.makeText(this, "Please enter face shape and age group.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get gender
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString().equals("Male") ? "male" : "female";

        // Prepare JSON object for the API request
        JSONObject postData = new JSONObject();
        try {
            postData.put("FaceShape", Integer.parseInt(faceShape)); // Key should match API
            postData.put("Gender", gender); // Key should match API
            postData.put("AgeGroup", Integer.parseInt(ageGroup)); // Key should match API
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON object.", Toast.LENGTH_SHORT).show();
            return;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Face shape and age group must be integers.", Toast.LENGTH_SHORT).show();
            return;
        }

        // API URL (replace with your Flask server IP)
        String url = "http://192.168.254.112:5000/predict"; // Change this IP accordingly

        // Send the POST request to the API
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        // Parse JSON response
                        String recommendedHairstyle = response.getString("RecommendedHairstyle");
                        textViewResult.setText("Suggested Hairstyle: " + recommendedHairstyle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        textViewResult.setText("Error in parsing prediction.");
                    }
                }, error -> {
            // Log the error message
            error.printStackTrace();
            // Log full error response for debugging
            if (error.networkResponse != null) {
                String errorMessage = new String(error.networkResponse.data);
                textViewResult.setText("Error occurred: " + error.networkResponse.statusCode + " - " + errorMessage);
            } else {
                textViewResult.setText("Error occurred: " + error.toString());
            }
        });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
}