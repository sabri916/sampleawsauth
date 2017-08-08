package om.metamorph.awsauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = (EditText) findViewById(R.id.et_username);
        passwordEditText = (EditText) findViewById(R.id.et_password);
        loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                login(username, password);
            }
        });
    }

    private void login(String userId, final String password) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        String poolId = "eu-central-1_6ZmavYKn8";
        String clientId = "4fm391mao775n4q4fs1gd88aeo";

        // Create a CognitoUserPool object to refer to your user pool
        String clientSecret = "1q5nu3f48h9a5r9vd0km0l8bnmh7nm9r3s8mnji29lmd63nfafvs";
        CognitoUserPool userPool = new CognitoUserPool(this, poolId, clientId, clientSecret, clientConfiguration);
        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

            public static final String TAG = "aws login";

            @Override
            public void onSuccess(CognitoUserSession cognitoUserSession) {
                // Sign-in was successful, cognitoUserSession will contain tokens for the user
                Log.i(TAG, "login is successful");
                Log.i(TAG, "login is successful");
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                // The API needs user sign-in credentials to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, password, null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
                // Allow the sign-in process to continue
                multiFactorAuthenticationContinuation.continueTask();
            }

            @Override
            public void onFailure(Exception exception) {
                // Sign-in failed, check exception for the cause
                Log.i(TAG, "login failed: " + exception);
            }
        };
        CognitoUser cognitoUser = userPool.getUser(userId);
        cognitoUser.getSessionInBackground(authenticationHandler);

    }
}
