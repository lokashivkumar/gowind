package org.gowind;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
    }

    public void submitCard(View view) {
        final String publishableApiKey = BuildConfig.DEBUG ?
                "pk_test_K1liIODIOqNaqUxvCVKcCfqM" :
                getString(R.string.com_stripe_publishable_key);

        TextView userNameField = (TextView) findViewById(R.id.ccUserName);
        TextView cardNumberField = (TextView) findViewById(R.id.cardNumber);
        TextView cardType = (TextView) findViewById(R.id.ccType);
        TextView addressLine1 = (TextView) findViewById(R.id.ccAddressLine1);
        TextView addressLine2 = (TextView) findViewById(R.id.ccAddressLine2);
        TextView addressCity = (TextView) findViewById(R.id.ccAddressCity);
        TextView addressState = (TextView) findViewById(R.id.ccAddressState);
        TextView addressCountry = (TextView) findViewById(R.id.ccAddressCountry);
        TextView addressZip = (TextView) findViewById(R.id.ccAddressZip);
        TextView cardCurrency = (TextView) findViewById(R.id.cardCurrency);
        TextView monthField = (TextView) findViewById(R.id.month);
        TextView yearField = (TextView) findViewById(R.id.year);
        TextView cvcField = (TextView) findViewById(R.id.cvc);
        TextView fingerprint = (TextView) findViewById(R.id.userFingerprint);
        TextView last4 = (TextView) findViewById(R.id.last4);

        Card card = new Card(cardNumberField.getText().toString(),
                Integer.valueOf(monthField.getText().toString()),
                Integer.valueOf(yearField.getText().toString()),
                cvcField.getText().toString(),
                userNameField.getText().toString(),
                cardType.getText().toString(),
                addressLine1.getText().toString(),
                addressLine2.getText().toString(),
                addressCity.getText().toString(),
                addressState.getText().toString(),
                addressCountry.getText().toString(),
                addressZip.getText().toString(),
                cardCurrency.getText().toString(),
                fingerprint.getText().toString(),
                last4.getText().toString());


        Stripe stripe = new Stripe();
        stripe.createToken(card, publishableApiKey, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                Toast.makeText(
                        getApplicationContext(),
                        "Token created: " + token.getId(),
                        Toast.LENGTH_LONG).show();
            }

            public void onError(Exception error) {
                Log.d("Stripe", error.getLocalizedMessage());
            }
        });
    }

}
