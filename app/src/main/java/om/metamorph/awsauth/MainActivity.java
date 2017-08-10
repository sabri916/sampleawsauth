package om.metamorph.awsauth;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.model.ChallengeNameType;

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
        CognitoUserPool userPool = new CognitoUserPool(this, poolId, clientId, clientSecret,
                clientConfiguration, Regions.EU_CENTRAL_1);
        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

            static final String TAG = "aws login";


            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                // Sign-in was successful, cognitoUserSession will contain tokens for the user
                Log.i(TAG, "login is successful");
                Log.i(TAG,String.valueOf(userSession.getAccessToken()));
                Log.i(TAG,String.valueOf(userSession.getIdToken()));
                Log.i(TAG,String.valueOf(userSession.getUsername()));
                Log.i(TAG,String.valueOf(userSession.isValid()));
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
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
            public void authenticationChallenge(final ChallengeContinuation continuation) {
                // A custom challenge has to be solved to authenticate
                Log.i(TAG, continuation.getChallengeName());
                if(continuation.getChallengeName().equals(ChallengeNameType.NEW_PASSWORD_REQUIRED.toString())){
                    NewPasswordContinuation newPasswordContinuation = (NewPasswordContinuation) continuation;
                    createNewPassword(newPasswordContinuation);
                }
                else{
                    Log.i(TAG, "challenge unknown");
                    continuation.continueTask();
                }

                // Call continueTask() method to respond to the challenge and continue with authentication.
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

    private void createNewPassword(final NewPasswordContinuation newPasswordContinuation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new Password");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPasswordContinuation.setPassword(input.getText().toString());
                newPasswordContinuation.continueTask();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
