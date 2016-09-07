package org.gowind;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserProfileActivity extends AppCompatActivity {

    private Button mAddPaymentMethodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAddPaymentMethodButton = (Button) findViewById(R.id.addPayment);

        mAddPaymentMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
    }
}
